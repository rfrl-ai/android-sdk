package ai.rfrl.saas.test.util

import ai.rfrl.saas.test.SdkRunner
import android.content.Context
import org.koin.dsl.module


// Default context provider module
fun Context.koinContextModule() = module {
  // Context
  single {
    this@koinContextModule
  }
}


val appModule = module {
  // Helper to invoke RFRL SDK functions
  single {
    SdkRunner(logger = get(),
              config = get(),
              context = get())
  }

  // Default logger
  single<Logger> {
    DefaultLogger()
  }

  // App configuration
  single {
    Config(context = get())
  }

  // Permission manager to support media picker
  single {
    PermissionManager(context = get())
  }
}
