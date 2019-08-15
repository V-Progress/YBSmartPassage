package com.yunbiao.yb_smart_passage.faceview.camera;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import com.jdjr.risk.face.local.frame.FaceFrameManager;
import com.yunbiao.yb_smart_passage.faceview.rect.FrameHelper;

import java.util.Arrays;
import java.util.List;

public class ExtCameraManager {
    private static final String TAG = "ExtCameraManager";
    private Camera mRGBCamera;
    private Camera mNIRCamera;

    private static ExtCameraManager surfaceCameraManager = new ExtCameraManager();
    private List<Camera.Size> supportedPreviewSizes;

    public static ExtCameraManager instance(){
        return surfaceCameraManager;
    }

    public void init(TextureView rgbTexture, TextureView nirTexture){
        rgbTexture.setSurfaceTextureListener(rgbListener);
        nirTexture.setSurfaceTextureListener(nirListener);
    }

    public void init(TextureView rgbTexture){
        rgbTexture.setSurfaceTextureListener(rgbListener);
    }

    private TextureView.SurfaceTextureListener rgbListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            mRGBCamera = doOpenCamera(CameraType.getRGB(),CameraSettings.getCameraPreviewWidth(),CameraSettings.getCameraPreviewHeight(),surface,mRGBCallback);
        }
        @Override public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }
        @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            releaseRGBCamera();
            return false;
        }
        @Override public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };
    private TextureView.SurfaceTextureListener nirListener = new TextureView.SurfaceTextureListener() {
        @Override public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            mNIRCamera = doOpenCamera(CameraType.getNIR(),CameraSettings.getCameraPreviewWidth(),CameraSettings.getCameraPreviewHeight(),surface,mNIRCallback);
        }
        @Override public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) { }
        @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            releaseNIRCamera();
            return false;
        }
        @Override public void onSurfaceTextureUpdated(SurfaceTexture surface) { }
    };

    private Camera doOpenCamera(int cameraType, int cameraPreviewWidth, int cameraPreviewHeight, SurfaceTexture surfaceTexture , Camera.PreviewCallback previewCallback){
        try{
            Camera camera = Camera.open(cameraType);
            camera.setDisplayOrientation(CameraSettings.getCameraDisplayRotation());
            Camera.Parameters parameters = camera.getParameters();
            parameters.setPreviewSize(cameraPreviewWidth, cameraPreviewHeight);
            camera.setParameters(parameters);
            for (int i = 0; i < 3; i++) {
                int length = cameraPreviewWidth * cameraPreviewHeight * 3 / 2;
                camera.addCallbackBuffer(new byte[length]);
            }
            camera.setPreviewCallbackWithBuffer(previewCallback);
            camera.setPreviewTexture(surfaceTexture);
            camera.startPreview();
            return camera;
        }catch (Exception e){
            e.printStackTrace();
            Log.d("FaceLocalSystemRGBNIR", "########## doCameraPreview RGB exception");
        }
        return null;
    }

    /***
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     */
    public void init(SurfaceView rgbSurface, SurfaceView nirSurface){
        rgbSurface.getHolder().addCallback(rgbCallback);
        nirSurface.getHolder().addCallback(nirCallback);
    }

    public void init(SurfaceView rgbSurface){
        rgbSurface.getHolder().addCallback(rgbCallback);
    }

    private SurfaceHolder.Callback rgbCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if(mListener != null){
                mListener.onSurfaceReady();
            }
            releaseRGBCamera();
            mRGBCamera = doOpenCamera(CameraType.getRGB(),CameraSettings.getCameraPreviewWidth(),CameraSettings.getCameraPreviewHeight(),holder,mRGBCallback);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            releaseRGBCamera();
        }
    };

    private SurfaceHolder.Callback nirCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            releaseNIRCamera();
            mNIRCamera = doOpenCamera(CameraType.getNIR(),CameraSettings.getCameraPreviewWidth(),CameraSettings.getCameraPreviewHeight(),holder,mNIRCallback);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            releaseNIRCamera();
        }
    };

    private Camera doOpenCamera(int cameraType, int cameraPreviewWidth, int cameraPreviewHeight, SurfaceHolder holder , Camera.PreviewCallback previewCallback){
        try{
            Camera camera = Camera.open(cameraType);
            camera.setDisplayOrientation(CameraSettings.getCameraDisplayRotation());
            Camera.Parameters parameters = camera.getParameters();
            supportedPreviewSizes = parameters.getSupportedPreviewSizes();
            for (Camera.Size supportedPreviewSize : supportedPreviewSizes) {
                Log.e(TAG, "doOpenCamera: " + supportedPreviewSize.width + " * " + supportedPreviewSize.height);
            }

            parameters.setPreviewSize(cameraPreviewWidth, cameraPreviewHeight);
            camera.setParameters(parameters);
            for (int i = 0; i < 3; i++) {
                int length = cameraPreviewWidth * cameraPreviewHeight * 3 / 2;
                camera.addCallbackBuffer(new byte[length]);
            }
            camera.setPreviewCallbackWithBuffer(previewCallback);
            camera.setPreviewDisplay(holder);
            camera.startPreview();
            return camera;
        }catch (Exception e){
            e.printStackTrace();
            Log.d("FaceLocalSystemRGBNIR", "########## doCameraPreview RGB exception");
        }
        return null;
    }

    /***
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     * ========================================================================================================================================================
     */
    private Camera.PreviewCallback mRGBCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(final byte[] data, final Camera camera) {
            final byte[] frameCopy = Arrays.copyOf(data, data.length);

            if (camera != null) {
                camera.addCallbackBuffer(data);
            }
            final byte[] frameRotateRGB = FrameHelper.getFrameRotate(frameCopy, CameraSettings.getCameraPreviewWidth(), CameraSettings.getCameraPreviewHeight());
            FaceFrameManager.handleCameraFrame(frameRotateRGB, mLastFrameNIR, CameraSettings.getCameraWidth(), CameraSettings.getCameraHeight());
        }
    };

    private byte[] mLastFrameNIR;

    private Camera.PreviewCallback mNIRCallback = new Camera.PreviewCallback() {

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            final byte[] copy = Arrays.copyOf(data, data.length);
            if (camera != null) {
                camera.addCallbackBuffer(data);
            }
            final byte[] frameRotate = FrameHelper.getFrameRotate(copy, CameraSettings.getCameraWidth(), CameraSettings.getCameraHeight());
            mLastFrameNIR = frameRotate;
        }
    };

    public List<Camera.Size> getSupportSizeList(){
        return supportedPreviewSizes;
    }

    public void releaseRGBCamera(){
        try {
            if (mRGBCamera != null) {
                mRGBCamera.setPreviewCallback(null);
                mRGBCamera.stopPreview();
                mRGBCamera.release();
                mRGBCamera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void releaseNIRCamera(){
        try {
            if (mNIRCamera != null) {
                mNIRCamera.setPreviewCallback(null);
                mNIRCamera.stopPreview();
                mNIRCamera.release();
                mNIRCamera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void releaseAllCamera(){
        releaseRGBCamera();
        releaseNIRCamera();
    }

    public void setViewReadyListener(ViewReadyListener listener) {

        mListener = listener;
    }

    private ViewReadyListener mListener;

    public interface ViewReadyListener{
        void onSurfaceReady();
    }

}
