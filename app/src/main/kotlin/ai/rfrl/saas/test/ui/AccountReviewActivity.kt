package ai.rfrl.saas.test.ui

import ai.rfrl.saas.test.databinding.ActivityAccountReviewBinding
import ai.rfrl.saas.test.util.Config
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class AccountReviewActivity : AppCompatActivity(), KoinComponent {
  private val config: Config by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    ActivityAccountReviewBinding.inflate(layoutInflater).apply {
      setContentView(root)

      usernameInput.apply {
        setText(config.getRecentAccountReview())
        postDelayed(::selectAll, 100L)
      }

      startButton.setOnClickListener {
        val username = usernameInput.text.toString()
        setResult(RESULT_OK, Intent().putExtra("result", username))
        config.setRecentAccountReview(username)

        finish()
      }
    }
  }
}
