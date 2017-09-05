package com.soling.cameramanagersdk.server;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.soling.cameramanager.ILibCallBack;
import com.soling.cameramanager.IServiceAIDL;
import com.soling.camreamanager.util.LogUtil;

public class CameraManagerService extends Service
{
    public static final String TAG = "CameraManagerService";
    private RemoteCallbackList<ILibCallBack> mLibCallbacks = new RemoteCallbackList<ILibCallBack>();
    
    int[] mCameraState = {0, 0, 0, 0, 0};
    int mReqStartVedioId = -1;
    
    /**
     * 绑定服务
     */
    @Override
    public IBinder onBind(Intent intent)
    {
        // TODO Auto-generated method stub
        return mBinder;
    }
    
    /**
     * 创建服务
     */
    @Override
    public void onCreate()
    {
        super.onCreate();
        LogUtil.v(TAG, "onCreate  " );
    }

    /**
     * 关闭服务
     */
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        LogUtil.v(TAG, "onDestroy  " );
    }

    /**
     * 启动服务
     */
    @Override
    public void onStart(Intent intent, int startId)
    {
        super.onStart(intent, startId);
        LogUtil.v(TAG, "onStart  " );
    }

    /**
     * 解绑服务
     */
    @Override
    public boolean onUnbind(Intent intent)
    {
        LogUtil.v(TAG, "onUnbind  " );
        return super.onUnbind(intent);
    }
    
    
    IServiceAIDL.Stub mBinder = new IServiceAIDL.Stub()
    {

		@Override
		public void registerLibCallback(ILibCallBack callback)
				throws RemoteException {
			if (null != callback) {
				mLibCallbacks.register(callback);
			}
	    	LogUtil.v(TAG, "registerLibCallback ac : " + ((callback == null) ? null : callback.getClass().getName()));
		}

		@Override
		public void unregisterLibCallback(ILibCallBack callback)
				throws RemoteException {
			if (null != callback) {
				mLibCallbacks.register(callback);
			}
	    	LogUtil.v(TAG, "registerLibCallback ac : " + ((callback == null) ? null : callback.getClass().getName()));

		}

		@Override
		public void reqStartCamera(int vedioId) throws RemoteException {
			LogUtil.v(TAG, "reqStartCamera vedioId = " + vedioId );
			
			if(isCameraCanUse()){
				if(vedioId < 5 && vedioId >= 0){
					LogUtil.v(TAG, "reqStartCamera error check vedioId = " + vedioId );
					startCamera(vedioId);
					return;
				}
			}
			
			if(vedioId < 5 && vedioId >= 0){
				mReqStartVedioId = vedioId;
				for(int i = 0;i < 5;i++){
		    		if(mCameraState[i] == 1){
		    			stopCamera(i);
		    		}
		    	}				
			}
			
		}

		//关闭vedio节点Camera完成
		@Override
		public void setStopCameraOver(int vedioId) throws RemoteException {
			LogUtil.v(TAG, "setStopCameraOver vedioId = " + vedioId + " mReqStartVedioId = " + mReqStartVedioId );
			if(isCameraCanUse()){
				if(mReqStartVedioId < 5 && mReqStartVedioId >= 0){
					startCamera(mReqStartVedioId);
					mReqStartVedioId = -1;
				}
			}
			
		}

		@Override
		public boolean getIsCameraCanUse() throws RemoteException {
			return isCameraCanUse();
		}

		@Override 
		public void setCameraState(int vedioId, int state) throws RemoteException {
			LogUtil.v(TAG, "setCameraState vedioId = " + vedioId + " state = " + state);
			if(vedioId < 5 && vedioId >= 0)
				mCameraState[vedioId] = state;
		}
       
    };
    
    private boolean isCameraCanUse(){
    	for(int i = 0;i < 5;i++){
    		if(mCameraState[i] == 1){
    			LogUtil.v(TAG, "isCameraCanUse userid = " + i );
    			return false;
    		}
    	}
    	return true;
    }  
	
	public synchronized void startCamera(int vedioId) {
        final int N = mLibCallbacks.beginBroadcast();  
    	LogUtil.v(TAG, "startCamera vedioId =: " + vedioId );
        for (int i=0; i<N; i++) {   
            try {            	
            	mLibCallbacks.getBroadcastItem(i).startCamera(vedioId);   
            }  
            catch (RemoteException e) {   
                  
            }  
        }  
        mLibCallbacks.finishBroadcast();  
	}
	
	public synchronized void stopCamera(int vedioId) {
        final int N = mLibCallbacks.beginBroadcast();  
    	LogUtil.v(TAG, "stopCamera vedioId =: " + vedioId );
        for (int i=0; i<N; i++) {   
            try {            	
            	mLibCallbacks.getBroadcastItem(i).stopCamera(vedioId);   
            }  
            catch (RemoteException e) {   
                  
            }  
        }  
        mLibCallbacks.finishBroadcast();  
	}
    
}
