package ai.guiji.duix.sdk.welcome

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

/**
 * 欢迎页面Activity - 使用WebView展示选择界面
 *
 * 功能：
 * - 全屏显示欢迎页面
 * - 处理用户选择（本地/云端数字人）
 * - 与JavaScript交互
 * - 管理页面生命周期
 *
 * TODO: 后续可扩展的功能
 * - 页面加载进度显示
 * - 网络状态检测
 * - 主题切换支持
 * - 多语言支持
 */
class WelcomeActivity : AppCompatActivity() {

    companion object {
        // Intent常量
        const val EXTRA_SHOW_ANIMATION = "show_animation"

        // 选择结果常量
        const val RESULT_LOCAL_CHOICE = 100
        const val RESULT_CLOUD_CHOICE = 101

        /**
         * 启动欢迎页面
         * @param context 上下文
         * @param showAnimation 是否显示进入动画
         */
        fun start(context: Context, showAnimation: Boolean = true) {
            val intent = Intent(context, WelcomeActivity::class.java).apply {
                putExtra(EXTRA_SHOW_ANIMATION, showAnimation)
                // 如果从非Activity上下文启动，需要添加FLAG_ACTIVITY_NEW_TASK
                if (context !is Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            context.startActivity(intent)
        }
    }

    private lateinit var webView: WebView
    private var isPageLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置全屏和状态栏
        setupFullScreen()

        // 创建和配置WebView
        setupWebView()

        // 配置返回按钮处理
        setupBackPressedHandler()

        // 加载欢迎页面
        loadWelcomePage()
    }

    /**
     * 配置返回按钮处理
     * 使用OnBackPressedDispatcher替代onBackPressed
     */
    private fun setupBackPressedHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 防止意外退出，可以添加确认对话框
                finish()
            }
        })
    }

    /**
     * 设置全屏显示
     * 使用WindowInsetsController API (Android R+) 或 systemUiVisibility (旧版本)
     */
    private fun setupFullScreen() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // Android R (API 30) 及以上使用新API
            window.setDecorFitsSystemWindows(false)
            val windowInsetsController = window.insetsController
            windowInsetsController?.apply {
                // 隐藏状态栏和导航栏
                hide(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
                // 设置沉浸模式
                systemBarsBehavior = android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // Android R 以下使用旧API
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
        }

        // 设置状态栏透明
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        // 保持屏幕常亮
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    /**
     * 设置WebView配置
     */
    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView = WebView(this).apply {
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        // WebView基础设置
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            // 这些设置对于加载本地HTML文件是必需的
            // 虽然已被标记为deprecated，但对于本地assets中的文件是安全的
            @Suppress("DEPRECATION")
            allowFileAccessFromFileURLs = true
            @Suppress("DEPRECATION")
            allowUniversalAccessFromFileURLs = true

            // 缩放设置
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false

            // 缓存设置
            cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE

            // 字体设置
            defaultFontSize = 16
            minimumFontSize = 12
        }

        // 设置WebViewClient
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                isPageLoaded = true

                // TODO: 添加页面加载完成的动画效果
                animatePageEntry()
            }

            // 新版API (Android M及以上)
            override fun onReceivedError(
                view: WebView?,
                request: android.webkit.WebResourceRequest?,
                error: android.webkit.WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    handlePageError(error?.errorCode ?: -1, error?.description?.toString())
                }
            }

            // 旧版API (保持兼容性)
            @Deprecated("Use onReceivedError(WebView, WebResourceRequest, WebResourceError) instead")
            @Suppress("DEPRECATION")
            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                // TODO: 添加错误处理和重试机制
                handlePageError(errorCode, description)
            }
        }

        // 设置WebChromeClient用于调试
        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                // TODO: 添加调试日志收集
                consoleMessage?.let {
                    android.util.Log.d("WelcomeWebView", "${it.messageLevel()}: ${it.message()}")
                }
                return super.onConsoleMessage(consoleMessage)
            }
        }

        // 注册JavaScript接口
        webView.addJavascriptInterface(WebAppInterface(), "AndroidInterface")

        setContentView(webView)
    }

    /**
     * 加载欢迎页面
     */
    private fun loadWelcomePage() {
        val welcomeUrl = "file:///android_asset/welcome/welcome.html"

        // 简化处理，直接使用基本URL（避免BuildConfig依赖问题）
        webView.loadUrl(welcomeUrl)
    }

    /**
     * 页面进入动画
     */
    private fun animatePageEntry() {
        if (intent.getBooleanExtra(EXTRA_SHOW_ANIMATION, true)) {
            webView.alpha = 0f
            webView.animate()
                .alpha(1f)
                .setDuration(300)
                .start()
        }
    }

    /**
     * 处理页面加载错误
     */
    @Suppress("UNUSED_PARAMETER")
    private fun handlePageError(errorCode: Int, description: String?) {
        Toast.makeText(this, "页面加载失败: $description", Toast.LENGTH_SHORT).show()
        // TODO: 添加重试按钮或fallback UI
        finish()
    }

    /**
     * JavaScript接口类
     * 处理WebView与Android的通信
     */
    inner class WebAppInterface {

        /**
         * 处理用户选择
         * @param choice 用户选择类型 ("local" 或 "cloud")
         */
        @JavascriptInterface
        fun onUserChoice(choice: String) {
            runOnUiThread {
                handleUserChoice(choice)
            }
        }

        /**
         * 页面加载完成回调
         */
        @JavascriptInterface
        fun onPageLoaded() {
            runOnUiThread {
                // TODO: 可以在这里添加页面加载完成的额外处理
                android.util.Log.d("WelcomeActivity", "欢迎页面加载完成")
            }
        }
    }

    /**
     * 处理用户选择逻辑
     */
    private fun handleUserChoice(choice: String) {
        // 保存用户选择到配置
        WelcomeConfig.saveUserChoice(this, choice)

        // 设置结果并关闭Activity
        val resultCode = when (choice) {
            "local" -> RESULT_LOCAL_CHOICE
            "cloud" -> RESULT_CLOUD_CHOICE
            else -> RESULT_CANCELED
        }

        setResult(resultCode)

        // 添加退出动画
        webView.animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction {
                finish()
                // TODO: 添加Activity切换动画
                overridePendingTransition(0, android.R.anim.fade_out)
            }
            .start()
    }

    override fun onDestroy() {
        // 清理WebView资源
        webView.destroy()
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        if (::webView.isInitialized) {
            webView.onPause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::webView.isInitialized) {
            webView.onResume()
        }
    }
}