package com.soling.cameramanager;

import java.io.IOException;  
import java.util.List;  

import com.soling.camreamanager.util.LogUtil;
import android.hardware.Camera; 
import android.hardware.Camera.PreviewCallback;
import android.util.Log;  
import android.view.SurfaceHolder;  
  
public class CameraInterface {  
    private static final String TAG = "CameraInterface";  
    private Camera mCamera;  
    private Camera.Parameters mParams;  
    private boolean isPreviewing = false;  
    private float mPreviwRate = -1f;  
    private static CameraInterface mCameraInterface;  
  
    public interface CamOpenOverCallback{  
        public void cameraHasOpened();
        public void cameraOpenError(); 
    }  
  
    private CameraInterface(){  
  
    }  
    public static synchronized CameraInterface getInstance(){  
        if(mCameraInterface == null){  
            mCameraInterface = new CameraInterface();  
        }  
        return mCameraInterface;  
    }  
    
    public Camera getCamera(){
    	return mCamera;
    }
      
    /**打开Camera 
     * @param callback 
     */  
    public synchronized void doOpenCamera(CamOpenOverCallback callback,PreviewCallback preCallback){  
        Log.i(TAG, "Camera open....");  
        doStopCamera();
        try {
        	Log.i(TAG, "AVM try Camera open....");  
            mCamera = Camera.open();	           
        } catch (Exception e) {           
            callback.cameraOpenError();//------------camera打开失败回调
            Log.i(TAG, "AVM catch Camera open....");  	         
            return;
        }        
        Log.i(TAG, "Camera open over....");  
        if(mCamera != null){
            callback.cameraHasOpened(); //------------camera打开成功回调
        	mCamera.setPreviewCallback(preCallback);//---------camera预览数据回调
        }
    }  
    /**开启预览 
     * @param holder 
     * @param previewRate 
     */  
    public void doStartPreview(SurfaceHolder holder){  
        Log.i(TAG, "doStartPreview...");  
        if(isPreviewing){  
            mCamera.stopPreview();  
            return;  
        }  
        if(mCamera != null){  
            try {  
                mCamera.setPreviewDisplay(holder);  
                mCamera.startPreview();//开启预览  
            } catch (IOException e) {  
                // TODO Auto-generated catch block  
                e.printStackTrace();  
            }  
  
            isPreviewing = true;  
  
            mParams = mCamera.getParameters(); //重新get一次  
            Log.i(TAG, "最终设置:PreviewSize--With = " + mParams.getPreviewSize().width  
                    + "Height = " + mParams.getPreviewSize().height);  
            Log.i(TAG, "最终设置:PictureSize--With = " + mParams.getPictureSize().width  
                    + "Height = " + mParams.getPictureSize().height);  
        }  
    }  
    /** 
     * 停止预览，释放Camera 
     */  
    public synchronized void doStopCamera(){  
    	try {
    		if(null != mCamera)  
            {  
    			LogUtil.d(TAG, "doStopCamera start");
                mCamera.setPreviewCallback(null); //--------清空预览数据
                mCamera.stopPreview();  //--------停止预览
                isPreviewing = false;  
                mPreviwRate = -1f;  
                mCamera.release();  //--------释放camera资源，关闭Camera
                mCamera = null;
                LogUtil.d(TAG, "doStopCamera over");
            }  	           
        } catch (Exception e) {
            Log.e(TAG, "AVM doStopCamera error...."); 
        }         
    }  
    
    /** 
     * 停止预览，
     */  
    public synchronized void doStopPreview(){  
    	try {
    		if(null != mCamera)  
            {  
    			LogUtil.d(TAG, "doStopPreview");
                mCamera.setPreviewCallback(null); //--------清空预览数据
                mCamera.stopPreview();  //--------停止预览
                isPreviewing = false;  
                mPreviwRate = -1f;  
            }  	           
        } catch (Exception e) {
            Log.e(TAG, "AVM doStopPreview error...."); 
        }         
    }  
  
}  
