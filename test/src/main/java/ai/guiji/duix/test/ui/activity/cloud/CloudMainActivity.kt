/**
 * 云端数字人界面
 *
 * 功能：
 * - 显示"云端数字人尚未开发"提示
 * - 提供返回主界面按钮
 * - 支持语言切换
 *
 * 说明：
 * 云端数字人功能尚在开发中，此界面作为占位符使用
 */
package ai.guiji.duix.test.ui.activity.cloud

import ai.guiji.duix.sdk.welcome.WelcomeConfig
import ai.guiji.duix.test.databinding.ActivityCloudMainBinding
import ai.guiji.duix.test.ui.activity.BaseActivity
import ai.guiji.duix.test.ui.activity.MainActivity
import ai.guiji.duix.test.ui.dialog.LanguageSelectorDialog
import android.content.Intent
import android.os.Bundle


class CloudMainActivity : BaseActivity() {

    private lateinit var binding: ActivityCloudMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCloudMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    /**
     * 设置UI组件
     */
    private fun setupUI() {
        // 语言切换按钮
        binding.btnLanguage.setOnClickListener {
            showLanguageSelector()
        }

        // 返回主界面按钮
        binding.btnBackToHome.setOnClickListener {
            backToMainMenu()
        }
    }

    /**
     * 显示语言选择器
     * 功能: 弹出语言选择对话框，用户选择语言后重新创建Activity
     */
    private fun showLanguageSelector() {
        LanguageSelectorDialog.show(this) { _ ->
            // 语言变更后，重新创建Activity以应用新语言
            recreate()
        }
    }
}
