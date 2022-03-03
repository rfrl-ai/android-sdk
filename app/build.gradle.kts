plugins {
  id("com.android.application")
  kotlin("android")
  kotlin("kapt")
  google("gms.google-services")
  firebase("appdistribution")
  firebase("crashlytics")
}

android {
  compileSdk = Versions.Android.COMPILE
  defaultConfig {
    applicationId = "com.mnfst.saas.test"

    minSdk = Versions.Android.MIN
    targetSdk = Versions.Android.TARGET

    resourceConfigurations += "en"
    resourceConfigurations += "ru"

    versionCode = Versions.Mnfst.VERSION_CODE
    versionName = Versions.Mnfst.VERSION_NAME

    setProperty("archivesBaseName", "mnfst-saas-${Versions.Mnfst.VERSION_NAME}.${getGitHash()}")
  }

  firebaseAppDistribution {
    artifactType = "APK"
    groups = "androidsaastest"
  }

  sourceSets {
    getByName("main").java.srcDir("src/main/kotlin")
  }

  buildTypes {
    getByName("debug").signingConfig = signingConfigs.getByName("debug") {
      storeFile = File(projectDir, "debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }

    getByName("release").signingConfig = signingConfigs.create("release") {
      storeFile = File(projectDir, "release.keystore")
      val props = loadSigningProps()
      storePassword = props.getProperty("storePassword")
      keyAlias = props.getProperty("keyAlias")
      keyPassword = props.getProperty("keyPassword")
    }
  }

  buildFeatures.viewBinding = true
  lint.checkReleaseBuilds = false
  
  compileOptions.sourceCompatibility = JavaVersion.VERSION_1_8
  compileOptions.targetCompatibility = JavaVersion.VERSION_1_8
  kotlinOptions.jvmTarget = "1.8"
}


dependencies {
  // MNFST SDK. ":mnfst-dev" provides debug interface
  //implementation("com.mnfst.saas:sdk:${Versions.Mnfst.VERSION_NAME}")
  implementation("com.mnfst.saas:sdk-dev:${Versions.Mnfst.VERSION_NAME}")

  // Kotlin
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.Kotlin.LANGUAGE}")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.Kotlin.COROUTINES}")

  // Support libs
  implementation("androidx.core:core-ktx:${Versions.AppCompat.KTX}")
  implementation("androidx.appcompat:appcompat:${Versions.AppCompat.APP_COMPAT}")
  implementation("androidx.recyclerview:recyclerview:${Versions.AppCompat.RECYCLER}")
  implementation("androidx.constraintlayout:constraintlayout:${Versions.AppCompat.CONSTRAINT_LAYOUT}")

  // Utils
  implementation("io.insert-koin:koin-android:${Versions.Utils.KOIN}")
  implementation("com.google.firebase:firebase-crashlytics:${Versions.Utils.CRASHLYTICS}")
  implementation("com.jakewharton.timber:timber:${Versions.Utils.TIMBER}")
  implementation("com.jakewharton:process-phoenix:${Versions.Utils.PHOENIX}")
  implementation("com.pocketimps:extlib:${Versions.Utils.EXTLIB}")
}

taskAlias("upload" to listOf("assembleRelease", "appDistributionUploadRelease"))
