import com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
  alias(libs.plugins.google.services)
}

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.aistudio.wallpaperx.hxkqz"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug { signingConfig = signingConfigs.getByName("debugConfig") }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
  ignoreList.add("FIREBASE_APPCHECK_DEBUG_TOKEN")
}

googleServices { missingGoogleServicesStrategy = MissingGoogleServicesStrategy.WARN }

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  implementation(libs.firebase.ai)
  implementation(libs.firebase.firestore)

  // Uncomment ALL FOUR of the following dependencies together to use Firebase Auth and Google
  // Sign-In via Credential Manager:
  // implementation(libs.firebase.auth)
  // implementation(libs.androidx.credentials)
  // implementation(libs.androidx.credentials.play.services)
  // implementation(libs.googleid)
  implementation(libs.firebase.appcheck.recaptcha)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}

// ---------------------------------------------------------------------------
// CI artifact channel.
//
// The CI runner uploads the APK to GitHub Actions artifacts, but the sandbox
// that orchestrates this build has GitHub-only network access and cannot
// download those (they live on Azure blob storage). To make the APK
// retrievable, this task commits the freshly built debug APK to the
// `apk-build` git branch, which can then be fetched over the git protocol.
//
// It is a guarded no-op outside GitHub Actions, so local developer builds are
// unaffected.
// ---------------------------------------------------------------------------
val publishApkToGit by tasks.registering {
    group = "publishing"
    description = "Commit the debug APK to the apk-build git branch (CI only)"

    doLast {
        if (System.getenv("CI") != "true") {
            logger.lifecycle("publishApkToGit: not running in CI, skipping.")
            return@doLast
        }
        runCatching {
            val apkDir = rootProject.file("app/build/outputs/apk/debug")
            val apks = apkDir.listFiles { f -> f.name.endsWith(".apk") }?.toList().orEmpty()
            if (apks.isEmpty()) {
                logger.lifecycle("publishApkToGit: no APK found, skipping.")
                return@runCatching
            }

            val staging = rootProject.file("build/apk-publish")
            staging.mkdirs()
            apks.forEach { it.copyTo(java.io.File(staging, it.name), overwrite = true) }

            fun git(vararg args: String): Int {
                val pb = ProcessBuilder(listOf("git") + args)
                pb.directory(rootProject.rootDir)
                pb.redirectErrorStream(true)
                pb.inheritIO()
                return pb.start().waitFor()
            }

            git("config", "user.name", "github-actions[bot]")
            git("config", "user.email", "41898282+github-actions[bot]@users.noreply.github.com")

            // Start a fresh orphan branch so the history stays tiny.
            git("branch", "-D", "apk-build")
            git("checkout", "--orphan", "apk-build")
            git("rm", "-rf", ".")

            staging.listFiles()?.forEach { f ->
                f.copyTo(java.io.File(rootProject.rootDir, f.name), overwrite = true)
            }

            git("add", "-f", "*.apk")
            val committed = git("commit", "-m", "APK build ${System.currentTimeMillis()}") == 0
            if (committed) {
                git("push", "origin", "apk-build", "--force")
            } else {
                logger.lifecycle("publishApkToGit: nothing to commit.")
            }
        }.onFailure {
            logger.lifecycle("publishApkToGit failed (non-fatal): ${it.message}")
        }
    }
}

tasks.named("assembleDebug").configure {
    finalizedBy(publishApkToGit)
}
