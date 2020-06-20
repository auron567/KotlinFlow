private object Versions {
    const val androidGradlePlugin = "4.0.0"
    const val kotlin = "1.3.72"
    const val coroutines = "1.3.7"
    const val appcompat = "1.1.0"
    const val fragment = "1.2.5"
    const val constraintLayout = "1.1.3"
    const val recyclerview = "1.1.0"
    const val material = "1.1.0"
    const val lifecycle = "2.2.0"
    const val room = "2.2.5"
    const val retrofit = "2.9.0"
    const val okhttp = "4.7.2"
    const val koin = "2.1.6"
    const val glide = "4.11.0"
    const val timber = "4.7.1"
    const val junit = "4.13"
}

object BuildPlugins {
    const val androidGradle = "com.android.tools.build:gradle:${Versions.androidGradlePlugin}"
    const val kotlinGradle = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
}

object Libs {
    const val kotlin = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}"
    const val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
    const val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"
    const val appcompat = "androidx.appcompat:appcompat:${Versions.appcompat}"
    const val fragmentKtx = "androidx.fragment:fragment-ktx:${Versions.fragment}"
    const val constraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"
    const val recyclerview = "androidx.recyclerview:recyclerview:${Versions.recyclerview}"
    const val material = "com.google.android.material:material:${Versions.material}"
    const val viewmodelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}"
    const val livedataKtx = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifecycle}"
    const val lifecycleRuntimeKtx = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycle}"
    const val roomRuntime = "androidx.room:room-runtime:${Versions.room}"
    const val roomKtx = "androidx.room:room-ktx:${Versions.room}"
    const val roomCompiler = "androidx.room:room-compiler:${Versions.room}"
    const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    const val retrofitConverterGson = "com.squareup.retrofit2:converter-gson:${Versions.retrofit}"
    const val loggingInterceptor = "com.squareup.okhttp3:logging-interceptor:${Versions.okhttp}"
    const val koinAndroid = "org.koin:koin-android:${Versions.koin}"
    const val koinViewmodel = "org.koin:koin-android-viewmodel:${Versions.koin}"
    const val glide = "com.github.bumptech.glide:glide:${Versions.glide}"
    const val glideCompiler = "com.github.bumptech.glide:compiler:${Versions.glide}"
    const val timber = "com.jakewharton.timber:timber:${Versions.timber}"
}

object TestLibs {
    const val junit = "junit:junit:${Versions.junit}"
}