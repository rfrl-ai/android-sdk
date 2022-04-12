plugins {
  id("com.android.application")
  kotlin("android")
  kotlin("kapt")
}

android {
  compileSdk = Versions.Android.COMPILE
  defaultConfig {
    applicationId = "ai.rfrl.saas.test"

    minSdk = Versions.Android.MIN
    targetSdk = Versions.Android.TARGET

    resourceConfigurations += "en"
    resourceConfigurations += "ru"

    versionCode = Versions.App.VERSION_CODE
    versionName = Versions.App.VERSION_NAME

    setProperty("archivesBaseName", "rfrl-saas-${Versions.App.VERSION_NAME}.${getGitHash()}")
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
  // RFRL SDK. ":sdk-dev" provides debug interface
  //implementation("ai.rfrl.saas:sdk:${Versions.App.VERSION_NAME}")
  implementation("ai.rfrl.saas:sdk-dev:${Versions.App.VERSION_NAME}")

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
  implementation("com.jakewharton.timber:timber:${Versions.Utils.TIMBER}")
  implementation("com.jakewharton:process-phoenix:${Versions.Utils.PHOENIX}")
  implementation("com.pocketimps:extlib:${Versions.Utils.EXTLIB}")
}

taskAlias("upload" to listOf("assembleRelease", "appDistributionUploadRelease"))
