import java.time.LocalDate
import java.time.format.DateTimeFormatter.ofPattern

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.ddnsgeek.ilinpetar.hsltimetable"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ddnsgeek.ilinpetar.hsltimetable"
        minSdk = 34
        targetSdk = 34
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    applicationVariants.all {
        resValue("string", "versionName", versionName)
        resValue("string", "copyrightYear", LocalDate.now().format(ofPattern("yyyy")))
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.converter.scalars)
    implementation(libs.preference)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}