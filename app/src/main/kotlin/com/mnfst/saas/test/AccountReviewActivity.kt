package com.mnfst.saas.test

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mnfst.saas.test.databinding.ActivityAccountReviewBinding
import com.mnfst.saas.test.util.Config
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
