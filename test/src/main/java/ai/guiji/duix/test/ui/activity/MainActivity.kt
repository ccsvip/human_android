package ai.guiji.duix.test.ui.activity

import ai.guiji.duix.sdk.client.BuildConfig
import ai.guiji.duix.sdk.client.VirtualModelUtil
import ai.guiji.duix.sdk.welcome.WelcomeActivity
import ai.guiji.duix.sdk.welcome.WelcomeConfig
import ai.guiji.duix.test.R
import ai.guiji.duix.test.databinding.ActivityMainBinding
import ai.guiji.duix.test.ui.dialog.LoadingDialog
import ai.guiji.duix.test.ui.dialog.ModelSelectorDialog
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File


class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private var mLoadingDialog: LoadingDialog?=null
    private var mLastProgress = 0

    /**
     * Activity Result API替代startActivityForResult
     */
    private val welcomeActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            WelcomeActivity.RESULT_LOCAL_CHOICE -> {
                Toast.makeText(this, "您选择了本地数字人模式", Toast.LENGTH_LONG).show()
                // TODO: 可以在这里预加载本地资源或设置相关配置
            }
            WelcomeActivity.RESULT_CLOUD_CHOICE -> {
                Toast.makeText(this, "您选择了云端数字人模式", Toast.LENGTH_LONG).show()
                // TODO: 可以在这里检查网络连接或设置云端配置
            }
            else -> {
                // 用户取消选择
                Toast.makeText(this, "已取消选择", Toast.LENGTH_SHORT).show()
            }
        }
    }

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

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvSdkVersion.text = "SDK Version: ${BuildConfig.VERSION_NAME}"

        // 检查是否需要显示欢迎页面
        checkAndShowWelcomePage()

        setupUI()
    }

    /**
     * 检查并显示欢迎页面
     */
    private fun checkAndShowWelcomePage() {
        if (WelcomeConfig.shouldShowWelcomePage(this)) {
            // 启动欢迎页面
            val intent = Intent(this, WelcomeActivity::class.java)
            welcomeActivityLauncher.launch(intent)
        } else {
            // 用户已经做过选择，根据选择显示相应提示
            showUserChoiceInfo()
        }
    }

    /**
     * 显示用户选择信息
     */
    private fun showUserChoiceInfo() {
        val userChoice = WelcomeConfig.getUserChoiceEnum(this)
        when (userChoice) {
            WelcomeConfig.UserChoice.LOCAL -> {
                Toast.makeText(this, "当前模式：本地数字人", Toast.LENGTH_SHORT).show()
            }
            WelcomeConfig.UserChoice.CLOUD -> {
                Toast.makeText(this, "当前模式：云端数字人", Toast.LENGTH_SHORT).show()
            }
            else -> {
                // 理论上不应该到这里
                Toast.makeText(this, "未检测到选择，将重新显示欢迎页面", Toast.LENGTH_SHORT).show()
                checkAndShowWelcomePage()
            }
        }
    }

    /**
     * 设置UI组件
     */
    private fun setupUI() {
        binding.btnMoreModel.setOnClickListener {
            val modelSelectorDialog = ModelSelectorDialog(mContext, models, object : ModelSelectorDialog.Listener{
                override fun onSelect(url: String) {
                    binding.etUrl.setText(url)
                }
            })
            modelSelectorDialog.show()
        }
        binding.btnPlay.setOnClickListener {
            play()
        }
    }

    private fun play(){
        mBaseConfigUrl = binding.etBaseConfig.text.toString()
        mModelUrl = binding.etUrl.text.toString()
        if (TextUtils.isEmpty(mBaseConfigUrl)){
            Toast.makeText(mContext, R.string.base_config_cannot_be_empty, Toast.LENGTH_SHORT).show()
            return
        }
        if (TextUtils.isEmpty(mModelUrl)){
            Toast.makeText(mContext, R.string.model_url_cannot_be_empty, Toast.LENGTH_SHORT).show()
            return
        }
        checkBaseConfig()
    }

    private fun checkBaseConfig(){
        if (VirtualModelUtil.checkBaseConfig(mContext)){
            checkModel()
        } else {
            baseConfigDownload()
        }
    }

    private fun checkModel(){
        if (VirtualModelUtil.checkModel(mContext, mModelUrl)){
            jumpPlayPage()
        } else {
            modelDownload()
        }
    }

    private fun jumpPlayPage(){
        val intent = Intent(mContext, CallActivity::class.java)
        intent.putExtra("modelUrl", mModelUrl)
        val debug = binding.switchDebug.isChecked
        intent.putExtra("debug", debug)
        startActivity(intent)
    }

    private fun baseConfigDownload(){
        mLoadingDialog?.dismiss()
        mLoadingDialog = LoadingDialog(mContext, "Start downloading")
        mLoadingDialog?.show()
        VirtualModelUtil.baseConfigDownload(mContext, mBaseConfigUrl, object :
            VirtualModelUtil.ModelDownloadCallback {
            override fun onDownloadProgress(url: String?, current: Long, total: Long) {
                val progress = (current * 100 / total).toInt()
                if (progress != mLastProgress){
                    mLastProgress = progress
                    runOnUiThread {
                        if (mLoadingDialog?.isShowing == true){
                            mLoadingDialog?.setContent("Config download(${progress}%)")
                        }
                    }
                }
            }

            override fun onUnzipProgress(url: String?, current: Long, total: Long) {
                val progress = (current * 100 / total).toInt()
                if (progress != mLastProgress){
                    mLastProgress = progress
                    runOnUiThread {
                        if (mLoadingDialog?.isShowing == true){
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

    private fun modelDownload(){
        mLoadingDialog?.dismiss()
        mLoadingDialog = LoadingDialog(mContext, "Start downloading")
        mLoadingDialog?.show()
        VirtualModelUtil.modelDownload(mContext, mModelUrl, object : VirtualModelUtil.ModelDownloadCallback{
            override fun onDownloadProgress(
                url: String?,
                current: Long,
                total: Long,
            ) {
                val progress = (current * 100 / total).toInt()
                if (progress != mLastProgress){
                    mLastProgress = progress
                    runOnUiThread {
                        if (mLoadingDialog?.isShowing == true){
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
                if (progress != mLastProgress){
                    mLastProgress = progress
                    runOnUiThread {
                        if (mLoadingDialog?.isShowing == true){
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
