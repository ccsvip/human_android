package ai.guiji.duix.sdk.client.controller;

import android.content.Context;

import java.io.File;

/**
 * DUIX初始化器
 *
 * 职责：
 * - 检查模型文件是否存在
 * - 验证基础配置（gj_dh_res）
 * - 验证数字人模型文件
 * - 提供模型目录路径
 *
 * 单一职责原则：仅负责初始化前的准备和检查逻辑
 * 依赖：Android Context（仅用于获取外部文件目录）
 */
public class DUIXInitializer {

    private final Context context;
    private final String modelName;

    public DUIXInitializer(Context context, String modelName) {
        this.context = context;
        this.modelName = modelName;
    }

    /**
     * 检查并获取DUIX目录
     * @return DUIX根目录，失败返回null
     */
    public File getDuixDir() {
        return context.getExternalFilesDir("duix");
    }

    /**
     * 检查基础配置是否存在
     * @return CheckResult包含检查结果和错误信息
     */
    public CheckResult checkBaseConfig() {
        File duixDir = getDuixDir();
        if (duixDir == null) {
            return CheckResult.error("Failed to get duix directory");
        }

        File baseConfigDir = new File(duixDir, "model/gj_dh_res");
        File baseConfigTag = new File(duixDir, "model/tmp/gj_dh_res");

        if (!baseConfigDir.exists()) {
            return CheckResult.error("[gj_dh_res] directory does not exist");
        }

        if (!baseConfigTag.exists()) {
            return CheckResult.error("[gj_dh_res] tag file does not exist");
        }

        return CheckResult.success();
    }

    /**
     * 检查模型文件是否存在
     * @return CheckResult包含检查结果、错误信息和模型目录
     */
    public CheckResult checkModel() {
        File duixDir = getDuixDir();
        if (duixDir == null) {
            return CheckResult.error("Failed to get duix directory");
        }

        String dirName = extractModelDirName(modelName);
        if (dirName == null || dirName.isEmpty()) {
            return CheckResult.error("Invalid model name: " + modelName);
        }

        File modelDir = new File(duixDir, "model/" + dirName);
        File modelTag = new File(duixDir, "model/tmp/" + dirName);

        if (!modelDir.exists()) {
            return CheckResult.error("[" + dirName + "] directory does not exist");
        }

        if (!modelTag.exists()) {
            return CheckResult.error("[" + dirName + "] tag file does not exist");
        }

        return CheckResult.success(modelDir);
    }

    /**
     * 从模型名称或URL中提取目录名称
     * @param modelName 模型名称或URL
     * @return 目录名称
     */
    private String extractModelDirName(String modelName) {
        if (modelName == null || modelName.isEmpty()) {
            return null;
        }

        try {
            if (modelName.startsWith("https://") || modelName.startsWith("http://")) {
                // 从URL中提取文件名，去掉.zip后缀
                String fileName = modelName.substring(modelName.lastIndexOf("/") + 1);
                return fileName.replace(".zip", "");
            } else {
                // 直接使用名称
                return modelName;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 检查结果类
     */
    public static class CheckResult {
        public final boolean success;
        public final String errorMessage;
        public final File modelDir;

        private CheckResult(boolean success, String errorMessage, File modelDir) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.modelDir = modelDir;
        }

        public static CheckResult success() {
            return new CheckResult(true, null, null);
        }

        public static CheckResult success(File modelDir) {
            return new CheckResult(true, null, modelDir);
        }

        public static CheckResult error(String message) {
            return new CheckResult(false, message, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public File getModelDir() {
            return modelDir;
        }
    }
}
