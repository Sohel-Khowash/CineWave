plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
    id ("kotlin-parcelize")
}


android {
    namespace = "com.example.cinewave"
    compileSdk = 35

    buildFeatures{
        viewBinding = true
        buildConfig = true
    }
    defaultConfig {
        applicationId = "com.example.cinewave"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "API_KEY", "\"${project.properties["API_KEY"]}\"")

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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation (libs.glide)
    // Retrofit for API calls
    implementation (libs.retrofit)
    implementation (libs.converter.gson)

// Coroutines for async tasks
    implementation (libs.kotlinx.coroutines.android)

// Lifecycle & ViewModel
    implementation (libs.androidx.lifecycle.livedata.ktx)
    implementation (libs.androidx.lifecycle.viewmodel.ktx)

// Navigation Component
    implementation (libs.androidx.navigation.fragment.ktx)
    implementation (libs.androidx.navigation.ui.ktx)

// Room for local database (watchlist)
    implementation (libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler.v250)
    implementation (libs.androidx.room.ktx)

// Material Design for Bottom Navigation
    implementation (libs.material.v190)

    // Navigation Component
    implementation (libs.androidx.navigation.fragment.ktx.v277)
    implementation (libs.androidx.navigation.ui.ktx.v277)

    implementation (libs.glide)
    ksp (libs.compiler)

    implementation (libs.lottie)
    implementation(libs.lottie.v630)



}