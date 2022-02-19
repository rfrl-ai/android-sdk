plugins {
  id("com.android.application")
  kotlin("android")
  kotlin("kapt")
  id("kotlin-android-extensions")
  id("com.google.gms.google-services")
}

android {
  compileSdk = Versions.Android.COMPILE
  defaultConfig {
    applicationId = "com.mnfst.saas.test"

    minSdk = Versions.Android.MIN
    targetSdk = Versions.Android.TARGET

    resourceConfigurations += "en"
    resourceConfigurations += "ru"

    versionCode = 11
    versionName = "1.2.$versionCode"
  }

  sourceSets {
    getByName("main").java.srcDir("src/main/kotlin")
  }

  signingConfigs {
    create("default") {
      storeFile = File("$projectDir/signing.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    getByName("debug") {
      versionNameSuffix = "-debug"
      signingConfig = signingConfigs.getByName("default")
    }

    getByName("release") {
      signingConfig = signingConfigs.getByName("default")
    }
  }

  // Drop some garbage from the final APK
  packagingOptions {
    resources.excludes += setOf("DebugProbesKt.bin",
                                "**.properties",
                                "kotlin*/**",
                                "okhttp3/internal/publicsuffix/NOTICE",
                                "**/**.version",
                                "**/**.kotlin_module")
  }

  // Enable view binding
  buildFeatures.viewBinding = true
  
  compileOptions.sourceCompatibility = JavaVersion.VERSION_1_8
  compileOptions.targetCompatibility = JavaVersion.VERSION_1_8
  kotlinOptions.jvmTarget = "1.8"
}


dependencies {
  // Dev SDK with debug interface
  debugImplementation(project(":mnfst-dev"))

  // Release SDK
  releaseImplementation(project(":mnfst"))

  // ---- Dependencies for MNFST SDK ----

  // Kotlin
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.Kotlin.LANGUAGE}")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.Kotlin.COROUTINES}")

  // Support libs
  implementation("androidx.core:core-ktx:${Versions.AppCompat.KTX}")
  implementation("androidx.appcompat:appcompat:${Versions.AppCompat.APP_COMPAT}")
  implementation("androidx.recyclerview:recyclerview:${Versions.AppCompat.RECYCLER}")
  implementation("androidx.constraintlayout:constraintlayout:${Versions.AppCompat.CONSTRAINT_LAYOUT}")

  // Firebase ML
  implementation("com.google.android.gms:play-services-mlkit-face-detection:${Versions.Utils.FACE_DETECTION}")

  // Networking
  implementation("com.squareup.retrofit2:retrofit:${Versions.Network.RETROFIT}")
  implementation("com.squareup.retrofit2:converter-gson:${Versions.Network.RETROFIT}")
  implementation("com.squareup.retrofit2:converter-scalars:${Versions.Network.RETROFIT}")
  implementation("com.squareup.okhttp3:logging-interceptor:${Versions.Network.LOGGING_INTERCEPTOR}")

  // Utils
  implementation("io.insert-koin:koin-android:${Versions.Utils.KOIN}")
  implementation("io.coil-kt:coil:${Versions.Utils.COIL}")
  implementation("com.google.android.exoplayer:exoplayer-core:${Versions.Utils.EXO_PLAYER}")
  implementation("com.pocketimps:extlib:${Versions.Utils.EXTLIB}")
  implementation("com.pocketimps:unlzma:${Versions.Utils.UNLZMA}")
  implementation("com.jakewharton.timber:timber:${Versions.Utils.TIMBER}")
}
