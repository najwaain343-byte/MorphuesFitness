plugins {
    alias(libs.plugins.android.application)

    // Required for Firebase google-services.json
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.morpheusfitness"

    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.morpheusfitness"

        minSdk = 24
        targetSdk = 36

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    // =========================================
    // ANDROID
    // =========================================

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)


    // =========================================
    // MEDIAPIPE
    // =========================================

    implementation(
        "com.google.mediapipe:tasks-vision:latest.release"
    )


    // =========================================
    // CAMERAX
    // =========================================

    implementation(
        "androidx.camera:camera-core:1.4.2"
    )

    implementation(
        "androidx.camera:camera-camera2:1.4.2"
    )

    implementation(
        "androidx.camera:camera-lifecycle:1.4.2"
    )

    implementation(
        "androidx.camera:camera-view:1.4.2"
    )


    // =========================================
    // FIREBASE
    // =========================================

    implementation(
        platform(
            "com.google.firebase:firebase-bom:34.19.0"
        )
    )

    // Firebase Authentication
    implementation(
        "com.google.firebase:firebase-auth"
    )

    // Firebase Realtime Database
    implementation(
        "com.google.firebase:firebase-database"
    )

    implementation("androidx.recyclerview:recyclerview:1.4.0")


    // =========================================
    // TESTING
    // =========================================

    testImplementation(libs.junit)

    androidTestImplementation(
        libs.ext.junit
    )

    androidTestImplementation(
        libs.espresso.core
    )

}