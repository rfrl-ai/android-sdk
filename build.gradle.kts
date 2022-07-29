buildscript {
  repositories {
    google()
    mavenCentral()
    maven("https://dl.bintray.com/android/android-tools")
    maven("https://storage.googleapis.com/r8-releases/raw")
    maven("https://kotlin.bintray.com/kotlinx")
  }

  dependencies {
    classpath(kotlin("gradle-plugin", Versions.Kotlin.LANGUAGE))
    classpath("com.android.tools.build:gradle:${Versions.Build.GRADLE_PLUGIN}")
    classpath("com.android.tools:r8:${Versions.Build.R8}")
  }
}


allprojects {
  repositories {
    google()
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://rfrl-builds.s3.eu-central-1.amazonaws.com/saas/repository/")
  }
}
