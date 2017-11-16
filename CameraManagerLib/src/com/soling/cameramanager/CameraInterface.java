package com.soling.cameramanager;

import java.io.IOException; 

import com.soling.camreamanager.util.LogUtil;

import android.graphics.ImageFormat;
import android.hardware.Camera; 
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
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
    
    public void addCallbackBuffer(byte[] data){
    	if(mCamera != null){
    		mCamera.addCallbackBuffer(data);    	
    	}
    }  
      
    /**打开Camera 
     * @param callback 
     */  
    public synchronized void doOpenCamera(CamOpenOverCallback callback,PreviewCallback preCallback){  
    	LogUtil.i(TAG, "Camera open....");  
        doStopCamera();
        try {
        	LogUtil.i(TAG, "AVM try Camera open....");  
            mCamera = Camera.open();	           
        } catch (Exception e) {           
            callback.cameraOpenError();//------------camera打开失败回调
            LogUtil.i(TAG, "AVM catch Camera open....");  	         
            return;
        }    
        
        
        LogUtil.i(TAG, "Camera open over....");  
        if(mCamera != null){
            callback.cameraHasOpened(); //------------camera打开成功回调
        	//mCamera.setPreviewCallback(preCallback);//---------camera预览数据回调
            
            //更换回调方式，解决gc回收问题
            mCamera.setPreviewCallbackWithBuffer(preCallback);
            mParams = mCamera.getParameters();  
            Size previewSize = com.soling.camreamanager.util.CamParaUtil.getInstance().getPropPreviewSize(  
                    mParams.getSupportedPreviewSizes(), 1, 800);   
            Log.i(TAG, "PreviewSize--With = " + previewSize.width + "Height = " + previewSize.height); 
            
            mCamera.addCallbackBuffer(new byte[((previewSize.width * previewSize.height) * ImageFormat.getBitsPerPixel(ImageFormat.NV21)) / 8]);

        }else{           
        	 callback.cameraOpenError();//------------camera打开失败回调
             LogUtil.i(TAG, "AVM  Camera open.is null.");  
        }
    }  
    /**开启预览 
     * @param holder 
     * @param previewRate 
     */  
    public void doStartPreview(SurfaceHolder holder){  
    	LogUtil.i(TAG, "doStartPreview...");  
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
    			try {  
				 	mCamera.setPreviewCallback(null); //--------清空预览数据
	                mCamera.stopPreview();  //--------停止预览
	                isPreviewing = false;  
	                mPreviwRate = -1f;  
	                mCamera.release();  //--------释放camera资源，关闭Camera 
	            } catch (Exception e) {  
	                // TODO Auto-generated catch block  
	                e.printStackTrace();  
	            }  
                
                mCamera = null;
                LogUtil.d(TAG, "doStopCamera over");
            }  	           
        } catch (Exception e) {
        	LogUtil.e(TAG, "AVM doStopCamera error...."); 
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
                mCamera.addCallbackBuffer(null);
                mCamera.stopPreview();  //--------停止预览
                isPreviewing = false;  
                mPreviwRate = -1f;  
            }  	           
        } catch (Exception e) {
            Log.e(TAG, "AVM doStopPreview error...."); 
        }         
    }  
  
}  
