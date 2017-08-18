package com.soling.cameramanager;

import java.util.ArrayList;

import com.soling.cameramanager.CameraInterface.CamOpenOverCallback;
import com.soling.camreamanager.imp.CameraListenerImp;
import com.soling.camreamanager.util.LogUtil;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.view.SurfaceHolder;
/**
 * @author:soling
 */
public class CameraManager implements CamOpenOverCallback,PreviewCallback{
	private final String TAG = CameraManager.class.getSimpleName();
	
	private final String ServerPackageName = "com.soling.cameramanagersdk";
	private final String ServerClassName = "com.soling.cameramanagersdk.server.CameraManagerService";
	private static final int CAMERA_HANDER_MSG_START = 1;
	private static final int CAMERA_HANDER_MSG_STOP = 2;
	
	private ArrayList<CameraOpenStateListener> mCameraObservers = new ArrayList<CameraOpenStateListener>();
	private LibCallbackStub mLibCallback;	
	private IServiceAIDL mServiceStub;
	private static CameraManager mInstance;
	private Context mContext;
	private CameraHandler mCameraHandler;	
	private CameraInterface mCameraInterface;
	private int myVedioId = 0;
	private boolean mIsCVBSIn = false;
	private boolean mIsCameraOpen = false;
	private static long mOpenCameraOkMillis = 0;
	
	//-------------------------------提供的camera 打开状态---------------------------------------
	/**
	 * listener interface, apps need fix model.
	  * @author:soling
	 */
	public interface CameraOpenStateListener {	
		
		/**
		 * camera open ok 
		 * vedioid  
		 */
		void onCameraHasOpened();
		
		/**
		 * camera open error 
		 * vedioid  
		 */
		void onCameraOpenError();
		
		/**
		 * camera cvbs changer
		 * vedioid  
		 */
		void onCameraCVBSChange(boolean isCvbs);
		
	}	
	
	public CameraManager(Context mContext) {
		this.mContext = mContext;
		getNewStub();
		HandlerThread thread = new HandlerThread("camera");
		thread.start();
		mCameraHandler = new CameraHandler(thread.getLooper());
		mCameraInterface = CameraInterface.getInstance();
	}

	public static CameraManager getInstance(Context mContext) {
		if (null == mInstance) {
			mInstance =  new CameraManager(mContext);
		}		
		return mInstance;
	}	
	
	private IServiceAIDL getStub() {
		if (null == mServiceStub) {
			getNewStub();
		}		
		
		if(mServiceStub != null)
			return mServiceStub;
		else{
			return null;
		}
	}

