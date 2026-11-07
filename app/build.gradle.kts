plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.xinman939.cineanchor.tv"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.xinman939.cineanchor.tv"
        minSdk = 26          // 覆盖主流盒子 (Android 8.0+)
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions { jvmTarget = "11" }
    buildFeatures { compose = true }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Compose BOM
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    // 设置对话框的 TextField 用 compose.material3 (TV 主组件仍是 androidx.tv.material3)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // TV 专用: 必须是 androidx.tv.material3, 不是 androidx.compose.material3!
    // (import 错了组件长得像但焦点行为完全不同)
    implementation(libs.androidx.tv.material)
    implementation(libs.androidx.tv.foundation)

    // 播放器: Media3 (ExoPlayer 已并入), HLS = m3u8 必须
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.exoplayer.hls)
    implementation(libs.media3.ui)

    // 网络
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)

    // 图片
    implementation(libs.coil.compose)

    // 路由 + 存储
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
}
