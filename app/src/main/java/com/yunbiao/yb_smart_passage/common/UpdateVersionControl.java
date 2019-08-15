package com.yunbiao.yb_smart_passage.common;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.yunbiao.yb_smart_passage.APP;


/**
 * Created by Administrator on 2015/12/8.
 */
public class UpdateVersionControl {

    private static UpdateVersionControl updateVersionControl;

    public static UpdateVersionControl getInstance() {
        if (updateVersionControl == null) {
            updateVersionControl = new UpdateVersionControl();
        }
        return updateVersionControl;
    }

    private UpdateVersionControl() {
    }

    //推送更新
    public void checkUpdate(Activity activity) {
        UpdateManager updateManager = new UpdateManager(activity);
        updateManager.checkUpdateInfo(getVersionName());
    }

    /**
     * 获取当前版本号
     *
     * @return
     */
    private String getVersionName() {
        String version = "";
        try {
            // 获取packagemanager的实例
            PackageManager packageManager = APP.getContext().getPackageManager();
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = packageManager.getPackageInfo(APP.getContext().getPackageName
                    (), 0);
            version = packInfo.versionName;
        } catch (Exception ignored) {

        }
        return version;
    }
}
