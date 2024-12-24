plugins {
    id("com.android.library")
}

android {
    namespace = "com.dimen"
    compileSdk = 34

    defaultConfig {
        minSdk = 10
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}