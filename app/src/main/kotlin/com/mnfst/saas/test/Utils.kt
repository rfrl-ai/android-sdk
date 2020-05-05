package com.mnfst.saas.test

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.OutputStream


@SuppressLint("LogNotTimber")
fun String.log() {
  Log.i("App", this)
}


fun Bitmap.toBytes(output: OutputStream) =
    compress(Bitmap.CompressFormat.JPEG, 90, output)

fun Bitmap.saveToFile(file: File) =
    BufferedOutputStream(file.outputStream()).use {
      toBytes(it)
    }

fun Context.readBitmap(fileName: String): Bitmap = assets.open(fileName).use {
  BufferedInputStream(it).use(BitmapFactory::decodeStream)
}
