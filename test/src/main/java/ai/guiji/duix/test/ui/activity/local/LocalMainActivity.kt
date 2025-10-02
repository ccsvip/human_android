/**
 * 本地数字人配置界面
 *
 * 功能：
 * - 配置和下载本地数字人模型
 * - 配置基础配置文件
 * - 跳转到本地数字人播放界面
 * - 提供返回主界面按钮
 * - 采用模块化设计，符合CLAUDE.md规范
 *
 * 设计原则：
 * - 单一职责: 每个函数只负责一个功能
 * - UI与业务分离: UI事件处理和业务逻辑分离
 * - 清晰注释: 每个函数都有清晰的功能说明
 */
package ai.guiji.duix.test.ui.activity.local

import ai.guiji.duix.sdk.client.VirtualModelUtil
import ai.guiji.duix.test.R
import ai.guiji.duix.test.databinding.ActivityLocalMainBinding
import ai.guiji.duix.test.ui.activity.BaseActivity
import ai.guiji.duix.test.ui.dialog.LanguageSelectorDialog
import ai.guiji.duix.test.ui.dialog.LoadingDialog
import ai.guiji.duix.test.ui.dialog.ModelSelectorDialog
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import java.io.File


class LocalMainActivity : BaseActivity() {

    private lateinit var binding: ActivityLocalMainBinding
    private var mLoadingDialog: LoadingDialog? = null
    private var mLastProgress = 0
    private var isAdvancedExpanded = false

    // 可选模型列表
    val models = arrayListOf(
        "https://github.com/duixcom/Duix-Mobile/releases/download/v1.0.0/bendi3_20240518.zip",
        "https://github.com/duixcom/Duix-Mobile/releases/download/v1.0.0/airuike_20240409.zip",
        "https://github.com/duixcom/Duix-Mobile/releases/download/v1.0.0/675429759852613_7f8d9388a4213080b1820b83dd057cfb_optim_m80.zip",
        "https://github.com/duixcom/Duix-Mobile/releases/download/v1.0.0/674402003804229_f6e86fb375c4f1f1b82b24f7ee4e7cb4_optim_m80.zip",
        "https://github.com/duixcom/Duix-Mobile/releases/download/v1.0.0/674400178376773_3925e756433c5a9caa9b9d54147ae4ab_optim_m80.zip",
        "https://github.com/duixcom/Duix-Mobile/releases/download/v1.0.0/674397294927941_6e297e18a4bdbe35c07a6ae48a1f021f_optim_m80.zip",
        "https://github.com/duixcom/Duix-Mobile/releases/download/v1.0.0/674393494597701_f49fcf68f5afdb241d516db8a7d88a7b_optim_m80.zip",
        "https://github.com/duixcom/Duix-Mobile/releases/download/v1.0.0/651705983152197_ccf3256b2449c76e77f94276dffcb293_optim_m80.zip",
        "https://github.com/duixcom/Duix-Mobile/releases/download/v1.0.0/627306542239813_1871244b5e6912efc636ba31ea4c5c6d_optim_m80.zip",
    )

