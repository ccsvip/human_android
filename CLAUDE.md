# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# 📐 数字人项目设计规范

1. **单一职责**  
   - 每个类和函数必须只负责一件事，不允许掺杂无关逻辑。  

2. **模块化**  
   - 必须按功能拆分模块，保持低耦合、高内聚，任何模块都能独立替换。  

3. **依赖倒置**  
   - 高层逻辑依赖抽象而不是具体实现，所有实现必须可替换。  

4. **事件驱动**  
   - 模块之间通过事件或状态流传递，避免强耦合和直接依赖链。  

5. **可测试性**  
   - 所有核心逻辑必须可单元测试，不允许依赖特定平台细节。  

6. **清晰边界**  
   - UI、业务逻辑、数据访问、工具必须严格分离，禁止跨层调用。  

7. **简洁函数**  
   - 函数必须保持清晰和简洁，逻辑复杂时必须拆分，不得出现冗长或难以维护的函数。  

8. **无全局状态**  
   - 禁止未受控的全局变量或单例，必须通过依赖注入或上下文传递。  

9. **错误处理**  
   - 异常必须统一捕获和处理，禁止散落或忽略错误。  

10. **日志与配置**  
    - 日志必须统一格式；所有常量与配置必须集中管理，不得硬编码。  

🚫 **禁止事项**  
- 不允许出现“上帝类”或任何集中所有逻辑的万能类。  
- 不允许在 UI 层直接编写业务逻辑或数据访问逻辑。  


## 项目概述

这是一个Android平台的2D虚拟数字人SDK项目，提供轻量级、纯离线的数字人解决方案，支持通过语音音频驱动数字人形象并进行实时渲染。

## 项目架构

### 核心模块
- **duix-sdk**: 核心SDK模块，包含数字人渲染、音频处理、动作控制等功能
- **test**: 示例应用，展示SDK的使用方法

### 技术栈
- **Android**: 最低API 24 (Android 7.0)，目标API 33/34
- **Kotlin**: 主要开发语言
- **C++**: Native代码，使用CMake构建
- **OpenGL ES**: 图形渲染
- **NCNN**: 神经网络推理框架
- **OpenCV**: 计算机视觉库
- **ONNX**: AI模型格式

### 核心组件
- `DUIX`: 数字人主控对象，集成模型加载、渲染、播报、动作功能
- `DUIXRenderer`: 默认渲染器，支持透明通道
- `DUIXTextureView`: 渲染视图组件
- `VirtualModelUtil`: 模型下载和管理工具
- `AudioPlayer`: 音频播放器

## 常用开发命令

### 构建项目
```bash
# 构建debug版本
./gradlew assembleDebug

# 构建release版本
./gradlew assembleRelease

# 构建SDK AAR
./gradlew :duix-sdk:assembleRelease
```

### 运行测试
```bash
# 安装测试应用
./gradlew :test:installDebug

# 运行Lint检查
./gradlew lint
```

### 清理项目
```bash
./gradlew clean
```

## 开发环境要求

### 系统要求
- Android Studio Giraffe 2022.3.1 Patch 2 或更高版本
- JDK 17 (在File->Settings->Build,Execution,Deployment->Gradle Projects->Gradle JDK中设置)
- CMake 3.18.1
- NDK (支持arm64-v8a和armeabi-v7a架构)

### 硬件要求
- CPU: 8核及以上(推荐骁龙8 Gen2)
- 内存: 8GB及以上
- 可用存储: 1GB及以上

## 重要配置

### ProGuard规则
如果启用代码混淆，必须在proguard-rules.pro中添加：
```proguard
-keep class ai.guiji.duix.DuixNcnn{*; }
```

### 音频格式要求
- PCM音频: 16kHz采样率、16bit位深、单通道
- WAV音频: 16kHz采样率、16bit位深、单通道
- 每段音频最少1秒(32000字节)才能触发口型驱动

### 内存要求
- 数字人运行需要至少800MB可用内存
- PCM音频缓存在内存中，避免过长的音频流

## SDK核心API

### 初始化流程
1. 检查和下载模型: `VirtualModelUtil.checkModel()` / `VirtualModelUtil.modelDownload()`
2. 创建DUIX实例: `new DUIX(context, modelName, renderSink, callback)`
3. 初始化: `duix.init()`
4. 等待初始化完成回调

### 音频驱动
- PCM流式推送: `startPush()` -> `pushPcm()` -> `stopPush()`
- WAV文件播放: `playAudio(wavPath)`
- 停止播放: `stopAudio()`

### 动作控制
- 指定动作: `startMotion(name, now)`
- 随机动作: `startRandomMotion(now)`

## 主要代码路径

### SDK源码
- `duix-sdk/src/main/java/ai/guiji/duix/sdk/client/DUIX.java` - 主要API接口
- `duix-sdk/src/main/java/ai/guiji/duix/sdk/client/render/` - 渲染相关代码
- `duix-sdk/src/main/java/ai/guiji/duix/sdk/client/VirtualModelUtil.java` - 模型管理工具
- `duix-sdk/src/main/cpp/` - Native代码

### 示例应用
- `test/src/main/java/ai/guiji/duix/test/ui/activity/MainActivity.kt` - 主界面
- `test/src/main/java/ai/guiji/duix/test/ui/activity/CallActivity.kt` - 数字人渲染界面

## 常见问题

### 性能优化
- 设备性能不足时可能导致音频特征提取速度跟不上播放速度
- 可使用`duix.setReporter()`监控帧渲染信息

### 模型下载
- 支持自建模型文件托管服务以解决下载速度问题
- 模型文件需要完整下载解压后才能使用

### 渲染问题
- 确保正确设置EGL配置以支持透明通道
- 使用SDK提供的DUIXRenderer和DUIXTextureView可快速集成

### 其他规则
为了方便溯源，每解决一个问题之后，都需要在 根目录 `docs`目录下面新建一个对应的文档加上当天日期，去记录解决了什么问题，日期格式必须是`YYYY-MM-DD`。
> 比如：`环境初初始化-2025-09-29.md`