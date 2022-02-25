@file:Suppress("MemberVisibilityCanBePrivate")


object Versions {
  object Mnfst {
    const val VERSION_CODE = 16
    const val VERSION_NAME = "1.2.$VERSION_CODE"
  }

  object Kotlin {
    const val LANGUAGE = "1.6.10"
    const val COROUTINES = "1.6.0"
  }

  object Build {
    const val GRADLE_PLUGIN = "7.1.2"
    const val GMS = "4.3.10"
    const val R8 = "3.0.73"
  }

  object Android {
    const val MIN = 21
    const val TARGET = 32
    const val COMPILE = TARGET
  }

  object AppCompat {
    const val KTX = "1.7.0"
    const val APP_COMPAT = "1.4.1"
    const val RECYCLER = "1.2.1"
    const val CONSTRAINT_LAYOUT = "2.1.3"
  }

  object Network {
    const val RETROFIT = "2.9.0"
    const val LOGGING_INTERCEPTOR = "4.9.3"
  }

  object Utils {
    const val CRASHLYTICS = "18.2.8"
    const val EXO_PLAYER = "2.16.1"
    const val COIL = "1.4.0"
    const val KOIN = "3.1.5"
    const val EXTLIB = "1.0.1"
    const val UNLZMA = "1.0.1"
    const val TIMBER = "5.0.1"
    const val FACE_DETECTION = "16.2.0"
  }
}
