object Dependencies{
    const val androidGradlePlugin = "com.android.tools.build:gradle:${Versions.gradle}"
    const val timber = "com.jakewharton.timber:timber:${Versions.timber}"
    const val google_play_services_location = "com.google.android.gms:play-services-location:${Versions.gms_location}"
    const val google_play_services_maps = "com.google.android.gms:play-services-maps:${Versions.play_services_maps}"
    const val material = "com.google.android.material:material:${Versions.material}"

    object Lifecycle {
        const val lifecycle_extensions =
            "androidx.lifecycle:lifecycle-extensions:${Versions.lifecycle}"
        const val lifecycle_compiler = "androidx.lifecycle:lifecycle-compiler:${Versions.lifecycle}"
    }

    object Retrofit{
        const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
        const val converter_gson = "com.squareup.retrofit2:converter-gson:${Versions.retrofit}"
        const val adapter_rxjava2 = "com.squareup.retrofit2:adapter-rxjava2:${Versions.retrofit}"
    }

    object RxJava{
        const val rxjava3 = "io.reactivex.rxjava3:rxjava:${Versions.rxjava}"
        const val rxAndroid = "io.reactivex.rxjava2:rxandroid:${Versions.rxjava2_rxandroid}"
    }

    object Dagger2{
        const val dagger = "com.google.dagger:dagger:${Versions.dagger2}"
        const val daggerCompiler = "com.google.dagger:dagger-compiler:${Versions.dagger2}"
        const val daggerAndroid = "com.google.dagger:dagger-android:${Versions.dagger2}"
        const val daggerAndroidSupport = "com.google.dagger:dagger-android-support:${Versions.dagger2}"
        const val daggerAndroidProcessor = "com.google.dagger:dagger-android-processor:${Versions.dagger2}"
    }

    object Room{
        const val room = "androidx.room:room-runtime:${Versions.room}"
        const val roomKtx = "androidx.room:room-ktx:${Versions.room}"
        const val roomRxjava2 = "androidx.room:room-rxjava2:${Versions.room}"
        const val roomCompiler = "androidx.room:room-compiler:${Versions.room}"
    }

    object Paging{
        const val paging = "androidx.paging:paging-runtime:${Versions.paging}"
        const val pagingCommon = "androidx.paging:paging-common:${Versions.paging}"
        const val pagingRxjava2 = "androidx.paging:paging-rxjava2:${Versions.paging}"
    }

    object Kotlin {
        const val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
        const val stdLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
    }

    object Androidx{
        const val appcompat = "androidx.appcompat:appcompat:${Versions.Androidx.appcompat}"
        const val core = "androidx.core:core-ktx:${Versions.Androidx.core}"
        const val constraintlayout = "androidx.constraintlayout:constraintlayout:${Versions.Androidx.constraintlayout}"
        const val legacy_support_v4 = "androidx.legacy:legacy-support-v4:${Versions.Androidx.legacy_support_v4}"

        const val cardview = "androidx.cardview:cardview:${Versions.Androidx.cardview}"
        const val recyclerview = "androidx.recyclerview:recyclerview:${Versions.Androidx.recyclerview}"
        const val preference = "androidx.preference:preference:${Versions.Androidx.preference}"
    }

    object Okhttp3{
        const val okhttp3 = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
        const val okhttp3LoggingInterceptor = "com.squareup.okhttp3:logging-interceptor:${Versions.okhttp}"
        const val conscrypt = "org.conscrypt:conscrypt-android:${Versions.conscrypt}"
    }

    object Test{
        const val jUnit = "junit:junit:${Versions.Test.junit}"
        const val junit_ext="androidx.test.ext:junit:${Versions.Test.junit_ext}"
        const val espresso = "androidx.test.espresso:espresso-core:${Versions.Test.espresso}"
    }

    object FireBase{
        const val firebaseBom = "com.google.firebase:firebase-bom:${Versions.firebase}"
        const val firebaseAnalyticsKtx = "com.google.firebase:firebase-analytics-ktx"
        const val firebaseCrashlyticsKtx = "com.google.firebase:firebase-crashlytics-ktx"
    }

}