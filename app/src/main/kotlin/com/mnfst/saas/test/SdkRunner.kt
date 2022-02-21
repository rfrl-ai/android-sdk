package com.mnfst.saas.test

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import com.mnfst.saas.sdk.MnfstAccountReviewResult
import com.mnfst.saas.sdk.MnfstApi
import com.mnfst.saas.sdk.MnfstCameraResult
import com.mnfst.saas.sdk.MnfstContext
import com.mnfst.saas.sdk.MnfstDebug
import com.mnfst.saas.sdk.MnfstGenerationResult
import com.mnfst.saas.sdk.MnfstInitConfig
import com.mnfst.saas.sdk.MnfstInitStatus
import com.mnfst.saas.sdk.MnfstModerationResult
import com.mnfst.saas.sdk.MnfstRecognitionResult
import com.mnfst.saas.sdk.MnfstSdk
import com.mnfst.saas.test.util.Logger
import com.mnfst.saas.test.util.LoggerSink
import com.mnfst.saas.test.util.saveToFileSync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File


class SdkRunner(private val context: Context,
                private val logger: Logger) {
  private var status: MnfstInitStatus? = null

  // DEV build of MNFST exposes debug interface, so you may tune up it a bit
  private var debug: MnfstDebug? = null

  // Current MNFST SDK context object
  private var mnfstContext: MnfstContext? = null

  private inline fun <T> ensureInitialized(block: () -> T): T? =
    if (status == MnfstInitStatus.SUCCESS)
      block.invoke()
    else
      null.also {
        logger.print("FAIL: MNFST SDK was not initialized")
      }
  
  private fun <R> debug(proc: MnfstDebug.() -> R): R? =
      debug?.let(proc::invoke)

  // Create MNFST SDK configuration
  private fun createInitConfig(): MnfstInitConfig {
    logger.print(">> createInitConfig()")

    // Don't forget to set your client token for MNFST SDK!
    val token = context.getString(R.string.token_mnfst)
    logger.print(" - Using client token \"${token.take(8)}…\"")

    return MnfstInitConfig(context = context,
                           clientToken = token)
  }

  fun initSdk() {
    logger.print(">> initSdk()")

    when (status) {
      MnfstInitStatus.SUCCESS ->
        return logger.print("SKIP: already initialized")

      MnfstInitStatus.STILL_IN_PROGRESS ->
        return logger.print("SKIP: initialization is still in progress")

      MnfstInitStatus.ERROR_INVALID_TOKEN ->
        return logger.print("FAIL: initialization failed due to invalid MNFST SDK token. Please, contact MNFST team")
    }

    // Plant Timber logger sink to intercept logs from MNFST SDK
    Timber.plant(LoggerSink(logger::print))

    val config = createInitConfig()

    logger.print("Running MnfstSdk.init()…")
    MnfstSdk.init(config) {
      status = it.status
      logger.print("MnfstSdk.init() result: \"$status\"")

      if (it.status == MnfstInitStatus.SUCCESS)
        debug = try {
          // Try to obtain debug interface from MNFST SDK
          MnfstSdk.getDebugInterface()
        } catch (e: UnsupportedOperationException) {
          logger.print("Debug interface not available")
          null
        }
    }
  }

  // Ensure MNFST context is initialized
  private fun getContext() = ensureInitialized {
    mnfstContext ?: try {
      MnfstSdk.createContext().also {
        mnfstContext = it
      }
    } catch (e: Exception) {
      logger.print("FAIL: ${e.localizedMessage}")
      null
    }
  }

  // Release current MNFST context, if any
  fun resetContext() = ensureInitialized {
    logger.print(">> resetContext()")

    mnfstContext?.apply {
      release()
      logger.print("- Old context released")
      mnfstContext = null
    }

    getContext()
  }

  // Dump content of given MNFST API object (debug build only)
  fun dumpObject(obj: MnfstApi) = debug {
    dumpObject(obj).also(logger::print)
  }

  // Dump current MNFST context
  fun dumpContext(): String? {
    logger.print(">> dumpContext()")
    val context = getContext() ?: return null.also {
      logger.print("- No current context")
    }

    return dumpObject(context)
  }

  // Start MNFST camera. Resulted image/video info will be stored inside current MNFST context
  fun startCamera(activity: Activity, resultCallback: (result: MnfstCameraResult) -> Unit) = ensureInitialized {
    logger.print(">> startCamera()")

    // Enable camera debug overlay (debug build only)
    debug {
      setCameraDebug(true)
    }

    getContext()?.startCamera(activity, resultCallback)
  }

  // Start image recognition. Result will be stored in current MNFST context.
  fun startImageRecognition(bitmap: Bitmap, resultCallback: (result: MnfstRecognitionResult) -> Unit) = ensureInitialized {
    logger.print(">> startImageRecognition()")

    getContext()?.apply {
      setOriginal(bitmap)
      startImageRecognition(resultCallback)
    }
  }

  fun startFileManualModeration(file: File, resultCallback: (result: MnfstModerationResult) -> Unit) = ensureInitialized {
    logger.print(">> startFileManualModeration(file: \"$file\")")
    getContext()?.startManualModeration(file, resultCallback)
  }

  // Creative moderation is performed on the generated video or image creative.
  // On completion, we have a verdict from the MNFST cloud
  fun startBitmapManualModeration(bitmap: Bitmap, resultCallback: (result: MnfstModerationResult) -> Unit) = ensureInitialized {
    logger.print(">> startBitmapManualModeration()")
    getContext()?.startManualModeration(bitmap, resultCallback)
  }

  // Start account review for given Instagram username
  fun startAccountReview(username: String, resultCallback: (result: MnfstAccountReviewResult) -> Unit) = ensureInitialized {
    logger.print(">> startAccountReview(username: \"$username\")")
    getContext()?.startAccountReview(username, resultCallback)
  }

  fun canStartGeneration(): Boolean {
    logger.print(">> canStartGeneration()")
    val ctx = getContext() ?: return false

    return ctx.hasCreative().also {
      logger.print("- FAIL: Creative not set. Run camera first")
    }
  }

  fun startGeneration(resultCallback: (result: MnfstGenerationResult) -> Unit) = ensureInitialized {
    logger.print(">> startGeneration()")
    getContext()?.startCreativeGeneration(resultCallback)
  }

  suspend fun saveGeneratedCreative(result: MnfstGenerationResult) = ensureInitialized {
    logger.print(">> saveGeneratedCreative()")
    
    withContext(Dispatchers.IO) {
      val targetDir = context.getExternalFilesDir(null)
      val targetFile: File

      try {
        if (result.isVideo) {
          targetFile = File(targetDir, "video.mp4")
          result.videoFile?.copyTo(targetFile, true)
        } else {
          targetFile = File(targetDir, "image.jpg")
          result.image?.saveToFileSync(targetFile)
        }

        logger.print("- OK: Saved to $targetFile")
      } catch (e: Exception) {
        logger.print("- FAIL: \"${e.message}\"")
      }
    }
  }

  fun startCreativeModeration(resultCallback: (result: MnfstModerationResult) -> Unit) = ensureInitialized {
    logger.print(">> startCreativeModeration()")
    getContext()?.startModeration(resultCallback)
  }
}
