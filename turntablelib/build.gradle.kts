plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    namespace = "com.zhouz.turntablelib"
    compileSdk = 33

    defaultConfig {
        minSdk = 16
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.canvasanimation)
}