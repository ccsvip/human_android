package ai.guiji.duix.sdk.client;

import android.content.Context;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ai.guiji.duix.sdk.client.controller.DUIXAudioController;
import ai.guiji.duix.sdk.client.controller.DUIXInitializer;
import ai.guiji.duix.sdk.client.controller.DUIXMotionController;
import ai.guiji.duix.sdk.client.loader.ModelInfo;
import ai.guiji.duix.sdk.client.render.RenderSink;
import ai.guiji.duix.sdk.client.thread.RenderThread;

/**
 * DUIX - 数字人SDK主控类
 *
 * 架构设计：使用Facade模式，内部委托给专门的Controller处理不同职责
 * - DUIXInitializer: 初始化和模型检查
 * - DUIXAudioController: 音频控制
 * - DUIXMotionController: 动作控制
 *
 * 职责：
 * - 提供统一的API接口
 * - 协调各个Controller的工作
 * - 管理渲染线程的生命周期
 * - 转发事件回调
 */
public class DUIX {

    private final Context mContext;
    private final Callback mCallback;
    private final String modelName;
    private final RenderSink renderSink;
    private ExecutorService commonExecutor = Executors.newSingleThreadExecutor();
    private RenderThread mRenderThread;

    // 控制器（按功能解耦）
    private final DUIXInitializer initializer;
    private final DUIXAudioController audioController;
    private final DUIXMotionController motionController;

    private boolean isReady;            // 准备完成的标记
    private RenderThread.Reporter reporter;

    public DUIX(Context context, String modelName, RenderSink sink, Callback callback) {
        this.mContext = context;
        this.mCallback = callback;
        this.modelName = modelName;
        this.renderSink = sink;

        // 初始化各个控制器
        this.initializer = new DUIXInitializer(context, modelName);
        this.audioController = new DUIXAudioController();
        this.motionController = new DUIXMotionController();
    }

    /**
     * 初始化数字人模型
     *
     * 步骤：
     * 1. 检查基础配置（gj_dh_res）
     * 2. 检查数字人模型文件
     * 3. 创建渲染线程
     * 4. 启动Native层初始化
     *
     * 回调：
     * - CALLBACK_EVENT_INIT_READY: 初始化成功
     * - CALLBACK_EVENT_INIT_ERROR: 初始化失败
     */
    public void init() {
        // 1. 检查基础配置
        DUIXInitializer.CheckResult baseResult = initializer.checkBaseConfig();
        if (!baseResult.isSuccess()) {
            if (mCallback != null) {
                mCallback.onEvent(Constant.CALLBACK_EVENT_INIT_ERROR, baseResult.getErrorMessage(), null);
            }
            return;
        }

        // 2. 检查模型文件
        DUIXInitializer.CheckResult modelResult = initializer.checkModel();
        if (!modelResult.isSuccess()) {
            if (mCallback != null) {
                mCallback.onEvent(Constant.CALLBACK_EVENT_INIT_ERROR, modelResult.getErrorMessage(), null);
            }
            return;
        }

        // 3. 获取模型目录
        File modelDir = modelResult.getModelDir();
        if (modelDir == null) {
            if (mCallback != null) {
                mCallback.onEvent(Constant.CALLBACK_EVENT_INIT_ERROR, "Model directory is null", null);
            }
            return;
        }

        // 4. 停止旧的渲染线程（如果存在）
        if (mRenderThread != null) {
            mRenderThread.stopPreview();
            mRenderThread = null;
        }

        // 5. 创建新的渲染线程
        mRenderThread = new RenderThread(mContext, modelDir, renderSink, audioController.getVolume(), new RenderThread.RenderCallback() {

            @Override
            public void onInitResult(int code, int subCode, String message, ModelInfo modelInfo) {
                if (code == 0){
                    isReady = true;
                    if (mCallback != null){
                        mCallback.onEvent(Constant.CALLBACK_EVENT_INIT_READY, "init ok", modelInfo);
                    }
                } else {
                    if (mCallback != null){
                        mCallback.onEvent(Constant.CALLBACK_EVENT_INIT_ERROR, code + ", " + subCode + ", " + message, null);
                    }
                }
            }

            @Override
            public void onPlayStart() {
                if (mCallback != null){
                    mCallback.onEvent(Constant.CALLBACK_EVENT_AUDIO_PLAY_START, "play start", null);
                }
            }

            @Override
            public void onPlayEnd() {
                if (mCallback != null){
                    mCallback.onEvent(Constant.CALLBACK_EVENT_AUDIO_PLAY_END, "play end", null);
                }
            }

            @Override
            public void onPlayError(int code, String msg) {
                if (mCallback != null){
                    mCallback.onEvent(Constant.CALLBACK_EVENT_AUDIO_PLAY_ERROR, "audio play error code: " + code + " msg: " + msg, null);
                }
            }

            @Override
            public void onMotionPlayStart(String name) {
                if (mCallback != null){
                    mCallback.onEvent(Constant.CALLBACK_EVENT_MOTION_START, "", null);
                }
            }

            @Override
            public void onMotionPlayComplete(String name) {
                if (mCallback != null){
                    mCallback.onEvent(Constant.CALLBACK_EVENT_MOTION_END, "", null);
                }
            }
        }, reporter);

        // 6. 设置线程名称并启动
        mRenderThread.setName("DUIXRender-Thread");
        mRenderThread.start();

        // 7. 将渲染线程注入到控制器
        audioController.setRenderThread(mRenderThread);
        motionController.setRenderThread(mRenderThread);
    }

    public boolean isReady() {
        return isReady;
    }

    /**
     * 设置音量
     * @param volume 音量值（0.0 ~ 1.0）
     */
    public void setVolume(float volume){
        audioController.setVolume(volume);
    }

    /**
     * 开始推送PCM音频流
     */
    public void startPush(){
        audioController.startPush();
    }

    /**
     * 推送PCM音频数据
     * @param buffer PCM数据（16kHz, 16bit, Mono）
     */
    public void pushPcm(byte[] buffer){
        audioController.pushPcm(buffer);
    }

    /**
     * 停止推送PCM音频流
     */
    public void stopPush(){
        audioController.stopPush();
    }


    /**
     * 播放WAV音频文件
     * （兼容旧的WAV文件驱动方式）
     * @param wavPath 16k采样率单通道16位深的WAV本地文件路径
     */
    public void playAudio(String wavPath) {
        if (isReady) {
            audioController.playAudio(wavPath);
        }
    }

    /**
     * 停止音频播放
     */
    public boolean stopAudio() {
        return isReady && audioController.stopAudio();
    }


    /**
     * 播放指定动作
     * @param name 动作名称
     * @param now true表示立即播放，false表示在当前动作结束后播放
     */
    public void startMotion(String name, boolean now) {
        motionController.startMotion(name, now);
    }

    /**
     * 随机播放一个动作
     * @param now true表示立即播放，false表示在当前动作结束后播放
     */
    public void startRandomMotion(boolean now) {
        motionController.startRandomMotion(now);
    }

    /**
     * 释放资源
     */
    public void release() {
        isReady = false;
        if (commonExecutor != null) {
            commonExecutor.shutdown();
            commonExecutor = null;
        }
        if (mRenderThread != null) {
            mRenderThread.stopPreview();
        }
    }

    /**
     * 设置渲染统计报告器
     * @param reporter 报告器
     */
    public void setReporter(RenderThread.Reporter reporter){
        this.reporter = reporter;
        if (mRenderThread != null) {
            mRenderThread.setReporter(reporter);
        }
    }
}
