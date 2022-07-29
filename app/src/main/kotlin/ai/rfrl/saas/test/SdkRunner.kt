package ai.rfrl.saas.test

import ai.rfrl.saas.sdk.RfrlApi
import ai.rfrl.saas.sdk.RfrlCameraResult
import ai.rfrl.saas.sdk.RfrlContext
import ai.rfrl.saas.sdk.RfrlDebug
import ai.rfrl.saas.sdk.RfrlGenerationResult
import ai.rfrl.saas.sdk.RfrlInitConfig
import ai.rfrl.saas.sdk.RfrlInitStatus
import ai.rfrl.saas.sdk.RfrlModerationResult
import ai.rfrl.saas.sdk.RfrlRecognitionResult
import ai.rfrl.saas.sdk.RfrlSdk
import ai.rfrl.saas.test.util.Config
import ai.rfrl.saas.test.util.Logger
import ai.rfrl.saas.test.util.LoggerSink
import ai.rfrl.saas.test.util.saveToFileSync
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File


class SdkRunner(private val context: Context,
                private val config: Config,
                private val logger: Logger) {
  private var status: RfrlInitStatus? = null

  // DEV build of RFRL exposes debug interface, so you may tune up it a bit
  private var debug: RfrlDebug? = null

  // Current RFRL SDK context object
  private var rfrlContext: RfrlContext? = null

  private inline fun <T> ensureInitialized(block: () -> T): T? =
    if (status == RfrlInitStatus.SUCCESS)
      block.invoke()
    else
      null.also {
        logger.print("FAIL: RFRL SDK was not initialized")
      }
  
  private fun <R> debug(proc: RfrlDebug.() -> R): R? =
    debug?.let(proc::invoke)

  // Create RFRL SDK configuration
  private fun createInitConfig(apiConfig: ApiConfig): RfrlInitConfig {
    logger.print(">> createInitConfig()")

    // Don't forget to set your client token for RFRL SDK!
    val token = context.getString(apiConfig.tokenResId)
    logger.print(" - API config: \"${apiConfig.tag}\"")
    logger.print(" - Using client token \"${token.take(8)}…\"")

    return RfrlInitConfig(context = context,
                          clientToken = token)
  }

  fun initSdk() {
    logger.print(">> initSdk()")

    when (status) {
      RfrlInitStatus.SUCCESS ->
        return logger.print("SKIP: already initialized")

      RfrlInitStatus.STILL_IN_PROGRESS ->
        return logger.print("SKIP: initialization is still in progress")

      RfrlInitStatus.ERROR_INVALID_TOKEN ->
        return logger.print("FAIL: initialization failed due to invalid RFRL SDK token. Please, contact RFRL team")

      else -> {}
    }

    // Plant Timber logger sink to intercept logs from RFRL SDK
    Timber.plant(LoggerSink(logger::print))

    // Obtain API config, default is release
    val apiConfig = config.getApiConfig()
    val initConfig = createInitConfig(apiConfig)

    logger.print("Running RfrlSdk.init()…")
    RfrlSdk.init(initConfig) {
      status = it.status
      logger.print("RfrlSdk.init() result: \"$status\"")

      if (it.status == RfrlInitStatus.SUCCESS) {
        debug = try {
          // Try to obtain debug interface from RFRL SDK
          RfrlSdk.getDebugInterface()
        } catch (e: UnsupportedOperationException) {
          logger.print("Debug interface not available")
          null
        }

        // Display SDK version
        val version = RfrlSdk.getVersion()
        logger.print(" - RFRL SDK version: \"$version\"")

        // Change API endpoint
        debug?.setApiEndpoint(context.getString(apiConfig.urlResId))
      }
    }
  }

  // Ensure RFRL context is initialized
  private fun getContext() = ensureInitialized {
    rfrlContext ?: try {
      RfrlSdk.createContext().also {
        rfrlContext = it
      }
    } catch (e: Exception) {
      logger.print("FAIL: ${e.localizedMessage}")
      null
    }
  }

  fun isInitialized() = (status != null && status != RfrlInitStatus.STILL_IN_PROGRESS)
  fun hasDebug() = (debug != null)

  // Release current RFRL context, if any
  fun resetContext() = ensureInitialized {
    logger.print(">> resetContext()")

    rfrlContext?.apply {
      release()
      logger.print("- Old context released")
      rfrlContext = null
    }

    getContext()
  }

  // Dump content of given RFRL API object (debug build only)
  fun dumpObject(obj: RfrlApi) = debug {
    dumpObject(obj).also(logger::print)
  }

  // Dump current RFRL context
  fun dumpContext(): String? {
    logger.print(">> dumpContext()")
    val context = getContext() ?: return null.also {
      logger.print("- No current context")
    }

    return dumpObject(context)
  }

  // Start RFRL camera. Resulted image/video info will be stored inside current RFRL context
  fun startCamera(activity: Activity, resultCallback: (result: RfrlCameraResult) -> Unit) = ensureInitialized {
    logger.print(">> startCamera()")

    // Enable camera debug overlay (debug build only)
    debug {
      setCameraDebug(true)
    }

    getContext()?.startCamera(activity, resultCallback)
  }

  // Start image recognition. Result will be stored in current RFRL context.
  fun startImageRecognition(bitmap: Bitmap, resultCallback: (result: RfrlRecognitionResult) -> Unit) = ensureInitialized {
    logger.print(">> startImageRecognition()")

    getContext()?.apply {
      setOriginal(bitmap)
      startImageRecognition(resultCallback)
    }
  }

  fun startFileManualModeration(file: File, resultCallback: (result: RfrlModerationResult) -> Unit) = ensureInitialized {
    logger.print(">> startFileManualModeration(file: \"$file\")")
    getContext()?.startManualModeration(file, resultCallback)
  }

  // Creative moderation is performed on the generated video or image creative.
  // On completion, we have a verdict from the RFRL cloud
  fun startBitmapManualModeration(bitmap: Bitmap, resultCallback: (result: RfrlModerationResult) -> Unit) = ensureInitialized {
    logger.print(">> startBitmapManualModeration()")
    getContext()?.startManualModeration(bitmap, resultCallback)
  }

  fun canStartGeneration(): Boolean {
    logger.print(">> canStartGeneration()")
    val ctx = getContext() ?: return false

    return ctx.hasCreative().also {
      logger.print("- FAIL: Creative not set. Run camera first")
    }
  }

  fun startGeneration(resultCallback: (result: RfrlGenerationResult) -> Unit) = ensureInitialized {
    logger.print(">> startGeneration()")
    getContext()?.startCreativeGeneration(resultCallback)
  }

  suspend fun saveGeneratedCreative(result: RfrlGenerationResult) = ensureInitialized {
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

  fun startCreativeModeration(resultCallback: (result: RfrlModerationResult) -> Unit) = ensureInitialized {
    logger.print(">> startCreativeModeration()")
    getContext()?.startModeration(resultCallback)
  }
}
