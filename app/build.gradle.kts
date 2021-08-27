
plugins{
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
    kotlin("kapt")
}

android {
    compileSdkVersion(Versions.Android.sdk)
//    buildToolsVersion "29.0.3"
    kotlinOptions {
        jvmTarget = "1.8"
    }
    defaultConfig {
        applicationId = "com.timothy.coffee"
        minSdkVersion(Versions.Android.minSdk)
        targetSdkVersion(Versions.Android.sdk)
        versionCode= 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {

        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    dataBinding {
        isEnabled = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(fileTree("dir" to "libs", "include" to listOf("*.jar")))
    implementation(project(":anchor-bottom-sheet-behavior"))

    implementation(Dependencies.Kotlin.stdLib)
    implementation(Dependencies.Androidx.appcompat)
    implementation(Dependencies.Androidx.core)
    implementation(Dependencies.Androidx.constraintlayout)
    implementation(Dependencies.Androidx.legacy_support_v4)
    testImplementation(Dependencies.Test.jUnit)
    androidTestImplementation(Dependencies.Test.junit_ext)
    androidTestImplementation(Dependencies.Test.espresso)

    implementation(Dependencies.timber)
    implementation(Dependencies.google_play_services_maps)
    implementation(Dependencies.google_play_services_location)
    //life cycle
    implementation(Dependencies.Lifecycle.lifecycle_extensions)
    //noinspection LifecycleAnnotationProcessorWithJava8
    kapt(Dependencies.Lifecycle.lifecycle_compiler)

    //retrofix
    implementation(Dependencies.Retrofit.retrofit)
    implementation(Dependencies.Retrofit.adapter_rxjava2)
    implementation(Dependencies.Retrofit.converter_gson)
    // support lib
    implementation(Dependencies.material)
    implementation(Dependencies.Androidx.cardview)
    implementation(Dependencies.Androidx.recyclerview)
    implementation(Dependencies.Androidx.preference)

    implementation(Dependencies.RxJava.rxjava3)
    implementation(Dependencies.RxJava.rxAndroid)

    // Dagger core
    implementation(Dependencies.Dagger2.dagger)
    kapt(Dependencies.Dagger2.daggerCompiler)

    // Dagger android
    implementation(Dependencies.Dagger2.daggerAndroid)
    implementation(Dependencies.Dagger2.daggerAndroidSupport)
    kapt(Dependencies.Dagger2.daggerAndroidProcessor)

    //room
    implementation(Dependencies.Room.room)
    kapt(Dependencies.Room.roomCompiler)
    implementation(Dependencies.Room.roomKtx)
    implementation(Dependencies.Room.roomRxjava2)

    //okhttp3
    implementation(Dependencies.Okhttp3.okhttp3)
    implementation(Dependencies.Okhttp3.okhttp3LoggingInterceptor)
}
