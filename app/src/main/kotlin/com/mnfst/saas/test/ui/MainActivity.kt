package com.mnfst.saas.test.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.processphoenix.ProcessPhoenix
import com.mnfst.saas.sdk.MnfstGenerationStatus
import com.mnfst.saas.sdk.MnfstRecognitionStatus
import com.mnfst.saas.test.ApiConfig
import com.mnfst.saas.test.R
import com.mnfst.saas.test.SdkRunner
import com.mnfst.saas.test.databinding.ActivityMainBinding
import com.mnfst.saas.test.util.Config
import com.mnfst.saas.test.util.Logger
import com.mnfst.saas.test.util.MediaPicker
import com.mnfst.saas.test.util.PermissionManager
import com.mnfst.saas.test.util.Utils
import com.mnfst.saas.test.util.hasGranted
import com.pocketimps.extlib.BoolProc
import com.pocketimps.extlib.Proc
import com.pocketimps.extlib.uiLazy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import kotlin.coroutines.CoroutineContext


class MainActivity : AppCompatActivity(), CoroutineScope, KoinComponent {
  private val sdkRunner: SdkRunner by inject()
  private val config: Config by inject()
  private val permissionManager: PermissionManager by inject()
  private val logger: Logger by inject()

  private lateinit var binding: ActivityMainBinding

  private val rootJob = SupervisorJob()
  override val coroutineContext: CoroutineContext
    get() = Dispatchers.Main + rootJob

  private val CODE_MEDIA_PICKER_START = RESULT_FIRST_USER + 1
  private val CODE_RECOGNITION_IMAGE_PICK = CODE_MEDIA_PICKER_START
  private val CODE_MODERATION_IMAGE_PICK = CODE_MEDIA_PICKER_START + 1
  private val CODE_MODERATION_VIDEO_PICK = CODE_MEDIA_PICKER_START + 2
  private val CODE_MEDIA_PICKER_END = CODE_MODERATION_VIDEO_PICK

  private var inProgress = false


  private val logAdapter by uiLazy {
    LogAdapter(binding.consoleOutput, this)
  }


  private val mediaPicker = object : MediaPicker(this) {
    override fun onImageReady(bitmap: Bitmap?, requestCode: Int) {
      showProgress(false)

      when (requestCode) {
        CODE_RECOGNITION_IMAGE_PICK ->
          bitmap.proceedImageRecognition()

        CODE_MODERATION_IMAGE_PICK ->
          bitmap.proceedImageModeration()

      }
    }

    override fun onVideoReady(file: File, requestCode: Int) {
      showProgress(false)
      
      if (requestCode != CODE_MODERATION_VIDEO_PICK)
        return

      file.proceedVideoModeration()
    }

    override fun onPrepareImage() = showProgress(true)
    override fun onFailure() = Toast.makeText(this@MainActivity, R.string.message_picker_error, Toast.LENGTH_SHORT).show()
    override fun onComplete() = showProgress(false)
  }


  private fun showProgress(show: Boolean) {
    inProgress = show
    binding.progressBar.visibility = if (show)
      View.VISIBLE
    else
      View.GONE
  }

  private fun launchWithProgress(block: suspend () -> Unit) {
    showProgress(true)
    launch {
      block.invoke()
      showProgress(false)
    }
  }

  @SuppressLint("LogNotTimber")
  private fun debugPrint(message: String) {
    logger.print(message)
  }

  private fun requestStoragePermission(permission: String, runOnSuccess: Proc) {
    requestPermission(permission) {
      if (it)
        runOnSuccess.invoke()
      else
        Toast.makeText(this, R.string.message_no_storage_permission, Toast.LENGTH_SHORT).show()
    }
  }

  private fun requestPermission(permission: String, resultProc: BoolProc) {
    permissionManager.requestPermissions(arrayOf(permission), this) { result ->
      resultProc.invoke(result.hasGranted(permission))
    }
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    permissionManager.onRequestPermissionsResult(permissions, grantResults)
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
  }

  private fun requestReadStoragePermission(runOnSuccess: Proc) =
    requestStoragePermission(Manifest.permission.READ_EXTERNAL_STORAGE, runOnSuccess)

