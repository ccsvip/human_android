package ai.guiji.duix.sdk.client.controller;

import ai.guiji.duix.sdk.client.thread.RenderThread;

/**
 * DUIX动作控制器
 *
 * 职责：
 * - 控制数字人播放指定动作（startMotion）
 * - 控制数字人播放随机动作（startRandomMotion）
 *
 * 单一职责原则：仅负责动作相关的控制逻辑
 * 依赖：RenderThread（通过接口隔离）
 */
public class DUIXMotionController {

    private RenderThread renderThread;

    /**
     * 设置渲染线程
     * @param renderThread 渲染线程实例
     */
    public void setRenderThread(RenderThread renderThread) {
        this.renderThread = renderThread;
    }

    /**
     * 播放指定动作
     *
     * @param name 动作名称（需要在模型配置文件中定义）
     * @param now true表示立即播放（中断当前动作），false表示在当前动作结束后播放
     */
    public void startMotion(String name, boolean now) {
        if (renderThread != null) {
            renderThread.requireMotion(name, now);
        }
    }

    /**
     * 播放随机动作
     *
     * 从模型定义的所有动作中随机选择一个进行播放
     *
     * @param now true表示立即播放（中断当前动作），false表示在当前动作结束后播放
     */
    public void startRandomMotion(boolean now) {
        if (renderThread != null) {
            renderThread.requireRandomMotion(now);
        }
    }
}
