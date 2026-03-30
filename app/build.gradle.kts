import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}
val geminiApiKey = localProperties.getProperty("GEMINI_API_KEY") ?: ""

android {
    namespace = "com.aadi.aurajournal"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.aadi.aurajournal"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")


    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true

    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.02.00")
    implementation(composeBom)

    // Core Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")

    // Material 3 Expressive (crucial for the new shapes and tonal elevation)
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3:material3-window-size-class")

//    Icons
    implementation("androidx.compose.material:material-icons-extended")
    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // Android Lifecycle & Activity
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
    implementation("androidx.activity:activity-compose:1.9.0")

//    pager components
    implementation("androidx.compose.foundation:foundation")

    // Google Gemini AI SDK for Android
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")


    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion") // For Coroutines & Flow
    kapt("androidx.room:room-compiler:${roomVersion}")

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.6.0")

//    biometrics
    implementation("androidx.biometric:biometric:1.2.0-alpha05")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}