  private fun updateConfigButtons(allowAwait: Boolean = true) {
    // Await until SDK is initialized
    if (!sdkRunner.isInitialized())
      return Utils.runUiLater(500L, ::updateConfigButtons)

    // Debug interface might become available a bit later. Delay
    if (allowAwait && !sdkRunner.hasDebug())
      return Utils.runUiLater(2000L) {
        updateConfigButtons(false)
      }

    binding.apply {
      val initialApiConfig = config.getApiConfig()
      var apiConfig = initialApiConfig
      var restartRequired = false

      fun update() {
        apiConfigButton.text = getString(R.string.button_api_config_template, apiConfig.tag)
        restartButton.isInvisible = !restartRequired
      }

      if (sdkRunner.hasDebug())
        apiConfigButton.setOnClickListener {
          // Toggle API config
          apiConfig = if (apiConfig == ApiConfig.RELEASE)
            ApiConfig.DEV
          else
            ApiConfig.RELEASE

          config.setApiConfig(apiConfig)
          restartRequired = (apiConfig !== initialApiConfig)
          update()
        }

      // Freeze button, if debug interface is not provided by MNFST SDK
      apiConfigButton.isEnabled = sdkRunner.hasDebug()
      update()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    binding = ActivityMainBinding.inflate(layoutInflater)
    super.onCreate(savedInstanceState)

    binding.apply {
      setContentView(root)

      consoleOutput.layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
      consoleOutput.adapter = logAdapter

      restartButton.setOnClickListener {
        ProcessPhoenix.triggerRebirth(this@MainActivity)
      }

      resetContextButton.setOnClickListener {
        sdkRunner.resetContext()
      }

      dumpContextButton.setOnClickListener {
        sdkRunner.dumpContext()
      }

      startCameraButton.setOnClickListener {
        sdkRunner.startCamera(this@MainActivity) {
          debugPrint("- Camera finished with result:")
          sdkRunner.dumpObject(it)
        }
      }

      recognitionButton.setOnClickListener {
        requestReadStoragePermission {
          mediaPicker.selectFromGallery(this@MainActivity, CODE_RECOGNITION_IMAGE_PICK, false)
        }
      }

      imageModerationButton.setOnClickListener {
        requestReadStoragePermission {
          mediaPicker.selectFromGallery(this@MainActivity, CODE_MODERATION_IMAGE_PICK, false)
        }
      }

      videoModerationButton.setOnClickListener {
        requestReadStoragePermission {
          mediaPicker.selectFromGallery(this@MainActivity, CODE_MODERATION_VIDEO_PICK, true)
        }
      }

      generationButton.setOnClickListener {
        if (sdkRunner.canStartGeneration())
          startGeneration()
      }

      moderationButton.setOnClickListener {
        sdkRunner.startCreativeModeration {
          debugPrint("- Creative moderation finished with result:")
          sdkRunner.dumpObject(it)
        }
      }

      shareLogsButton.setOnClickListener {
        launchWithProgress {
          logAdapter.shareLogs()
        }
      }
    }

    updateConfigButtons()
  }

  override fun onDestroy() {
    super.onDestroy()
    rootJob.cancel()
  }

  private fun Bitmap?.proceedImageRecognition() {
    debugPrint(">> proceedImageRecognition(null ? ${this == null})")
    this ?: return

    sdkRunner.startImageRecognition(this) {
      debugPrint("- Image recognition finished with result:")
      sdkRunner.dumpObject(it)

      // MNFST cloud recognition service detected something inappropriate on the image.
      // Find and show the reason(s):
      if (it.status == MnfstRecognitionStatus.DECLINED) {
        it.reasons.keys.forEach { key ->
          if (it.reasons[key] == false)
            logger.print("\"${key.tag}\" failed")
        }
      }
    }
  }

  private fun Bitmap?.proceedImageModeration() {
    debugPrint(">> proceedImageModeration(null ? ${this == null})")
    this ?: return

    sdkRunner.startBitmapManualModeration(this) {
      debugPrint("- Manual image moderation finished with result:")
      sdkRunner.dumpObject(it)
    }
  }

  private fun File.proceedVideoModeration() {
    sdkRunner.startFileManualModeration(this) {
      debugPrint("- Manual file moderation finished with result:")
      sdkRunner.dumpObject(it)
    }
  }

  private fun startGeneration() {
    sdkRunner.startGeneration {
      debugPrint("- Creative generation finished with result:")
      sdkRunner.dumpObject(it)

      if (it.status == MnfstGenerationStatus.COMPLETED)
        launch {
          sdkRunner.saveGeneratedCreative(it)
        }
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (resultCode != RESULT_OK)
      return

    when (requestCode) {
      in CODE_MEDIA_PICKER_START..CODE_MEDIA_PICKER_END ->
        mediaPicker.onActivityResult(requestCode, resultCode, data,
                                     requestCode == CODE_MODERATION_VIDEO_PICK)
      else ->
        super.onActivityResult(requestCode, resultCode, data)
    }
  }

  override fun onBackPressed() {
    if (!inProgress)
      super.onBackPressed()
  }
}
