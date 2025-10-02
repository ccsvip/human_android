package ai.guiji.duix.test.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import ai.guiji.duix.sdk.i18n.LanguageManager;
import ai.guiji.duix.sdk.welcome.WelcomeConfig;

import java.util.ArrayList;
import java.util.List;


public abstract class BaseActivity extends AppCompatActivity implements Handler.Callback {

    public final String TAG = getClass().getName();
    protected BaseActivity mContext;
    protected Handler mHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        HandlerThread mHandlerThread = new HandlerThread(TAG);
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper(), this);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        // 应用语言设置
        LanguageManager.Language language = LanguageManager.INSTANCE.getCurrentLanguage(newBase);
        Context context = LanguageManager.INSTANCE.applyLanguage(newBase, language);
        super.attachBaseContext(context);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        if (mHandler != null && mHandler.getLooper() != null) {
            mHandler.getLooper().quit();
        }
    }

    @Override
    public boolean handleMessage(@NonNull Message msg) {
        onMessage(msg);
        return false;
    }

    // try abstract
    protected void onMessage(@NonNull Message msg) {

    }

    protected void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private String[] mRequestPermissions;
    private int mRequestPermissionCode;
    ActivityResultLauncher<String[]> permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                boolean hasDeny = false;
                for (String permission : mRequestPermissions) {
                    if (null == permission) {
                        continue;
                    }
                    if (ContextCompat.checkSelfPermission(mContext, permission) !=
                            PackageManager.PERMISSION_GRANTED) {
                        hasDeny = true;
                    }
                }
                if (hasDeny) {
                    permissionsGet(false, mRequestPermissionCode);
                } else {
                    permissionsGet(true, mRequestPermissionCode);
                }
            });

    //申请权限
    public void requestPermission(String[] permissions, int code) {
        if (null == permissions) {
            permissionsGet(true, code);
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            permissionsGet(true, code);
            return;
        }
        mRequestPermissions = permissions;
        mRequestPermissionCode = code;
        List<String> requestPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(mContext, permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissions.add(permission);
            }
        }
        if (0 != requestPermissions.size()) {
            String[] permissionArray = new String[requestPermissions.size()];
            for (int i = 0; i < requestPermissions.size(); i++) {
                permissionArray[i] = requestPermissions.get(i);
            }
            permissionLauncher.launch(permissionArray);
        } else {
            permissionsGet(true, mRequestPermissionCode);
        }
    }

    //申请权限回调
    public void permissionsGet(boolean get, int code) {

    }

    /**
     * 返回主界面（重新显示选择界面）
     *
     * 功能：
     * - 重置欢迎页面配置
     * - 启动MainActivity并清空任务栈
     * - 用户可以重新选择本地或云端数字人
     *
     * 使用场景：
     * - 本地数字人配置界面
     * - 本地数字人播放界面
     * - 云端数字人界面
     */
    protected void backToMainMenu() {
        // 重置欢迎页面配置，以便重新显示WelcomeActivity
        WelcomeConfig.INSTANCE.resetWelcomeConfig(this);

        // 返回MainActivity，它会重新显示WelcomeActivity
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }


}
