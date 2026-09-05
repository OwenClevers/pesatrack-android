import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

// Release signing -- keystore.properties (gitignored, see CLAUDE.md) points at
// a keystore kept outside the repo entirely. Absent on machines that don't
// need to produce a release build (e.g. CI running just tests/debug builds),
// so assembleRelease/bundleRelease fail clearly there instead of silently
// falling back to an unsigned or debug-signed artifact.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "com.pesatrack.app"

    compileSdk = 36

    defaultConfig {
        applicationId = "com.pesatrack.app"
        minSdk = 29
        targetSdk = 36

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    constraints {
        implementation("androidx.core:core:1.15.0") {
            because("1.16+ requires compileSdk 37 and AGP 9.1.0; toolchain is pinned at 36 / 8.10.1")
        }
        implementation("androidx.core:core-ktx:1.15.0") {
            because("1.16+ requires compileSdk 37 and AGP 9.1.0; toolchain is pinned at 36 / 8.10.1")
        }
    }
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.biometric)
    // Overrides the ancient androidx.fragment:1.2.5 that biometric:1.1.0 pulls
    // in transitively -- that version predates the fix making
    // ActivityResultRegistry's generated request codes safe for
    // FragmentActivity's 16-bit requestCode check, which crashed any
    // rememberLauncherForActivityResult call ("Can only use lower 16 bits for
    // requestCode") once MainActivity became a FragmentActivity for
    // BiometricPrompt.
    implementation(libs.androidx.fragment)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    // Real org.json implementation for unit tests -- android.jar's org.json
    // classes are unimplemented stubs outside Robolectric/instrumented tests.
    testImplementation(libs.json)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}