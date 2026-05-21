import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// google-services 插件仅在 google-services.json 存在且为真实文件时加载
// 本地开发时放置真实文件即可启用 Firebase
// CI 会自动注入 GOOGLE_SERVICES_JSON Secret
val gservicesFile = file("google-services.json")
if (gservicesFile.exists() && gservicesFile.length() > 100) {
    plugins.apply("com.google.gms.google-services")
}

// 读取 signing.properties（不存在时不报错，仅用于本地签名）
val signingProperties = Properties().apply {
    val file = file("signing.properties")
    if (file.exists()) {
        load(file.inputStream())
    }
}

android {
    namespace = "com.huandao.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.huandao.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    signingConfigs {
        maybeCreate("release").apply {
            if (signingProperties.isNotEmpty()) {
                storeFile = file(signingProperties["storeFile"] as String)
                storePassword = signingProperties["storePassword"] as String
                keyAlias = signingProperties["keyAlias"] as String
                keyPassword = signingProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (signingProperties.isNotEmpty()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            // Debug 使用默认签名，适合开发测试
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

// Crashlytics Build ID（用于关联符号表）
// CI 会通过 gradle.properties 注入，未设置时使用默认值 "unspecified"
val crashlyticsBuildId: String by rootProject.extra

dependencies {
    implementation("androidx.core:core-ktx:1.13.0")
    // ---- Splash Screen API（Android 12+ 官方启动屏）----
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // ---- Firebase ----
    // 如需启用推送和崩溃监控，请在 Firebase Console 创建项目，下载 google-services.json
    // CI 会自动注入 GOOGLE_SERVICES_JSON Secret
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")
    // Crashlytics 崩溃监控
    implementation("com.google.firebase:firebase-crashlytics-ktx")
}
