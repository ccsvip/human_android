package ai.guiji.duix.test;

import ai.guiji.duix.sdk.i18n.LanguageManager;
import android.app.Application;
import android.text.TextUtils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class App extends Application {

    public static App mApp;
    private static OkHttpClient mOkHttpClient;

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;

        // 应用语言设置
        LanguageManager.Language language = LanguageManager.INSTANCE.getCurrentLanguage(this);
        LanguageManager.INSTANCE.applyLanguage(this, language);
    }

    public static OkHttpClient getOkHttpClient() {
        if (mOkHttpClient == null) {
            mOkHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build();
        }
        return mOkHttpClient;
    }
}
