package ai.guiji.duix.sdk.welcome

import android.app.Application
import android.content.Context

/**
 * 欢迎页面集成示例
 *
 * 演示如何在您的应用中集成欢迎页面功能
 *
 * 使用步骤：
 * 1. 在Application或MainActivity中检查是否需要显示欢迎页面
 * 2. 根据需要启动WelcomeActivity
 * 3. 处理用户选择结果
 */
object WelcomeIntegrationExample {

    /**
     * 在Application onCreate中初始化检查
     * 推荐在Application中调用，确保每次应用启动都检查
     */
    fun initializeInApplication(application: Application) {
        // 可以在这里做一些预检查，但不建议直接启动Activity
        // 因为Application context不适合启动Activity

        // 可以设置一些全局配置
        setupGlobalConfig(application)
    }

    /**
     * 在MainActivity中检查并显示欢迎页面
     * 推荐在MainActivity的onCreate或onResume中调用
     */
    fun checkAndShowWelcomeInMainActivity(context: Context) {
        // 检查是否需要显示欢迎页面
        if (WelcomeConfig.shouldShowWelcomePage(context)) {
            // 启动欢迎页面
            WelcomeActivity.start(context, showAnimation = true)
        } else {
            // 用户已经做过选择，可以根据选择进行相应处理
            handleExistingUserChoice(context)
        }
    }

    /**
     * 处理已有的用户选择
     */
    private fun handleExistingUserChoice(context: Context) {
        when (WelcomeConfig.getUserChoiceEnum(context)) {
            WelcomeConfig.UserChoice.LOCAL -> {
                // 用户选择了本地数字人，可以预加载本地资源
                android.util.Log.i("WelcomeExample", "用户选择本地模式，准备加载本地资源...")
                // TODO: 在这里添加本地数字人初始化逻辑
            }
            WelcomeConfig.UserChoice.CLOUD -> {
                // 用户选择了云端数字人，可以检查网络连接
                android.util.Log.i("WelcomeExample", "用户选择云端模式，检查网络连接...")
                // TODO: 在这里添加云端数字人初始化逻辑
            }
            WelcomeConfig.UserChoice.NONE -> {
                // 理论上不应该到这里，因为shouldShowWelcomePage会返回true
                android.util.Log.w("WelcomeExample", "用户未做选择，但跳过了欢迎页面")
            }
        }
    }

    /**
     * 在Activity中处理欢迎页面返回结果
     * 在startActivityForResult中使用
     */
    fun handleWelcomeResult(requestCode: Int, resultCode: Int, context: Context) {
        when (resultCode) {
            WelcomeActivity.RESULT_LOCAL_CHOICE -> {
                android.util.Log.i("WelcomeExample", "用户选择了本地数字人")
                // TODO: 启动本地数字人相关功能
                initializeLocalDigitalHuman(context)
            }
            WelcomeActivity.RESULT_CLOUD_CHOICE -> {
                android.util.Log.i("WelcomeExample", "用户选择了云端数字人")
                // TODO: 启动云端数字人相关功能
                initializeCloudDigitalHuman(context)
            }
            else -> {
                android.util.Log.w("WelcomeExample", "用户取消了选择")
                // 用户取消选择，可能需要退出应用或显示默认界面
            }
        }
    }

    /**
     * 强制重新显示欢迎页面（用于设置页面）
     * 可以在设置页面提供"重新选择"功能
     */
    fun forceShowWelcome(context: Context) {
        // 重置配置
        WelcomeConfig.resetWelcomeConfig(context)
        // 显示欢迎页面
        WelcomeActivity.start(context, showAnimation = true)
    }

    /**
     * 获取当前配置状态（用于调试）
     */
    fun getConfigStatus(context: Context): String {
        return WelcomeConfig.getConfigSummary(context)
    }

    /**
     * 设置全局配置
     */
    private fun setupGlobalConfig(context: Context) {
        // TODO: 在这里可以设置一些全局配置
        // 比如分析SDK初始化、日志配置等
        android.util.Log.d("WelcomeExample", "全局配置已设置")
    }

    /**
     * 初始化本地数字人功能
     */
    private fun initializeLocalDigitalHuman(context: Context) {
        // TODO: 在这里添加本地数字人的初始化逻辑
        // 例如：
        // - 检查本地模型文件
        // - 预加载必要资源
        // - 初始化渲染引擎
        android.util.Log.i("WelcomeExample", "开始初始化本地数字人...")
    }

    /**
     * 初始化云端数字人功能
     */
    private fun initializeCloudDigitalHuman(context: Context) {
        // TODO: 在这里添加云端数字人的初始化逻辑
        // 例如：
        // - 检查网络连接
        // - 验证API密钥
        // - 建立WebSocket连接
        android.util.Log.i("WelcomeExample", "开始初始化云端数字人...")
    }
}