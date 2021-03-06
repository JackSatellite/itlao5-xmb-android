package com.e7yoo.e7.app.light;

import android.content.Context;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;

import com.e7yoo.e7.E7App;
import com.e7yoo.e7.R;
import com.e7yoo.e7.util.TastyToastUtil;

public class CameraManager {

    private static Camera sCamera;
    static {
        if (E7App.mApp != null && hasFlash(E7App.mApp)) {
            try {
                sCamera = Camera.open();
            } catch (Exception e) {
                try {
                    sCamera = Camera.open(Camera.getNumberOfCameras() - 1);
                } catch (Exception e1) {
                    TastyToastUtil.toast(E7App.mApp, R.string.flashlight_open_error);
                }
            }
        } else if (E7App.mApp != null) {
            TastyToastUtil.toast(E7App.mApp, R.string.flashlight_no);
        }
    }

    public static Camera getCamera(Context context) {
        if (!hasFlash(context)) return null;
        if (sCamera == null) {
            try {
                sCamera = Camera.open();
            } catch (Exception e) {
                try {
                    sCamera = Camera.open(Camera.getNumberOfCameras() - 1);
                } catch (Exception e1) {
                    TastyToastUtil.toast(E7App.mApp, R.string.flashlight_open_error);
                }
            }
        }
        return sCamera;
    }

    private static void ensureCamera(Context context) {
        getCamera(context);
    }

    public static void openFlash(Context context) {
        ensureCamera(context);
        if (sCamera == null) return;
        try {
            Camera.Parameters parameters = sCamera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            sCamera.setParameters(parameters);
            sCamera.cancelAutoFocus();
            sCamera.startPreview();
        } catch (Exception e) {
        }
    }

    public static void closeFlash() {
        if (sCamera == null) return;
        try {
            Camera.Parameters parameters = sCamera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            sCamera.setParameters(parameters);
        } catch (Exception e) {
        }
    }

    public static void release() {
        if (sCamera != null) {
            sCamera.stopPreview();
            sCamera.release();
            sCamera = null;
        }
    }

    public static boolean hasFlash(Context context) {
        PackageManager pm = context.getPackageManager();
        FeatureInfo[] featureInfos = pm.getSystemAvailableFeatures();
        for (FeatureInfo f : featureInfos) {
            if (PackageManager.FEATURE_CAMERA_FLASH.equals(f.name)) {
                return true;
            }
        }
        return false;
    }

}
