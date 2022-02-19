package com.mnfst.saas.test

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.mnfst.saas.sdk.MnfstApi
import com.mnfst.saas.sdk.MnfstContext
import com.mnfst.saas.sdk.MnfstGenerationStatus
import com.mnfst.saas.sdk.MnfstModerationStatus
import com.mnfst.saas.sdk.MnfstRecognitionStatus
import com.mnfst.saas.sdk.MnfstSdk
import com.mnfst.saas.test.databinding.ActivityMainBinding
import java.io.File


class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding

  private fun MnfstApi.dumpMnfstObject() =
      if (BuildConfig.DEBUG)
         MnfstSdk.getDebugInterface().dumpObject(this)
      else
        ""

  private fun showProgress() {
    binding.progressBar.visibility = View.VISIBLE
  }

  private fun hideProgress() {
    binding.progressBar.visibility = View.GONE
  }

  private fun finishOperation(ctx: MnfstContext) {
    // Do not forget to release MNFST context object
    ctx.release()
    hideProgress()
  }

  private fun runImageRecognition(ctx: MnfstContext, completionProc: () -> Unit) {
    ctx.startImageRecognition {
      "Image recognition finished with result status: ${it.status}".log()
      it.dumpMnfstObject().log()

      if (it.status == MnfstRecognitionStatus.DECLINED) {
        // MNFST cloud recognition service detected something inappropriate on the image.
        // Find and show the reason(s):
        it.reasons.keys.forEach { key ->
          if (it.reasons[key] == false)
            "\"${key.tag}\" failed".log()
        }
      }

      completionProc.invoke()
    }
  }

  private fun generateCreative(ctx: MnfstContext) {
    // If user selected video mask, we prepare video on the MNFST servers.
    // Generation site is valid for video creatives only and ignored for images.

    ctx.startCreativeGeneration {
      "Creative generation finished with result status: ${it.status}".log()
      it.dumpMnfstObject().log()

      if (it.status == MnfstGenerationStatus.COMPLETED) {
        // Save generated creative to file

        val targetDir = getExternalFilesDir(null)
        val targetFile: File

        try {
          if (it.isVideo) {
            targetFile = File(targetDir, "video.mp4")
            it.videoFile?.copyTo(targetFile, true)
          } else {
            targetFile = File(targetDir, "image.jpg")
            it.image?.saveToFile(targetFile)
          }

          "OK: Saved to $targetFile".log()
        } catch (e: Exception) {
          "Failed to save generated creative: \"${e.message}\"".log()
        }

        // Creative is generated. Now run moderation
        moderateCreative(ctx)
      } else
        finishOperation(ctx)
    }
  }

  private fun moderateCreative(ctx: MnfstContext) {
    // Here creative moderation is performed on the generated video or image creative.
    // On completion, we have a verdict from MNFST cloud

    ctx.startModeration {
      "Creative moderation finished with result status: ${it.status}".log()
      it.dumpMnfstObject().log()

      if (it.status == MnfstModerationStatus.DECLINED) {
        // MNFST moderation service declined given creative. Report the reason:
        "Reason: ${it.reason}".log()
      }

      finishOperation(ctx)
    }
  }

  private fun openCamera() {
    showProgress()

    // Allocate a MNFST context
    val ctx = MnfstSdk.createContext()

    // Open camera screen and wait for resulting image or video file
    ctx.startCamera(this) { res ->
      if (res.canceled)
        return@startCamera finishOperation(ctx)

      // OK, user has taken a photo (or picked from the gallery).
      // Now start image recognition
      runImageRecognition(ctx) {
        // Now we are ready to generate creative from the selected mask and taken (or picked) photo.
        generateCreative(ctx)
      }
    }
  }

  private fun startAccountReview() {
    showProgress()

    // Allocate a MNFST context
    val ctx = MnfstSdk.createContext()

    // Start account review for some Instagram username
    val username = "mnfst.official"
    ctx.startAccountReview(username) {
      "Account review finished with result status: ${it.status}".log()
      it.dumpMnfstObject().log()

      // Now it.account contains info about recent posts, likes etc. See documentation for details.

      finishOperation(ctx)
    }
  }

  private fun startManualImageRecognition() {
    showProgress()

    // Load bitmap from assets
    val bitmap = readBitmap("sample1.jpg")

    // Allocate a MNFST context
    val ctx = MnfstSdk.createContext()

    // Set bitmap as original
    ctx.setOriginal(bitmap)

    // Start recognition and wait for result
    runImageRecognition(ctx) {
      finishOperation(ctx)
    }
  }

  private fun startManualImageModeration() {
    showProgress()

    // Load bitmap from assets
    val bitmap = readBitmap("sample1.jpg")

    // Allocate a MNFST context
    val ctx = MnfstSdk.createContext()

    // Start moderation of given bitmap. You may provide a bitmap or video file instead

    ctx.startManualModeration(bitmap) {
      "Manual moderation finished with result status: ${it.status}".log()
      it.dumpMnfstObject().log()

      if (it.status == MnfstModerationStatus.DECLINED) {
        // MNFST moderation service declined given creative. Report the reason:
        "Reason: ${it.reason}".log()
      }

      finishOperation(ctx)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    binding.openCameraButton.setOnClickListener {
      openCamera()
    }

    binding.accountReviewButton.setOnClickListener {
      startAccountReview()
    }

    binding.manualRecognitionButton.setOnClickListener {
      startManualImageRecognition()
    }

    binding.manualModerationButton.setOnClickListener {
      startManualImageModeration()
    }
  }
}
