package com.mnfst.saas.test

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mnfst.saas.sdk.MnfstGenerationStatus
import com.mnfst.saas.sdk.MnfstRecognitionStatus
import com.mnfst.saas.test.databinding.ActivityMainBinding
import com.mnfst.saas.test.util.Logger
import com.mnfst.saas.test.util.MediaPicker
import com.mnfst.saas.test.util.PermissionManager
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
  private val permissionManager: PermissionManager by inject()
  private val logger: Logger by inject()

  private lateinit var binding: ActivityMainBinding

  private val rootJob = SupervisorJob()
  override val coroutineContext: CoroutineContext
    get() = Dispatchers.Main + rootJob

  private val CODE_ACCOUNT_REVIEW = RESULT_FIRST_USER + 1
  private val CODE_MEDIA_PICKER_START = RESULT_FIRST_USER + 2
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

  override fun onCreate(savedInstanceState: Bundle?) {
    binding = ActivityMainBinding.inflate(layoutInflater)
    super.onCreate(savedInstanceState)

    binding.apply {
      setContentView(root)

      consoleOutput.layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
      consoleOutput.adapter = logAdapter

      resetContextButton.setOnClickListener {
        sdkRunner.resetContext()
      }

      dumpContextButton.setOnClickListener {
        sdkRunner.dumpContext()
      }

      recognitionButton.setOnClickListener {
        requestReadStoragePermission {
          mediaPicker.selectFromGallery(this@MainActivity, CODE_RECOGNITION_IMAGE_PICK, false)
        }
      }

      accountReviewButton.setOnClickListener {
        startActivityForResult(Intent(this@MainActivity, AccountReviewActivity::class.java), CODE_ACCOUNT_REVIEW)
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

      startCameraButton.setOnClickListener {
        sdkRunner.startCamera(this@MainActivity) {
          debugPrint("- Camera finished with result:")
          sdkRunner.dumpObject(it)
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

  private fun accountReview(username: String) {
    sdkRunner.startAccountReview(username) {
      debugPrint("- Account review finished with result:")
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
      CODE_ACCOUNT_REVIEW -> {
        val username = data?.getStringExtra("result")?.takeUnless(String::isNullOrBlank)
                       ?: return debugPrint("No username given")
        accountReview(username)
      }

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
