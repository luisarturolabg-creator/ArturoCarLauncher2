
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.arturo.carlauncher"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.arturo.carlauncher"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
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
    // Reproductor de audio ExoPlayer
    implementation("androidx.media3:media3-exoplayer:1.11.1")

    // Controles multimedia y reproducción en segundo plano
    implementation("androidx.media3:media3-session:1.11.1")
}
