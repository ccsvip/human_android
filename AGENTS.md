# 代码仓库指南

## 项目结构与模块组织
- `duix-sdk/` 包含可重用的 SDK；API 位于 `ai.guiji.duix.sdk.client` 下，原生渲染/音频代码在 `src/main/cpp` 中。将新的公共接口保持在 `DUIX.java` 或其 Kotlin 对应文件附近。
- `test/` 提供示例启动器；UI 流程保持在 `ui/activity` 中，音频助手在 `audio/` 中，演示资源在 `src/main/assets` 中。APK 文件输出到 `test/build/outputs/apk/`。
- `docs/` 存储发布说明和事故报告——当行为或设置发生变化时更新相关文件。生成的构件应保留在 `build/` 内。

## 构建、测试和开发命令
- 运行 `./gradlew clean assembleDebug`（或 `.\gradlew.bat clean assembleDebug`）来构建演示；`./gradlew :duix-sdk:assembleRelease` 生成 AAR。
- `./gradlew lint` 强制执行 lint 基线（`android_glide_lint.xml`）；在 PR 之前解决或证明新警告的合理性。
- 对于设备检查，使用 `./gradlew :test:installDebug` 安装，当缓存资源漂移时通过 `adb shell pm clear ai.guiji.duix.test` 重置。

## 编码风格与命名约定
- 使用四空格缩进和 Android Studio 格式化。类遵循 `PascalCase`，成员 `camelCase`，常量 `UPPER_SNAKE_CASE`，如 `Constant.java` 中所示。
- 将入口点保持在 `ai.guiji.duix.sdk.client.*` 下，渲染助手在 `render/` 中，工具在 `util/` 中。文件名与类型名称保持一致（`WelcomeIntegrationExample.kt`），避免多类文件。
- 优先使用共享的 `Logger` 包装器；将原始 `android.util.Log` 保留给仅示例的代码片段。

## 测试指南
- 在 `duix-sdk/src/test/java` 中为纯逻辑添加 JVM 测试，在 `test/src/androidTest/java` 中添加仪器测试；文件命名为 `<Feature>Test.kt`。
- 在合并之前运行 `./gradlew test` 加上 `./gradlew connectedDebugAndroidTest`（设备或模拟器）。在 PR 中注明关键路径的覆盖率或手动检查。
- 在 `docs/` 中捕获冒烟测试步骤或资源验证列表，以便团队成员可以重现它们。

## 提交与拉取请求指南
- 历史记录偏好简洁的中文摘要（例如，`更新依赖文件和项目说明`）；在有帮助时添加模块前缀：`[duix-sdk] 修复渲染崩溃`。
- 拉取请求应包括范围、关联的问题/Jira、验证命令（`./gradlew lint`、`assembleDebug`）以及 UI 截图或录制。
- 指出配置变更（JDK 17、NDK 23.1.7779620、ABI 过滤器）并在入职先决条件变更时刷新 `README*.md`。

## 原生资源与配置提示
- 原生源码位于 `duix-sdk/src/main/cpp`，使用 CMake 4.1 脚本；在 `CMakeLists.txt` 中注册新文件并遵守 `arm64-v8a`/`armeabi-v7a` `abiFilters`。
- 将大型模型包保存在 `test/src/main/assets` 中或由 `VirtualModelUtil` 引用的外部 CDN 中；避免提交解密的缓存文件或构建输出。
