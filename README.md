# 缓岛 - Android App

> 用 WebView 封装缓岛网页的 Android 应用，支持下拉刷新、返回键导航等功能。

---

## 项目简介

| 项目 | 说明 |
|------|------|
| 应用名称 | 缓岛 |
| 应用ID | com.huandao.app |
| 版本 | 1.0.0 |
| 最低安卓版本 | Android 7.0 (API 24) |
| 目标安卓版本 | Android 14 (API 34) |

---

## 方法一：用 Android Studio 打开编译（推荐）

### 第一步：安装 Android Studio

1. 打开浏览器，访问 [https://developer.android.com/studio](https://developer.android.com/studio)
2. 点击绿色的 **"Download Android Studio"** 按钮
3. 下载完成后，双击安装包进行安装
4. 安装过程中保持默认选项，一路点击 **"Next"** 即可

### 第二步：打开本项目

1. 打开 Android Studio
2. 点击左上角的 **"File"** 菜单
3. 选择 **"Open..."**
4. 在弹出的文件选择器中，找到并选中本项目的文件夹（`缓岛_Android`）
5. 点击 **"OK"**
6. 等待 Android Studio 自动下载依赖（首次打开可能需要 5-10 分钟，请耐心等待）

### 第三步：编译 APK

1. 确认 Android Studio 顶部工具栏显示的是 **"app"**（不是其他选项）
2. 点击顶部菜单 **"Build"**
3. 选择 **"Build Bundle(s) / APK(s)"**
4. 选择 **"Build APK(s)"**
5. 等待编译完成（底部状态栏会显示进度）
6. 编译成功后，右下角会弹出提示 **"Build Analyzer detected..."**
7. 点击提示中的 **"locate"** 链接，即可找到生成的 APK 文件

### 第四步：安装到手机

**方式 A - 通过 USB 数据线直接安装（推荐开发调试）**

1. 用 USB 数据线将手机连接到电脑
2. 在手机上弹出提示，选择 **"文件传输"** 或 **"MTP"** 模式
3. 回到 Android Studio
4. 点击顶部工具栏的运行按钮（绿色的三角形播放按钮）
5. 等待编译和安装完成，应用会自动在手机上打开

**方式 B - 手动传输 APK 安装**

1. 找到生成的 APK 文件（路径在 `app/build/outputs/apk/debug/app-debug.apk`）
2. 将 APK 文件通过微信、QQ、邮件等方式发送到手机
3. 在手机上打开收到的 APK 文件
4. 如果系统提示 **"禁止安装未知来源应用"**，请点击 **"设置"** 并允许
5. 点击 **"安装"** 即可

---

## 方法二：用命令行编译（适合有开发经验的用户）

### 前置条件

- 已安装 JDK 17（可通过 `java -version` 检查）
- 已安装 Android SDK
- 已配置 `ANDROID_HOME` 环境变量

### 编译步骤

1. 打开终端（Terminal）
2. 切换到项目目录：
   ```bash
   cd /path/to/缓岛_Android
   ```
3. 运行编译命令：
   ```bash
   ./gradlew assembleDebug
   ```
   > Windows 用户请使用 `gradlew.bat assembleDebug`
4. 编译完成后，APK 文件位于：
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

### 编译 Release 版本

```bash
./gradlew assembleRelease
```

Release APK 位于：
```
app/build/outputs/apk/release/app-release-unsigned.apk
```

---

## 项目文件结构

```
缓岛_Android/
├── app/
│   ├── build.gradle.kts          # 应用模块构建配置
│   └── src/main/
│       ├── AndroidManifest.xml   # 应用清单
│       ├── java/com/huandao/app/
│       │   ├── MainActivity.kt   # 主Activity（WebView逻辑）
│       │   └── HuanDaoApplication.kt  # 应用类
│       └── res/
│           ├── layout/activity_main.xml       # 主布局
│           ├── values/colors.xml              # 颜色定义
│           ├── values/strings.xml             # 字符串
│           ├── values/themes.xml              # 主题样式
│           ├── drawable/splash_background.xml # 启动页背景
│           └── mipmap-xxxhdpi/ic_launcher.png # 应用图标
├── build.gradle.kts              # 项目级构建配置
├── settings.gradle.kts           # 项目设置
├── gradle.properties             # Gradle配置
├── gradle/wrapper/               # Gradle Wrapper
└── README.md                     # 本文件
```

---

## 功能说明

| 功能 | 说明 |
|------|------|
| WebView 加载 | 自动加载缓岛网页 |
| 下拉刷新 | 在页面顶部下拉可刷新当前页面 |
| 返回键支持 | 按返回键优先返回上一页，到达首页再退出 |
| 进度条 | 页面加载时显示橙色进度条 |
| 竖屏锁定 | 应用锁定竖屏显示 |
| 沉浸式状态栏 | 状态栏颜色与应用主题一致 |

---

## 常见问题

### Q1: 编译时提示 "Gradle sync failed"
- 点击 Android Studio 右上角的 **"Sync Now"** 重新同步
- 检查网络连接，确保可以访问 Google 服务器
- 如果在中国大陆，可能需要配置镜像源

### Q2: 找不到 APK 文件
- 编译成功后，点击菜单 **"Build" > "APK Analysis..."**
- 或在项目目录中查找 `app/build/outputs/apk/` 文件夹

### Q3: 手机安装时提示 "解析包时出现问题"
- 确保 APK 文件完整传输到手机
- 检查手机系统版本是否满足最低要求（Android 7.0+）
- 尝试重新编译

### Q4: Android Studio 打开项目后一直在加载
- 首次打开需要下载大量依赖，请耐心等待（可能需要 10-30 分钟）
- 可以查看底部状态栏的进度
- 确保网络连接稳定

---

## 技术栈

- **语言**: Kotlin
- **最低 SDK**: API 24 (Android 7.0)
- **目标 SDK**: API 34 (Android 14)
- **WebView**: Android 原生 WebView
- **UI 组件**: SwipeRefreshLayout（下拉刷新）

---

## 注意事项

1. 本应用需要 **联网权限**，首次打开需要网络连接
2. 网页内容依赖于服务器，请确保网络畅通
3. 如需修改加载的网页地址，请编辑 `MainActivity.kt` 中的 `loadUrl()` 方法

---

*项目生成时间: 2024*
