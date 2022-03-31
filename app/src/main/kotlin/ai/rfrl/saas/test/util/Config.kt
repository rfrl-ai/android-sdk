package ai.rfrl.saas.test.util

import ai.rfrl.saas.test.ApiConfig
import android.content.Context
import com.pocketimps.extlib.toTagged


class Config(private val context: Context) {
  private val KEY_API_CONFIG = "api config"
  private val KEY_RECENT_ACCOUNT_REVIEW = "recent account review"

  private val prefs by lazy {
    context.getSharedPreferences("settings", Context.MODE_PRIVATE)
  }


  private fun getString(key: String): String? = prefs.getString(key, null)
  private fun setString(key: String, value: String?) = prefs.edit().putString(key, value).apply()


  fun getApiConfig() = getString(KEY_API_CONFIG).toTagged() ?: ApiConfig.RELEASE
  fun setApiConfig(config: ApiConfig) = setString(KEY_API_CONFIG, config.tag)

  fun getRecentAccountReview() = getString(KEY_RECENT_ACCOUNT_REVIEW).orEmpty()
  fun setRecentAccountReview(username: String) = setString(KEY_RECENT_ACCOUNT_REVIEW, username)
}
