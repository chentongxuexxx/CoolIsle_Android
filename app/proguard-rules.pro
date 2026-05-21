# =============================================
# 缓岛 App — ProGuard 规则
# =============================================

# ---- 通用优化 ----
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

# ---- Kotlin ----
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}

# ---- AndroidX / Jetpack ----
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-dontwarn androidx.**

# ---- Material Design ----
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# ---- WebView — 保持类名不变 ----
# WebView 通过反射调用 JavaScript 接口，必须保留完整类名
-keep class com.huandao.app.** { *; }
-keepclassmembers class com.huandao.app.** { *; }

# ---- ViewBinding ----
# ViewBinding 生成的 Binding 类会被 R8 移除，需要明确保留
-keep class * implements androidx.viewbinding.ViewBinding {
    public static ** bind(android.view.View);
    public static ** inflate(android.view.LayoutInflater);
}

# ---- WebView 运行时保留 ----
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public void *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.WebChromeClient {
    public void *(android.webkit.WebView, int);
}

# ---- 资源压缩优化 ----
# 移除未使用的资源，shrinkResources 需要 minifyEnabled=true 时才生效
#（已在 build.gradle.kts 中启用 isShrinkResources = true）

# ---- 网络相关 ----
-dontwarn java.net.**
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ---- 广告 / 第三方 SDK（按需取消注释）----
# -keep class com.google.android.gms.** { *; }
# -keep class com.google.firebase.** { *; }

# =============================================
# 第五阶段新增
# =============================================

# ---- Firebase Crashlytics ----
# 保留 Crashlytics 相关类，防止被 R8 移除导致崩溃不上报
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.crashlytics.**

# ---- Splash Screen API ----
# 保留 SplashScreen 相关类
-keep class androidx.core.splashscreen.** { *; }
-dontwarn androidx.core.splashscreen.**

# ---- Firebase Cloud Messaging ----
# 保留 FCM Service 和 Intent Filter
-keep class com.huandao.app.HuanDaoFirebaseService { *; }
-keep class com.google.firebase.messaging.** { *; }
