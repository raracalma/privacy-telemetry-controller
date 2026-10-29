plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val keystorePath = System.getenv("KEYSTORE_PATH")
val keystorePasswordValue = System.getenv("KEYSTORE_PASSWORD")
val keyAliasValue = System.getenv("KEY_ALIAS")
val keyPasswordValue = System.getenv("KEY_PASSWORD")

val hasReleaseKey = listOf(
    keystorePath,
    keystorePasswordValue,
    keyAliasValue,
    keyPasswordValue
).all { !it.isNullOrBlank() }

android {
    namespace = "com.privacy.controller"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.privacy.controller"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "0.2.0"
    }

    signingConfigs {
        if (hasReleaseKey) {
            create("release") {
                storeFile = file(keystorePath!!)
                storePassword = keystorePasswordValue!!
                keyAlias = keyAliasValue!!
                keyPassword = keyPasswordValue!!
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            if (hasReleaseKey) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("dev.rikka.shizuku:api:13.1.5")
    implementation("dev.rikka.shizuku:provider:13.1.5")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
