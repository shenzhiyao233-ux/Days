# 日子（Days）

一个离线、无广告、无需登录的原生 Android 倒数日工具。日常信息通过桌面 Widget 展示，App 只负责创建、编辑与配置。

## 技术

- Kotlin、Jetpack Compose、Material 3
- Jetpack Glance AppWidget（每个 Widget 独立绑定）
- Room 本地数据库
- Android Photo Picker（无相册权限）
- `java.time.LocalDate` 日期计算

最低 Android 8.0（API 26），目标 Android 15（API 35）。

## 打开与构建

用 Android Studio Ladybug 或更新版本打开项目根目录，等待 Gradle Sync，然后运行 `app`。推荐 JDK 17。

调试 APK：`./gradlew assembleDebug`

Release APK：`./gradlew assembleRelease`

未配置正式签名时 Release 产物为未签名 APK；可在 Android Studio 的 **Build > Generate Signed Bundle / APK** 创建 keystore 并签名。调试 APK 可直接安装测试。

## 照片背景

系统照片选择器只授予所选照片的临时读取能力。保存时会把照片采样、居中裁切并压缩成内部 JPEG（2×1 为 900×450，2×2 为 720×720），Widget 不读取原始大图。文件损坏或丢失时自动回退为默认背景。
