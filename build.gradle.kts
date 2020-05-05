buildscript {
  repositories {
    google()
    jcenter()
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
    maven("https://dl.bintray.com/android/android-tools")
    maven("http://storage.googleapis.com/r8-releases/raw")
    maven("https://kotlin.bintray.com/kotlinx")
  }

  dependencies {
    classpath("com.android.tools.build:gradle:${Versions.Build.GRADLE_PLUGIN}")
    classpath("com.android.tools.build.jetifier:jetifier-processor:${Versions.Build.JETIFIER}")
    classpath("com.google.gms:google-services:${Versions.Build.GMS}")
    classpath("com.android.tools:r8:${Versions.Build.R8}")
    classpath(kotlin("gradle-plugin", Versions.Kotlin.LANGUAGE))
  }
}


allprojects {
  repositories {
    google()
    jcenter()
    maven("https://jitpack.io")
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
  }
}
