@file:Suppress("MemberVisibilityCanBePrivate")


object Versions {
  object App {
    const val VERSION_CODE = 26
    const val VERSION_NAME = "1.2.$VERSION_CODE"
  }

  object Kotlin {
    const val LANGUAGE = "1.6.10"
    const val COROUTINES = "1.6.0"
  }

  object Build {
    const val GRADLE_PLUGIN = "7.1.3"
    const val R8 = "3.1.51"
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

  object Utils {
    const val KOIN = "3.1.6"
    const val TIMBER = "5.0.1"
    const val PHOENIX = "2.1.2"
    const val EXTLIB = "1.0.1"
  }
}
