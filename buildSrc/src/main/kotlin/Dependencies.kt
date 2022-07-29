@file:Suppress("MemberVisibilityCanBePrivate")


object Versions {
  object App {
    const val VERSION_CODE = 28
    const val VERSION_NAME = "1.2.$VERSION_CODE"
  }

  object Kotlin {
    const val LANGUAGE = "1.7.10"
    const val COROUTINES = "1.6.4"
  }

  object Build {
    const val GRADLE_PLUGIN = "7.2.1"
    const val R8 = "3.1.51"
  }

  object Android {
    const val MIN = 21
    const val TARGET = 32
    const val COMPILE = TARGET
  }

  object AppCompat {
    const val KTX = "1.8.0"
    const val APP_COMPAT = "1.4.2"
    const val RECYCLER = "1.2.1"
    const val CONSTRAINT_LAYOUT = "2.1.4"
  }

  object Utils {
    const val KOIN = "3.2.0"
    const val TIMBER = "5.0.1"
    const val PHOENIX = "2.1.2"
    const val EXTLIB = "1.0.1"
  }
}
