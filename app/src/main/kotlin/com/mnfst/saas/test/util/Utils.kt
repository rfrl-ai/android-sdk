package com.mnfst.saas.test.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import com.pocketimps.extlib.Proc
import com.pocketimps.extlib.tryOrFalse
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream


fun Closeable?.safeClose() {
  try {
    this?.close()
  } catch (ignored: Throwable) {}
}

fun <T> Uri.readStream(context: Context, readProc: (stream: InputStream) -> T?): T? {
  var fs: BufferedInputStream? = null
  var fd: ParcelFileDescriptor? = null
  val cr = context.contentResolver

  try {
    fs = BufferedInputStream(cr.openInputStream(this)!!)
  } catch (e: Exception) {
    try {
      fd = cr.openFileDescriptor(this, "r")
      fs = fd?.let {
        BufferedInputStream(FileInputStream(fd.fileDescriptor))
      }
    } catch (e: Exception) {}
  }

  return try {
    fs?.let(readProc)
  } catch (e: Exception) {
    null
  } finally {
    fs.safeClose()
    fd.safeClose()
  }
}

fun Uri.readStream(context: Context) =
    readStream(context, InputStream::readBytes)

fun ByteArray.loadBitmap(): Bitmap? = BitmapFactory.decodeByteArray(this, 0, size)
fun Uri.loadBitmap(context: Context) = readStream(context)?.loadBitmap()


fun Uri.toFile(context: Context) = try {
  val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
  context.contentResolver.query(this, filePathColumn, null, null, null)?.let { cursor ->
    cursor.moveToFirst()
    val columnIndex = cursor.getColumnIndex(filePathColumn[0])
    cursor.getString(columnIndex)?.let {
      cursor.close()
      File(it)
    }
  }
} catch (e: Exception) {
  null
}

fun Bitmap.toBytes(output: OutputStream, hasAlpha: Boolean = false) {
  if (hasAlpha)
    compress(Bitmap.CompressFormat.PNG, 0, output)
  else
    compress(Bitmap.CompressFormat.JPEG, 90, output)
}

fun Bitmap.saveToFileSync(file: File, hasAlpha: Boolean = false) = tryOrFalse {
  BufferedOutputStream(file.outputStream()).use {
    toBytes(it, hasAlpha)
  }
}

fun String.cleanOutput(): String {
  var changedOutput: StringBuilder? = null

  forEachIndexed { index, c ->
    var resChar = c

    if (!c.isDefined())
      changedOutput = (changedOutput ?: StringBuilder(substring(0, index))).also {
        resChar = '.'
      }

    changedOutput?.append(resChar)
  }

  return (changedOutput?.toString() ?: this).let {
    if (it.length > 2000)
      it.take(2000) + "(… truncated from ${it.length} chars)"
    else
      it
  }
}


object Utils {
  private val uiHandler = Handler(Looper.getMainLooper())

  private fun isMainThread() =
      (Thread.currentThread().id == Looper.getMainLooper().thread.id)

  fun runUi(proc: Proc) {
    if (isMainThread())
      proc.invoke()
    else
      uiHandler.post(proc)
  }

  fun runUiLater(delay: Long = 0L, proc: Runnable) {
    if (delay == 0L)
      uiHandler.post(proc)
    else
      uiHandler.postDelayed(proc, delay)
  }

  fun cancelUiTask(proc: Runnable) = uiHandler.removeCallbacks(proc)
}