    private var mBaseConfigUrl = ""
    private var mModelUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocalMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDefaultValues()
        setupUI()
    }

    /**
     * 设置默认值
     * 功能: 初始化默认的配置URL
     */
    private fun setupDefaultValues() {
        // 使用默认的baseConfig URL(不再从UI获取)
        mBaseConfigUrl = "https://github.com/duixcom/Duix-Mobile/releases/download/v1.0.0/gj_dh_res.zip"
    }

    /**
     * 设置UI组件
     * 功能: 绑定所有UI控件的事件监听器
     */
    private fun setupUI() {
        // 语言切换按钮
        binding.btnLanguage.setOnClickListener {
            showLanguageSelector()
        }

        // 返回主界面按钮
        binding.fabBackToHome.setOnClickListener {
            backToMainMenu()
        }

        // 刷新按钮
        binding.ivRefresh.setOnClickListener {
            refreshAddress()
        }

        // 模型选择下拉框
        binding.tvModelSelector.setOnClickListener {
            showModelSelectorDialog()
        }

        binding.layoutModel.setOnClickListener {
            showModelSelectorDialog()
        }

        // 应用选择下拉框(预留功能)
        binding.tvAppSelector.setOnClickListener {
            showAppSelectorDialog()
        }

        binding.layoutApp.setOnClickListener {
            showAppSelectorDialog()
        }

        // 高级设置展开/收起
        binding.tvAdvancedToggle.setOnClickListener {
            toggleAdvancedSettings()
        }

        // 启动按钮
        binding.btnStart.setOnClickListener {
            play()
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

    /**
     * 刷新地址
     * 功能: 清空或重置输入地址
     */
    private fun refreshAddress() {
        Toast.makeText(mContext, "刷新地址", Toast.LENGTH_SHORT).show()
        // 可选: 清空输入框或重置为默认值
        // binding.etAddress.setText("")
    }

    /**
     * 显示模型选择对话框
     * 功能: 弹出模型选择对话框，选择后更新UI和地址
     */
    private fun showModelSelectorDialog() {
        val modelSelectorDialog = ModelSelectorDialog(mContext, models, object : ModelSelectorDialog.Listener {
            override fun onSelect(url: String) {
                binding.etAddress.setText(url)
                // 更新模型选择显示
                val modelName = extractModelName(url)
                binding.tvModelSelector.text = modelName
                binding.tvModelSelector.setTextColor(getColor(R.color.local_main_text_primary))
            }
        })
        modelSelectorDialog.show()
    }

    /**
     * 显示应用选择对话框
     * 功能: 预留功能，暂时显示提示信息
     */
    private fun showAppSelectorDialog() {
        Toast.makeText(mContext, "应用选择功能开发中", Toast.LENGTH_SHORT).show()
        // 预留: 可在此添加应用选择逻辑
        // 示例: 选择不同的使用场景(语音助手、虚拟主播等)
    }

    /**
     * 从URL提取模型名称
     * 功能: 从完整URL中提取模型文件名
     */
    private fun extractModelName(url: String): String {
        val fileName = url.substringAfterLast("/").substringBeforeLast(".zip")
        return fileName.ifEmpty { "已选择模型" }
    }

    /**
     * 切换高级设置展开/收起状态
     * 功能: 显示或隐藏高级设置面板，点击直接展示内容
     */
    private fun toggleAdvancedSettings() {
        isAdvancedExpanded = !isAdvancedExpanded
        binding.layoutAdvanced.visibility = if (isAdvancedExpanded) View.VISIBLE else View.GONE
    }

    /**
     * 开始播放流程
     * 功能: 验证输入并启动下载检查流程
     */
    private fun play() {
        // mBaseConfigUrl已在setupDefaultValues中设置
        mModelUrl = binding.etAddress.text.toString()

        if (TextUtils.isEmpty(mBaseConfigUrl)) {
            Toast.makeText(mContext, R.string.base_config_cannot_be_empty, Toast.LENGTH_SHORT).show()
            return
        }
        if (TextUtils.isEmpty(mModelUrl)) {
            Toast.makeText(mContext, R.string.model_url_cannot_be_empty, Toast.LENGTH_SHORT).show()
            return
        }
        checkBaseConfig()
    }

    /**
     * 检查基础配置是否存在
     * 功能: 检查本地是否已有基础配置文件
     */
    private fun checkBaseConfig() {
        if (VirtualModelUtil.checkBaseConfig(mContext)) {
            checkModel()
        } else {
            baseConfigDownload()
        }
    }

    /**
     * 检查模型是否存在
     * 功能: 检查本地是否已有选择的模型文件
     */
    private fun checkModel() {
        if (VirtualModelUtil.checkModel(mContext, mModelUrl)) {
            jumpPlayPage()
        } else {
            modelDownload()
        }
    }

    /**
     * 跳转到播放界面
     * 功能: 启动本地数字人播放Activity
     */
    private fun jumpPlayPage() {
        val intent = Intent(mContext, LocalCallActivity::class.java)
        intent.putExtra("modelUrl", mModelUrl)
        val debug = binding.switchDebug.isChecked
        intent.putExtra("debug", debug)
        startActivity(intent)
    }

    /**
     * 下载基础配置文件
     * 功能: 从URL下载并解压基础配置
     */
    private fun baseConfigDownload() {
        mLoadingDialog?.dismiss()
        mLoadingDialog = LoadingDialog(mContext, "Start downloading")
        mLoadingDialog?.show()
        VirtualModelUtil.baseConfigDownload(mContext, mBaseConfigUrl, object :
            VirtualModelUtil.ModelDownloadCallback {
            override fun onDownloadProgress(url: String?, current: Long, total: Long) {
                val progress = (current * 100 / total).toInt()
                if (progress != mLastProgress) {
                    mLastProgress = progress
                    runOnUiThread {
                        if (mLoadingDialog?.isShowing == true) {
                            mLoadingDialog?.setContent("Config download(${progress}%)")
                        }
                    }
                }
            }

            override fun onUnzipProgress(url: String?, current: Long, total: Long) {
                val progress = (current * 100 / total).toInt()
                if (progress != mLastProgress) {
                    mLastProgress = progress
                    runOnUiThread {
                        if (mLoadingDialog?.isShowing == true) {
                            mLoadingDialog?.setContent("Config unzip(${progress}%)")
                        }
                    }
                }
            }

            override fun onDownloadComplete(url: String?, dir: File?) {
                runOnUiThread {
                    mLoadingDialog?.dismiss()
                    checkModel()
                }
            }

            override fun onDownloadFail(url: String?, code: Int, msg: String?) {
                runOnUiThread {
                    mLoadingDialog?.dismiss()
                    Toast.makeText(mContext, "BaseConfig download error: $msg", Toast.LENGTH_SHORT).show()
                }
            }

        })
    }

    /**
     * 下载模型文件
     * 功能: 从URL下载并解压数字人模型
     */
    private fun modelDownload() {
        mLoadingDialog?.dismiss()
        mLoadingDialog = LoadingDialog(mContext, "Start downloading")
        mLoadingDialog?.show()
        VirtualModelUtil.modelDownload(mContext, mModelUrl, object : VirtualModelUtil.ModelDownloadCallback {
            override fun onDownloadProgress(
                url: String?,
                current: Long,
                total: Long,
            ) {
                val progress = (current * 100 / total).toInt()
                if (progress != mLastProgress) {
                    mLastProgress = progress
                    runOnUiThread {
                        if (mLoadingDialog?.isShowing == true) {
                            mLoadingDialog?.setContent("Model download(${progress}%)")
                        }
                    }
                }
            }

            override fun onUnzipProgress(
                url: String?,
                current: Long,
                total: Long,
            ) {
                val progress = (current * 100 / total).toInt()
                if (progress != mLastProgress) {
                    mLastProgress = progress
                    runOnUiThread {
                        if (mLoadingDialog?.isShowing == true) {
                            mLoadingDialog?.setContent("Model unzip(${progress}%)")
                        }
                    }
                }
            }

            override fun onDownloadComplete(url: String?, dir: File?) {
                runOnUiThread {
                    mLoadingDialog?.dismiss()
                    jumpPlayPage()
                }
            }

            override fun onDownloadFail(
                url: String?,
                code: Int,
                msg: String?,
            ) {
                runOnUiThread {
                    mLoadingDialog?.dismiss()
                    Toast.makeText(mContext, "Model download error: $msg", Toast.LENGTH_SHORT).show()
                }
            }

        })
    }

}
