/**
 * 应用主入口Activity（路由器）
 *
 * 功能：
 * - 应用启动入口（LAUNCHER）
 * - 显示欢迎选择界面（WelcomeActivity）
 * - 根据用户选择路由到对应的功能界面：
 *   * 本地数字人 -> LocalMainActivity
 *   * 云端数字人 -> CloudMainActivity
 *
 * 设计原则：
 * - 单一职责：仅负责路由逻辑，不包含业务逻辑
 * - 无UI界面：作为透明的路由层
 */
package ai.guiji.duix.test.ui.activity

import ai.guiji.duix.sdk.welcome.WelcomeActivity
import ai.guiji.duix.sdk.welcome.WelcomeConfig
import ai.guiji.duix.test.ui.activity.cloud.CloudMainActivity
import ai.guiji.duix.test.ui.activity.local.LocalMainActivity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts


class MainActivity : BaseActivity() {

    /**
     * Activity Result API替代startActivityForResult
     */
    private val welcomeActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            WelcomeActivity.RESULT_LOCAL_CHOICE -> {
                // 用户选择本地数字人
                routeToLocalDigitalHuman()
            }
            WelcomeActivity.RESULT_CLOUD_CHOICE -> {
                // 用户选择云端数字人
                routeToCloudDigitalHuman()
            }
            else -> {
                // 用户取消选择，退出应用
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // MainActivity作为路由器，不需要UI
        // 直接根据状态进行路由
        checkAndRoute()
    }

    /**
     * 检查状态并进行路由
     */
    private fun checkAndRoute() {
        if (WelcomeConfig.shouldShowWelcomePage(this)) {
            // 第一次启动或用户清除了选择，显示欢迎页面
            showWelcomePage()
        } else {
            // 用户已经做过选择，直接根据选择路由
            routeBasedOnUserChoice()
        }
    }

    /**
     * 显示欢迎页面
     */
    private fun showWelcomePage() {
        val intent = Intent(this, WelcomeActivity::class.java)
        welcomeActivityLauncher.launch(intent)
    }

    /**
     * 根据用户历史选择进行路由
     */
    private fun routeBasedOnUserChoice() {
        val userChoice = WelcomeConfig.getUserChoiceEnum(this)
        when (userChoice) {
            WelcomeConfig.UserChoice.LOCAL -> {
                routeToLocalDigitalHuman()
            }
            WelcomeConfig.UserChoice.CLOUD -> {
                routeToCloudDigitalHuman()
            }
            else -> {
                // 未检测到有效选择，重新显示欢迎页面
                showWelcomePage()
            }
        }
    }

    /**
     * 路由到本地数字人界面
     */
    private fun routeToLocalDigitalHuman() {
        val intent = Intent(this, LocalMainActivity::class.java)
        startActivity(intent)
        finish()
    }

    /**
     * 路由到云端数字人界面
     */
    private fun routeToCloudDigitalHuman() {
        val intent = Intent(this, CloudMainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
