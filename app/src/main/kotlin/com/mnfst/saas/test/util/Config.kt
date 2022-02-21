package com.mnfst.saas.test.util

import android.content.Context


class Config(private val context: Context) {
  private val KEY_RECENT_ACCOUNT_REVIEW = "recent account review"

  private val prefs by lazy {
    context.getSharedPreferences("settings", Context.MODE_PRIVATE)
  }

  fun getRecentAccountReview() = prefs.getString(KEY_RECENT_ACCOUNT_REVIEW, null) ?: ""
  fun setRecentAccountReview(username: String) = prefs.edit().putString(KEY_RECENT_ACCOUNT_REVIEW, username).apply()
}