	private void getNewStub() {
		LogUtil.v(TAG, "getNewStub() invoked ");
	
		if(mContext!=null && mServiceStub == null){
			Intent intent = new Intent();
			intent.setClassName(ServerPackageName, ServerClassName);
			mContext.bindService(intent,new ServiceConnection() {
				
				@Override
				public void onServiceDisconnected(ComponentName name) {				
					mServiceStub = null;
					if (mLibCallback != null ) {						
						mLibCallback = null;
					}
				}
				
				@Override
				public void onServiceConnected(ComponentName name, IBinder service) {
					// TODO Auto-generated method stub
					mServiceStub =  IServiceAIDL.Stub.asInterface(service);
					if (mLibCallback == null) {
						mLibCallback = new LibCallbackStub();
						if(mServiceStub != null)
							try {
								mServiceStub.registerLibCallback(mLibCallback);
							} catch (RemoteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
					}
				}
			}, Context.BIND_AUTO_CREATE);
		}
	}
	
	public boolean getIsCameraOpen(){
		return mIsCameraOpen;
	}

	//请求打开摄像头
	public void reqStartCamera(int vedioId) throws RemoteException {
		LogUtil.i(TAG, "startCamera vedioId  " + vedioId + " mIsCameraOpen = " + mIsCameraOpen);
		myVedioId = vedioId;
		if(getStub() != null){
			if(getStub().getIsCameraCanUse()){
				if(mCameraHandler != null){
					 mCameraHandler.sendEmptyMessage(CAMERA_HANDER_MSG_START);
				}
			}else{
				getStub().reqStartCamera(vedioId);
			}
		}
	}
	
	//关闭摄像头
	public void stopCamera(){
		if(mCameraHandler != null){
			 mCameraHandler.sendEmptyMessage(CAMERA_HANDER_MSG_STOP);
		}	
	}
	
	public void stopPrivew(){
		if(mCameraInterface.getCamera() != null){
			mCameraInterface.doStopPreview();
		}
	}
	
	//获取摄像头是否有信号
	public boolean getCvbsState(){	
		LogUtil.v(TAG, "Vedio  "+ myVedioId + " getCvbsState = " + mIsCVBSIn );
		return mIsCVBSIn;	
	}
	
	public void doStartPreview(SurfaceHolder holder){		
		if(mCameraInterface.getCamera() != null){
			mCameraInterface.doStartPreview(holder);
		}
	}
	
	public void setStopCameraOver(int vedioId) throws RemoteException {
		LogUtil.i(TAG, "setStopCameraOver vedioId  " + vedioId);
		if(getStub() != null)
			getStub().setStopCameraOver(vedioId);
	}
	
	public void getIsCameraCanUse() throws RemoteException {
		LogUtil.i(TAG, "getIsCameraCanUse" );
		if(getStub() != null)
			getStub().getIsCameraCanUse();
	}
	
	public void setCameraState(int vedioId,int state) throws RemoteException {
		if(state == 1)
			mIsCameraOpen = true;
		else
			mIsCameraOpen = false;
		LogUtil.i(TAG, "setCameraState vedioId  " + vedioId + " state =" + state);
		if(getStub() != null)
			getStub().setCameraState(vedioId,state);
	}

	/**
	 * Register air conditioner listener to airobservers.
	  * @param al
	  * 		air condition listerner
	  * @throws RemoteException  运行时异常
	 */
	public void registerListener(CameraListenerImp al) throws RemoteException {
		LogUtil.i(TAG, "registerListener ");
		if (!mCameraObservers.contains(al)) {
			mCameraObservers.add(al);			
		}
		LogUtil.i(TAG, "registerListener al : " + ((al == null) ? null : al.getClass().getName()));
	}

	/**
	 * Unregister air conditioner listener from airobservers.
	  * @param al
	  * 		air condition listerner
	  * @throws RemoteException 运行时异常
	 */
	public void unRegisterListener(CameraListenerImp al) throws RemoteException {
		if (mCameraObservers.contains(al)) {
			mCameraObservers.remove(al);
		}		
		
		LogUtil.i(TAG, "unRegisterListener al : " + ((al == null) ? null : al.getClass().getName()));
	}
	
	//-----------------------回调--------------------------------------
	private class LibCallbackStub extends ILibCallBack.Stub {		

		@Override
		public void stopCamera(int vedioId) throws RemoteException {
			LogUtil.i(TAG, "stopCamera  myVedioId = " + myVedioId + " vedioId = " + vedioId);
			if(myVedioId == vedioId){
				if(mCameraHandler != null){
					 mCameraHandler.sendEmptyMessage(CAMERA_HANDER_MSG_STOP);
				}	
			}
		}

		@Override
		public void startCamera(int vedioId) throws RemoteException {			
			LogUtil.i(TAG, "startCamera  myVedioId = " + myVedioId + " vedioId = " + vedioId);
			if(myVedioId == vedioId){
				if(mCameraHandler != null){
					 mCameraHandler.sendEmptyMessage(CAMERA_HANDER_MSG_START);
				}	
			}
		}		
	}	
	
	class CameraHandler extends Handler{
		
		public CameraHandler(Looper looper){
			super(looper);
		}
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			//打开摄像头
			if(msg.what==CAMERA_HANDER_MSG_START){
				try {
					mIsCVBSIn = false;
					LogUtil.v(TAG, "Vedio  "+ myVedioId + " startCamera  mIsCVBSIn = " + mIsCVBSIn );
					setCameraState(myVedioId,1);
					mCameraInterface.doOpenCamera(CameraManager.this, CameraManager.this);
				} catch (RemoteException e) {
					e.printStackTrace();
				}				
			}else if(msg.what==CAMERA_HANDER_MSG_STOP){
				if(mCameraInterface.getCamera() != null){
					mCameraInterface.doStopCamera();
					try {
						mIsCVBSIn = false;
						LogUtil.v(TAG, "Vedio  "+ myVedioId + " stopCamera  mIsCVBSIn = " + mIsCVBSIn );		
						setCameraState(myVedioId,0);
						setStopCameraOver(myVedioId);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}

		}
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		boolean isCvbsIn = true;
		boolean isALL_16 = true; //前面10个数据是否都是16，是的话表示无信号
		if(data.length >= 10){
			for(int i = 0;i < 10;i++){
				if(data[i] != data[i+1]){
					isALL_16 = false;
					break;
				}				
			}
		}
		
		if(isALL_16 && (data[0] == 16)){
			isCvbsIn = false;
		}
		
		if(SystemClock.elapsedRealtime() - mOpenCameraOkMillis  < 200){
			LogUtil.v(TAG, "Camera check cvbs too fast " );
			return;
		}
		
		if(mIsCVBSIn != isCvbsIn){
			mIsCVBSIn = isCvbsIn;
			LogUtil.v(TAG, "Camera mIsCVBSIn = " + mIsCVBSIn );
			for (CameraOpenStateListener al : mCameraObservers) {
				LogUtil.v(TAG, "onCameraCVBSChange");
				al.onCameraCVBSChange(mIsCVBSIn);
			}	
		}		
		
	}

	@Override
	public void cameraOpenError() {
		try {
			setCameraState(myVedioId,0);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		for (CameraOpenStateListener al : mCameraObservers) {
			LogUtil.i(TAG, "cameraOpenError");
			al.onCameraOpenError();
		}	
		
	}
	
	@Override
	public void cameraHasOpened() {
		try {
			mOpenCameraOkMillis = SystemClock.elapsedRealtime();
			setCameraState(myVedioId,1);			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (CameraOpenStateListener al : mCameraObservers) {
			LogUtil.i(TAG, "cameraHasOpened");
			al.onCameraHasOpened();
		}	
		
	}
}
