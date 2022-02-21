package com.mnfst.saas.test

import android.app.Application
import com.mnfst.saas.test.util.appModule
import com.mnfst.saas.test.util.koinContextModule
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.startKoin
import org.koin.core.logger.Level


class App : Application(), KoinComponent {
  override fun onCreate() {
    super.onCreate()

    // Init DI modules
    startKoin {
      printLogger(Level.ERROR)
      modules(koinContextModule(),
              appModule)
    }

    // Initialization is asynchronous, its safe to do it on app startup
    val runner = get<SdkRunner>()
    runner.initSdk()
  }
}
