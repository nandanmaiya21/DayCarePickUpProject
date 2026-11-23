plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.daycarepickupproject"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.daycarepickupproject"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    androidResources {
        // Keep the .tflite model uncompressed so it can be memory-mapped
        noCompress += "tflite"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)

    // Camera preview + frame analysis
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    // On-device face detection, landmarks and eye-open probability (bundled model, works offline)
    implementation(libs.mlkit.face.detection)
    // On-device face embeddings (MobileFaceNet .tflite in assets)
    implementation(libs.litert)
    // Local database
    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}