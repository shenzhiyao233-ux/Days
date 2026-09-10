# 不安装 Android Studio：用 GitHub 自动生成 APK

1. 打开 GitHub，注册/登录账号。
2. 新建一个仓库（Repository），名称随意，例如 `Days`。
3. 解压本项目 ZIP，把解压后的**所有文件和文件夹**上传到仓库根目录。
4. 上传完成后，GitHub Actions 会自动开始构建。
5. 打开仓库顶部的 **Actions**，点开 `Build Android APK`。
6. 等构建显示绿色对勾后，在该运行页面底部找到 **Artifacts**。
7. 下载 `Days-debug-apk`，解压后得到 `app-debug.apk`。
8. 把 `app-debug.apk` 发到安卓手机并安装。

如果 Actions 没有自动运行：进入 **Actions > Build Android APK > Run workflow** 手动运行。

说明：这个构建生成的是调试版 APK，已带调试签名，适合自己安装和测试；以后如果要正式发布到应用商店，再配置正式签名。
