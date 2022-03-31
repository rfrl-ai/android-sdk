package ai.rfrl.saas.test.ui

import ai.rfrl.saas.test.R
import ai.rfrl.saas.test.util.LogSaver
import ai.rfrl.saas.test.util.Logger
import ai.rfrl.saas.test.util.Utils
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class LogAdapter(private val output: RecyclerView,
                 coroutineScope: CoroutineScope)
    : RecyclerView.Adapter<LogAdapter.ItemViewHolder>(),
      KoinComponent {
  private val baseLogger: Logger by inject()

  private var displayedCount = 0
  private val logs = ArrayList<String>()

  inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val textView = (itemView as TextView).also {
      it.movementMethod = LinkMovementMethod.getInstance()
    }

    fun setText(text: String) {
      textView.text = text
    }
  }


  private var scrollPending = false

  private val scrollProc = Runnable {
    scrollPending = false
    output.smoothScrollToPosition(logs.size - 1)
  }

  private val updateProc = Runnable {
    val added = (logs.size - displayedCount).coerceAtLeast(0)
    displayedCount = logs.size

    if (added > 0) {
      notifyItemRangeChanged(displayedCount, added)

      Utils.cancelUiTask(scrollProc)
      Utils.runUiLater(100L, scrollProc)
    }
  }


  init {
    coroutineScope.launch {
      baseLogger.collect {
        logs += it

        Utils.cancelUiTask(updateProc)
        Utils.runUiLater(100L, updateProc)
      }
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.item_log, parent, false)
    return ItemViewHolder(view)
  }

  override fun getItemCount() = logs.size

  override fun onBindViewHolder(holder: ItemViewHolder, position: Int) =
      holder.setText(logs[position])

  suspend fun shareLogs() {
    LogSaver(output.context, ArrayList(logs)).share()
  }
}
