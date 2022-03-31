package ai.rfrl.saas.test.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream


abstract class MediaPicker(private val coroutineScope: CoroutineScope)
             : KoinComponent {
  private val context: Context by inject()

  abstract fun onImageReady(bitmap: Bitmap?, requestCode: Int)
  abstract fun onVideoReady(file: File, requestCode: Int)

  protected open fun onPrepareImage() {}
  protected open fun onFailure() {}
  protected open fun onComplete() {}


  private fun Uri.fetchFromCloud(): File? {
    val file = File(context.cacheDir, "tempFromCloud")
    var ok = false

    try {
      context.contentResolver.openFileDescriptor(this, "r")?.use { fd ->
        BufferedInputStream(FileInputStream(fd.fileDescriptor)).use { fin ->
          BufferedOutputStream(file.outputStream()).use { fout ->
            fin.copyTo(fout)
            ok = true
          }
        }
      }
    } catch (_: Exception) {
    } finally {
      if (!ok)
        file.delete()
    }

    return file.takeIf { ok }
  }

  private fun extractFromGallery(uri: Uri?, video: Boolean, requestCode: Int) {
    uri ?: return onImageReady(null, requestCode)
    onPrepareImage()

    coroutineScope.launch {
      if (video) {
        try {
          uri.toFile(context)?.also {
            onVideoReady(it, requestCode)
          } ?: run {
            withContext(Dispatchers.IO) {
              uri.fetchFromCloud()
            }?.also {
              onVideoReady(it, requestCode)
            } ?: onFailure()
          }
        } catch (e: Exception) {
          onFailure()
        }

        onComplete()
        return@launch
      }
      
      val bitmap = withContext(Dispatchers.IO) {
        uri.loadBitmap(context) ?: null.also { onFailure() }
      }

      onImageReady(bitmap, requestCode)
      onComplete()
    }
  }

  fun selectFromGallery(activity: Activity, requestCode: Int, video: Boolean) = try {
    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    intent.type = if (video) "video/mp4" else "image/*"
    activity.startActivityForResult(intent, requestCode)
    true
  } catch (e: ActivityNotFoundException) {
    false
  }

  fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?, video: Boolean) {
    if (resultCode == Activity.RESULT_OK)
      extractFromGallery(data?.data, video, requestCode)
    else
      onComplete()
  }
}
