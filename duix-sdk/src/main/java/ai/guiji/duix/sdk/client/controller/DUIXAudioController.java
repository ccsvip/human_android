package ai.guiji.duix.sdk.client.controller;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

import ai.guiji.duix.sdk.client.thread.RenderThread;

/**
 * DUIX音频控制器
 *
 * 职责：
 * - 管理PCM音频流的推送（startPush, pushPcm, stopPush）
 * - 管理WAV文件的播放（playAudio）
 * - 控制音频播放的停止（stopAudio）
 * - 控制音量（setVolume）
 *
 * 单一职责原则：仅负责音频相关的控制逻辑
 * 依赖：RenderThread（通过接口隔离）
 */
public class DUIXAudioController {

    private RenderThread renderThread;
    private float volume = 1.0F;

    /**
     * 设置渲染线程
     * @param renderThread 渲染线程实例
     */
    public void setRenderThread(RenderThread renderThread) {
        this.renderThread = renderThread;
        if (renderThread != null && volume != 1.0F) {
            renderThread.setVolume(volume);
        }
    }

    /**
     * 设置音量
     * @param volume 音量值（0.0 ~ 1.0）
     */
    public void setVolume(float volume) {
        if (volume >= 0.0F && volume <= 1.0F) {
            this.volume = volume;
            if (renderThread != null) {
                renderThread.setVolume(volume);
            }
        }
    }

    /**
     * 开始推送PCM音频流
     * 调用此方法后，可以通过pushPcm推送音频数据
     */
    public void startPush() {
        if (renderThread != null) {
            renderThread.startPush();
        }
    }

    /**
     * 推送PCM音频数据
     * @param buffer PCM音频数据（16kHz, 16bit, Mono）
     */
    public void pushPcm(byte[] buffer) {
        if (renderThread != null) {
            renderThread.pushAudio(buffer.clone());
        }
    }

    /**
     * 停止推送PCM音频流
     */
    public void stopPush() {
        if (renderThread != null) {
            renderThread.stopPush();
        }
    }

    /**
     * 播放WAV音频文件
     *
     * 注意：
     * - WAV文件必须是16kHz采样率、单通道、16bit位深
     * - 方法会自动跳过44字节的WAV文件头
     * - 音频数据会转换为PCM格式驱动数字人口型
     *
     * @param wavPath WAV文件的绝对路径
     * @return true表示播放请求已发送，false表示文件不存在或参数错误
     */
    public boolean playAudio(String wavPath) {
        File wavFile = new File(wavPath);
        if (renderThread == null || !wavFile.exists() || wavFile.length() <= 44) {
            return false;
        }

        try {
            // 读取WAV文件（默认WAV头是44bytes，采样率16000Hz、单通道、16bit）
            byte[] data = new byte[(int) wavFile.length()];
            try (FileInputStream inputStream = new FileInputStream(wavFile)) {
                inputStream.read(data);
            }

            // 跳过44字节的WAV头，提取PCM数据
            byte[] pcmData = Arrays.copyOfRange(data, 44, data.length);

            // 推送PCM数据
            startPush();
            pushPcm(pcmData);
            stopPush();

            return true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to play audio file: " + wavPath, e);
        }
    }

    /**
     * 停止音频播放
     * @return true表示停止请求已发送，false表示未就绪
     */
    public boolean stopAudio() {
        if (renderThread != null) {
            renderThread.stopPlayAudio();
            return true;
        }
        return false;
    }

    /**
     * 获取当前音量
     * @return 当前音量值（0.0 ~ 1.0）
     */
    public float getVolume() {
        return volume;
    }
}
