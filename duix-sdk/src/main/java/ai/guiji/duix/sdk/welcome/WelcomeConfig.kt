package ai.guiji.duix.sdk.welcome

import android.content.Context
import android.content.SharedPreferences

/**
 * 欢迎页面配置管理类
 *
 * 功能：
 * - 管理用户首次启动状态
 * - 保存和读取用户选择偏好
 * - 提供配置常量和工具方法
 *
 * TODO: 后续可扩展的功能
 * - 支持多用户配置
 * - 配置导入导出
 * - 远程配置更新
 * - 配置版本管理
 */
object WelcomeConfig {

    // SharedPreferences配置
    private const val PREF_NAME = "duix_welcome_config"
    private const val KEY_FIRST_LAUNCH = "is_first_launch"
    private const val KEY_USER_CHOICE = "user_choice"
    private const val KEY_CHOICE_TIMESTAMP = "choice_timestamp"
    private const val KEY_WELCOME_VERSION = "welcome_version"

    // 用户选择类型常量
    const val CHOICE_LOCAL = "local"
    const val CHOICE_CLOUD = "cloud"
    const val CHOICE_NONE = ""

    // 当前欢迎页面版本 - TODO: 支持版本更新检测
    private const val CURRENT_WELCOME_VERSION = 1

    /**
     * 检查是否为首次启动
     * @param context 上下文
     * @return true 如果是首次启动
     */
    fun isFirstLaunch(context: Context): Boolean {
        val prefs = getSharedPreferences(context)
        return prefs.getBoolean(KEY_FIRST_LAUNCH, true)
    }

    /**
     * 标记已完成首次启动
     * @param context 上下文
     */
    fun markFirstLaunchCompleted(context: Context) {
        val prefs = getSharedPreferences(context)
        prefs.edit()
            .putBoolean(KEY_FIRST_LAUNCH, false)
            .putInt(KEY_WELCOME_VERSION, CURRENT_WELCOME_VERSION)
            .apply()
    }

    /**
     * 保存用户选择
     * @param context 上下文
     * @param choice 用户选择类型
     */
    fun saveUserChoice(context: Context, choice: String) {
        val prefs = getSharedPreferences(context)
        prefs.edit()
            .putString(KEY_USER_CHOICE, choice)
            .putLong(KEY_CHOICE_TIMESTAMP, System.currentTimeMillis())
            .putBoolean(KEY_FIRST_LAUNCH, false)
            .putInt(KEY_WELCOME_VERSION, CURRENT_WELCOME_VERSION)
            .apply()

        // TODO: 添加选择统计和分析
        logUserChoice(choice)
    }

    /**
     * 获取用户选择
     * @param context 上下文
     * @return 用户选择类型，如果未选择则返回CHOICE_NONE
     */
    fun getUserChoice(context: Context): String {
        val prefs = getSharedPreferences(context)
        return prefs.getString(KEY_USER_CHOICE, CHOICE_NONE) ?: CHOICE_NONE
    }

    /**
     * 获取用户选择时间戳
     * @param context 上下文
     * @return 选择时间戳，如果未选择则返回0
     */
    fun getChoiceTimestamp(context: Context): Long {
        val prefs = getSharedPreferences(context)
        return prefs.getLong(KEY_CHOICE_TIMESTAMP, 0L)
    }

    /**
     * 检查用户是否已做出选择
     * @param context 上下文
     * @return true 如果用户已选择
     */
    fun hasUserMadeChoice(context: Context): Boolean {
        return getUserChoice(context) != CHOICE_NONE
    }

    /**
     * 重置欢迎页面配置（用于测试或重置功能）
     * @param context 上下文
     */
    fun resetWelcomeConfig(context: Context) {
        val prefs = getSharedPreferences(context)
        prefs.edit()
            .clear()
            .apply()
    }

    /**
     * 检查是否需要显示欢迎页面
     * @param context 上下文
     * @return true 如果需要显示欢迎页面
     */
    fun shouldShowWelcomePage(context: Context): Boolean {
        // 如果是首次启动，显示欢迎页面
        if (isFirstLaunch(context)) {
            return true
        }

        // TODO: 检查版本更新是否需要重新显示欢迎页面
        val savedVersion = getWelcomeVersion(context)
        if (savedVersion < CURRENT_WELCOME_VERSION) {
            return true
        }

        // 如果用户没有做出选择，显示欢迎页面
        return !hasUserMadeChoice(context)
    }

    /**
     * 获取保存的欢迎页面版本
     * @param context 上下文
     * @return 保存的版本号
     */
    private fun getWelcomeVersion(context: Context): Int {
        val prefs = getSharedPreferences(context)
        return prefs.getInt(KEY_WELCOME_VERSION, 0)
    }

    /**
     * 获取SharedPreferences实例
     */
    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 记录用户选择日志（用于分析和调试）
     */
    private fun logUserChoice(choice: String) {
        // TODO: 集成分析SDK，记录用户选择行为
        android.util.Log.i("WelcomeConfig", "用户选择: $choice, 时间: ${System.currentTimeMillis()}")
    }

    /**
     * 用户选择类型枚举
     */
    enum class UserChoice(val value: String, val displayName: String) {
        LOCAL(CHOICE_LOCAL, "本地数字人"),
        CLOUD(CHOICE_CLOUD, "云端数字人"),
        NONE(CHOICE_NONE, "未选择");

        companion object {
            /**
             * 从字符串值获取枚举
             */
            fun fromValue(value: String): UserChoice {
                return values().find { it.value == value } ?: NONE
            }
        }
    }

    /**
     * 获取用户选择的枚举对象
     */
    fun getUserChoiceEnum(context: Context): UserChoice {
        return UserChoice.fromValue(getUserChoice(context))
    }

    /**
     * 检查是否选择了本地数字人
     */
    fun isLocalChoiceSelected(context: Context): Boolean {
        return getUserChoice(context) == CHOICE_LOCAL
    }

    /**
     * 检查是否选择了云端数字人
     */
    fun isCloudChoiceSelected(context: Context): Boolean {
        return getUserChoice(context) == CHOICE_CLOUD
    }

    /**
     * 获取配置摘要信息（用于调试）
     */
    fun getConfigSummary(context: Context): String {
        val choice = getUserChoiceEnum(context)
        val timestamp = getChoiceTimestamp(context)
        val isFirst = isFirstLaunch(context)
        val version = getWelcomeVersion(context)

        return """
            欢迎页面配置摘要:
            - 首次启动: $isFirst
            - 用户选择: ${choice.displayName}
            - 选择时间: ${if (timestamp > 0) java.util.Date(timestamp) else "未选择"}
            - 配置版本: $version
            - 需要显示: ${shouldShowWelcomePage(context)}
        """.trimIndent()
    }
}