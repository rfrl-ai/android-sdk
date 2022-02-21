package com.mnfst.saas.test.util

import com.pocketimps.extlib.make
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.coroutines.CoroutineContext


interface Logger {
  fun print(message: String, t: Throwable? = null)
  fun print(priority: Int, tag: String?, message: String, t: Throwable?)
  suspend fun collect(collector: FlowCollector<String>)
}


class DefaultLogger : Logger, CoroutineScope {
  private val rootJob = SupervisorJob()
  override val coroutineContext: CoroutineContext
    get() = Dispatchers.Main + rootJob

  private val buffer = MutableSharedFlow<String>(100, onBufferOverflow = BufferOverflow.DROP_OLDEST)

  override fun print(message: String, t: Throwable?) {
    launch {
      val s = message.cleanOutput()
      buffer.emit(s)

      t?.make {
        val bytes = ByteArrayOutputStream().also {
          t.printStackTrace(PrintStream(it))
        }.toByteArray()

        val text = String(bytes)
        buffer.emit(text.cleanOutput())
      }
    }
  }

  override fun print(priority: Int, tag: String?, message: String, t: Throwable?) {
    print((tag?.let { "[$tag]: " } ?: "") + message, t)
  }

  override suspend fun collect(collector: FlowCollector<String>) =
    buffer.collect(collector)
}


class LoggerSink(private val logSink: (priority: Int, tag: String?, message: String, t: Throwable?) -> Unit)
    : Timber.Tree() {
  override fun log(priority: Int, tag: String?, message: String, t: Throwable?) =
    logSink.invoke(priority, tag, message, t)
}
