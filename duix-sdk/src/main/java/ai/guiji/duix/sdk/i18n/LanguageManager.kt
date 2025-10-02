/**
 * 语言管理器
 *
 * 功能：
 * - 管理应用的语言设置
 * - 提供语言切换功能
 * - 持久化用户的语言选择
 * - 支持中文和英文，可扩展其他语言
 *
 * 设计原则：
 * - 单例模式：全局统一的语言管理
 * - 观察者模式：通知监听器语言变更
 * - 持久化存储：SharedPreferences保存用户选择
 */
package ai.guiji.duix.sdk.i18n

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import java.util.*

object LanguageManager {

    private const val PREF_NAME = "language_settings"
    private const val KEY_LANGUAGE = "current_language"

    /**
     * 支持的语言枚举
     */
    enum class Language(val code: String, val displayName: String, val locale: Locale) {
        CHINESE("zh", "中文", Locale.SIMPLIFIED_CHINESE),
        ENGLISH("en", "English", Locale.ENGLISH);

        companion object {
            fun fromCode(code: String): Language {
                return values().find { it.code == code } ?: getSystemLanguage()
            }

            /**
             * 获取系统默认语言
             */
            fun getSystemLanguage(): Language {
                val systemLanguage = Locale.getDefault().language
                return when {
                    systemLanguage.startsWith("zh") -> CHINESE
                    else -> ENGLISH
                }
            }
        }
    }

    /**
     * 语言变更监听器
     */
    interface LanguageChangeListener {
        fun onLanguageChanged(language: Language)
    }

    private val listeners = mutableListOf<LanguageChangeListener>()

    /**
     * 获取当前语言
     * 默认返回中文
     */
    fun getCurrentLanguage(context: Context): Language {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val languageCode = prefs.getString(KEY_LANGUAGE, null)
        return if (languageCode != null) {
            Language.fromCode(languageCode)
        } else {
            // 默认使用中文
            Language.CHINESE
        }
    }

    /**
     * 设置语言
     */
    fun setLanguage(context: Context, language: Language) {
        // 保存语言设置
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()

        // 应用语言设置
        applyLanguage(context, language)

        // 通知监听器
        notifyLanguageChanged(language)
    }

    /**
     * 应用语言设置到Context
     */
    fun applyLanguage(context: Context, language: Language): Context {
        val locale = language.locale
        Locale.setDefault(locale)

        val resources: Resources = context.resources
        val config: Configuration = resources.configuration

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            return context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
            @Suppress("DEPRECATION")
            resources.updateConfiguration(config, resources.displayMetrics)
            return context
        }
    }

    /**
     * 获取所有支持的语言
     */
    fun getSupportedLanguages(): List<Language> {
        return Language.values().toList()
    }

    /**
     * 注册语言变更监听器
     */
    fun registerListener(listener: LanguageChangeListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    /**
     * 注销语言变更监听器
     */
    fun unregisterListener(listener: LanguageChangeListener) {
        listeners.remove(listener)
    }

    /**
     * 通知所有监听器语言已变更
     */
    private fun notifyLanguageChanged(language: Language) {
        listeners.forEach { it.onLanguageChanged(language) }
    }
}
