plugins {
  id("com.android.application")
  id("kotlin-android")
  id("kotlin-android-extensions")
  id("kotlin-kapt")
  id("com.google.gms.google-services")
}


android {
  compileSdkVersion(Versions.Android.COMPILE)

  defaultConfig {
    applicationId = "com.mnfst.saas.test"

    minSdkVersion(Versions.Android.MIN)
    targetSdkVersion(Versions.Android.TARGET)

    resConfigs("en", "ru")

    versionCode = 1
    versionName = "1.0.$versionCode"
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
    exclude("DebugProbesKt.bin")
    exclude("**.properties")
    exclude("kotlin/**")
    exclude("okhttp3/internal/publicsuffix/NOTICE")
    exclude("META-INF/**.version")
    exclude("META-INF/**.kotlin_module")
    exclude("META-INF/com.android.tools/**")

    // TensorFlow libs are shipped compressed, unpacked on demand
    exclude("lib/armeabi-v7a/libtensorflowlite_jni.so")
    exclude("lib/arm64-v8a/libtensorflowlite_jni.so")
    exclude("lib/x86/libtensorflowlite_jni.so")
    exclude("lib/x86_64/libtensorflowlite_jni.so")
  }

  // Enable data binding
  dataBinding.isEnabled = true
  
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
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.Kotlin.COROUTINES}")

  // Support libs
  implementation("androidx.core:core:${Versions.AppCompat.CORE}")
  implementation("androidx.core:core-ktx:${Versions.AppCompat.CORE}")
  implementation("androidx.appcompat:appcompat:${Versions.AppCompat.APP_COMPAT}")
  implementation("androidx.activity:activity:${Versions.AppCompat.ACTIVITY}")
  implementation("androidx.recyclerview:recyclerview:${Versions.AppCompat.RECYCLER}")
  implementation("androidx.constraintlayout:constraintlayout:${Versions.AppCompat.CONSTRAINT_LAYOUT}")
  implementation("com.google.android.material:material:${Versions.AppCompat.MATERIAL}")

  // Firebase ML
  implementation("com.google.firebase:firebase-ml-vision:${Versions.ML.VISION}")
  implementation("com.google.firebase:firebase-ml-model-interpreter:${Versions.ML.MODEL_INTERPRETER}")

  // Networking
  implementation("com.squareup.retrofit2:retrofit:${Versions.Network.RETROFIT}")
  implementation("com.squareup.retrofit2:converter-gson:${Versions.Network.RETROFIT}")
  implementation("com.squareup.retrofit2:converter-scalars:${Versions.Network.RETROFIT}")
  implementation("com.squareup.okhttp3:logging-interceptor:${Versions.Network.LOGGING_INTERCEPTOR}")

  // Utils
  implementation("org.koin:koin-android:${Versions.Utils.KOIN}")
  //noinspection(GradleDependency)
  implementation("com.squareup.picasso:picasso:${Versions.Utils.PICASSO}")
  implementation("com.jakewharton.picasso:picasso2-okhttp3-downloader:${Versions.Utils.PICASSO_DOWNLOADER}")
  implementation("com.google.android.exoplayer:exoplayer-core:${Versions.Utils.EXO_PLAYER}")
  implementation("com.pocketimps:unlzma:${Versions.Utils.UNLZMA}")
  implementation("com.jakewharton.timber:timber:${Versions.Utils.TIMBER}")
}
