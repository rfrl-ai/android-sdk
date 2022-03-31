package ai.rfrl.saas.test.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


// Saves logs to text file, then shares to somewhere
class LogSaver(private val context: Context,
               private val logs: List<String>) {
  private fun getLogFileName(): String {
    val currentDate = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.US).format(Date().time)
    return "$currentDate.log"
  }

  private suspend fun saveLogs(): File? = withContext(Dispatchers.IO) {
    val dir = context.externalCacheDir ?: return@withContext null
    val fileName = File(dir, getLogFileName())

    var fileWriter: FileWriter? = null
    try {
      fileWriter = FileWriter(fileName, true)

      logs.forEach {
        fileWriter.write(it)
        fileWriter.write("\n")
      }
    } catch (e: IOException) {
      Toast.makeText(context, "Failed to save log file", Toast.LENGTH_SHORT).show()
    } finally {
      fileWriter?.safeClose()
    }

    fileName
  }

  private fun File.getUri(): Uri = try {
    FileProvider.getUriForFile(context, "${context.packageName}.provider", this)
  } catch (e: IllegalArgumentException) {
    // Workaround for some bugged Huawei devices
    val manufacturerHuawei = "Huawei"
    if (Build.MANUFACTURER == manufacturerHuawei &&
        (Build.VERSION.SDK_INT == Build.VERSION_CODES.O || Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1)) {
      try {
        val cacheFolder = File(context.cacheDir, manufacturerHuawei)
        val cacheLocation = File(cacheFolder, this.name)
        copyTo(cacheLocation, true)
        FileProvider.getUriForFile(context, "${context.packageName}.provider", cacheLocation)
      } catch (e: Throwable) {
        Uri.fromFile(this)
      }
    } else
      Uri.fromFile(this)
  }

  suspend fun share(): Boolean {
    val file = saveLogs() ?: return false

    val intent = Intent(Intent.ACTION_SEND).setType("text/plain")
    val uri = file.getUri()
    intent.putExtra(Intent.EXTRA_STREAM, uri)

    try {
      context.startActivity(intent)
    } catch (e: Exception) {
      Toast.makeText(context, "Unable to share log file", Toast.LENGTH_SHORT).show()
    }


    return true
  }
}
