package com.mnfst.saas.test

import android.app.Application
import android.util.Log
import com.mnfst.saas.sdk.MnfstInitConfig
import com.mnfst.saas.sdk.MnfstInitStatus
import com.mnfst.saas.sdk.MnfstSdk


class App : Application() {
  // Create MNFST SDK configuration
  private fun createMnfstInitConfig(): MnfstInitConfig {
    val token = getString(R.string.token_mnfst)
    return MnfstInitConfig(this, token, null)
  }

  override fun onCreate() {
    super.onCreate()

    val config = createMnfstInitConfig()

    // Initialization is asynchronous, its safe to do it on app startup
    MnfstSdk.init(config) {
      if (it.status != MnfstInitStatus.SUCCESS) {
        Log.e("App", "Failed to initialize MNFST SDK: ${it.status}")
        throw IllegalStateException()
      }

      Log.i("App", "MNFST SDK is initialized")

      // DEV build of MNFST exposes debug interface, so you may tune up it a bit.
      if (BuildConfig.DEBUG) {
        val debug = MnfstSdk.getDebugInterface()
        
        // Enable additional tracing on camera screen
        debug.setCameraDebug(true)
      }
    }
  }
}
