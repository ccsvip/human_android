/**
 * 语言选择对话框
 *
 * 功能：
 * - 显示支持的语言列表（中文、英文）
 * - 用户选择语言后自动应用并重启Activity
 * - Material Design风格对话框设计
 *
 * 设计原则：
 * - 单一职责：仅负责语言选择UI
 * - 观察者模式：通过回调通知语言变更
 * - 用户体验优先：选择后立即生效
 */
package ai.guiji.duix.test.ui.dialog

import ai.guiji.duix.sdk.i18n.LanguageManager
import ai.guiji.duix.test.R
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class LanguageSelectorDialog(
    context: Context,
    private val onLanguageSelected: (LanguageManager.Language) -> Unit
) : Dialog(context) {

    private var selectedLanguage: LanguageManager.Language? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 获取当前语言
        val currentLanguage = LanguageManager.getCurrentLanguage(context)
        selectedLanguage = currentLanguage

        // 获取所有支持的语言
        val languages = LanguageManager.getSupportedLanguages()

        // 语言显示名称数组
        val languageNames = languages.map { language ->
            when (language) {
                LanguageManager.Language.CHINESE -> context.getString(R.string.language_chinese)
                LanguageManager.Language.ENGLISH -> context.getString(R.string.language_english)
            }
        }.toTypedArray()

        // 当前选中的索引
        val currentIndex = languages.indexOf(currentLanguage)

        // 构建对话框
        val builder = MaterialAlertDialogBuilder(context)
            .setTitle(R.string.language_dialog_title)
            .setSingleChoiceItems(languageNames, currentIndex) { _, which ->
                selectedLanguage = languages[which]
            }
            .setPositiveButton(R.string.dialog_confirm) { _, _ ->
                selectedLanguage?.let { language ->
                    // 如果语言有变化，应用新语言
                    if (language != currentLanguage) {
                        LanguageManager.setLanguage(context, language)

                        // 显示提示信息
                        Toast.makeText(
                            context,
                            R.string.language_changed,
                            Toast.LENGTH_SHORT
                        ).show()

                        // 通知调用者语言已变更
                        onLanguageSelected(language)
                    }
                }
                dismiss()
            }
            .setNegativeButton(R.string.dialog_cancel) { _, _ ->
                dismiss()
            }

        // 显示对话框
        builder.create().show()
    }

    companion object {
        /**
         * 显示语言选择对话框
         *
         * @param context 上下文
         * @param onLanguageSelected 语言选择回调
         */
        fun show(context: Context, onLanguageSelected: (LanguageManager.Language) -> Unit) {
            LanguageSelectorDialog(context, onLanguageSelected).show()
        }
    }
}
