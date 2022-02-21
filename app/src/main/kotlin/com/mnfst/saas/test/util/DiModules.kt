package com.mnfst.saas.test.util

import android.content.Context
import com.mnfst.saas.test.SdkRunner
import org.koin.dsl.module


// Default context provider module
fun Context.koinContextModule() = module {
  // Context
  single {
    this@koinContextModule
  }
}


val appModule = module {
  // Helper to invoke MNFST SDK functions
  single {
    SdkRunner(logger = get(),
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
