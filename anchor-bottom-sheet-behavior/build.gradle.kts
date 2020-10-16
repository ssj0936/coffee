plugins{
    id("com.android.library")
}

android{
    compileSdkVersion(Versions.Android.sdk)

    defaultConfig {
        minSdkVersion(Versions.Android.minSdk)
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation("com.google.android.material:material:1.0.0")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.0.0")
}