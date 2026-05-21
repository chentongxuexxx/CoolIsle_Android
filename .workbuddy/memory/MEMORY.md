# 缓岛 App — 长期记忆

## 项目信息
- 包名：`com.huandao.app`
- 加载地址：`https://t2hyptyq43t3m.ok.kimi.link/`
- 主题色：橙色 `#E8913A`，背景 `#FFF8F0`

## 技术栈
- Kotlin / AGP 8.2.0 / Kotlin 1.9.22 / JVM 17
- 编译SDK 34（Android 14），最低SDK 24（Android 7.0）
- Firebase（FCM + Crashlytics）
- WebView 封装型 App

## 四阶段完成情况
1. ✅ 废弃API修复 + 断网处理 + URL配置化
2. ✅ 进度条 + Adaptive Icon + 网络恢复重载
3. ✅ 签名配置 + CI自动构建 + ProGuard
4. ✅ JS Bridge + WebView缓存 + 推送通知

## 第五阶段完成情况（2026-05-20）
1. ✅ Splash Screen API 升级（androidx.core:core-splashscreen）
2. ✅ POST_NOTIFICATIONS 权限申请（Android 13+ 合规）
3. ✅ Firebase Crashlytics 崩溃监控
4. ✅ ProGuard 规则更新

## 关键文件
- `app/build.gradle.kts` — 主构建配置
- `app/src/main/java/com/huandao/app/MainActivity.kt` — 主Activity
- `app/proguard-rules.pro` — R8混淆规则
- `.github/workflows/android.yml` — CI配置
