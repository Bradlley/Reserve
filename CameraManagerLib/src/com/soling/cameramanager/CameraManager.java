package com.soling.cameramanager;

import java.util.ArrayList;

import com.soling.cameramanager.CameraInterface.CamOpenOverCallback;
import com.soling.camreamanager.imp.CameraListenerImp;
import com.soling.camreamanager.multiscreen.PresenttationManager;
import com.soling.camreamanager.preview.CameraGLSurfaceView;
import com.soling.camreamanager.preview.TextureUtil;
import com.soling.camreamanager.util.LogUtil;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
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
import android.view.View;
/**
 * @author:soling
 */
public class CameraManager implements CamOpenOverCallback,PreviewCallback,OnFrameAvailableListener{
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
	private int myVedioId = -1; //有数据默认是0,
	private boolean mIsCVBSIn = false;
	private boolean mIsCameraOpen = false;
	private static long mOpenCameraOkMillis = 0; //camera打开成功到cvbs记时200ms防抖
	private static long mReqOpenCameraMillis = 0; //请求打开camrea到预览最少300ms
	private final Handler mHandler = new Handler();
	private int mBindNum = 0;

	boolean mBindResult = false;
	
	private CameraGLSurfaceView mGlSurfaceView[];//前屏显示画布
	private boolean mIsNeedRearScreen = false; //是否支持后屏显示
	private boolean mIsOpenRearScreen = false; //是否开启后屏
	private boolean mIsShowRearScreen = false; //是否显示后屏
	
	private CameraGLSurfaceView mPipGlSurfaceView;//pip画布
	
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
	
	//初始化CameraManager时候要一起调用
	public void initPresenttationManager(boolean isNeedRearScreen){
		LogUtil.v(TAG, " CameraManager init ");
		mIsNeedRearScreen = isNeedRearScreen;
		if(isNeedRearScreen)
			PresenttationManager.getInstence(mContext);
	}
	
	public CameraManager(Context mContext) {
		LogUtil.v(TAG, " CameraManager init ");
		this.mContext = mContext;
		getNewStub();
		HandlerThread thread = new HandlerThread("camera");
		thread.start();
		mCameraHandler = new CameraHandler(thread.getLooper());
		mCameraInterface = CameraInterface.getInstance();
		TextureUtil.getInstance().setOnFrameAvailableListener(this);
		
	}

	public static CameraManager getInstance(Context mContext){
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
		LogUtil.v(TAG, "getNewStub() invoked mServiceStub = " + mServiceStub);
	
		if(mContext!=null && mServiceStub == null){
			Intent intent = new Intent();
			intent.setClassName(ServerPackageName, ServerClassName);
			mBindResult = mContext.bindService(intent,new ServiceConnection() {
				
				@Override
				public void onServiceDisconnected(ComponentName name) {		
					LogUtil.i(TAG, "onServiceDisconnected name  " + name );
					mServiceStub = null;
					if (mLibCallback != null ) {						
						mLibCallback = null;
					}
				}
				
				@Override
				public void onServiceConnected(ComponentName name, IBinder service) {
					LogUtil.i(TAG, "onServiceConnected name  " + name + " service = " + service);
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

			LogUtil.d(TAG, "bindService bindResult = " + mBindResult);
		}
	}
	
	public boolean getIsCameraOpen(){
		return mIsCameraOpen;
	}

	//请求打开摄像头
	public void reqStartCamera(int vedioId) throws RemoteException {
		LogUtil.i(TAG, "reqStartCamera vedioId  " + vedioId + " mIsCameraOpen = " + mIsCameraOpen);
		myVedioId = vedioId;
		if(getStub() != null){
			if(getStub().getIsCameraCanUse()){
				if(mCameraHandler != null){
					 mCameraHandler.sendEmptyMessage(CAMERA_HANDER_MSG_START);
				}
			}else{
				getStub().reqStartCamera(vedioId);
			}
		}else{
			if(mCameraHandler != null){
				 mCameraHandler.sendEmptyMessage(CAMERA_HANDER_MSG_START);
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
	
	//是否开启后屏显示 用于开关
	public void setOpenRearScreen(boolean isOpen){
		LogUtil.v(TAG, "setOpenRearScreen isOpen = " + isOpen );
		if(!mIsNeedRearScreen ){
			LogUtil.v(TAG, "setOpenRearScreen not need NeedRearScreen");
			return;
		}
		mIsOpenRearScreen = isOpen;		
		if(mIsOpenRearScreen){
			LogUtil.v(TAG, "please show by user");
		}else{
			hideRearScreen();
		}
	}	
	
	//显示自己的后屏到前台
	public void showRearScreen(){		
		if(!mIsNeedRearScreen && mIsOpenRearScreen){
			LogUtil.v(TAG, "showRearScreen mIsNeedRearScreen = " + mIsNeedRearScreen + ";mIsOpenRearScreen = " + mIsOpenRearScreen);
			return;
		}
		
		if(mIsOpenRearScreen){
			mIsShowRearScreen = true;
			PresenttationManager.getInstence(mContext).showAllDisPlay();
		}
	}
	
	//隐藏自己的后屏显示
	public void hideRearScreen(){
		PresenttationManager.getInstence(mContext).disMissDisPlay();
		mIsShowRearScreen = false;         
	}
	
	public void setPipSurfaceView(CameraGLSurfaceView glSurfaceView){
		mPipGlSurfaceView = glSurfaceView;
		LogUtil.i(TAG, "setPipSurfaceView  mPipGlSurfaceView = " + mPipGlSurfaceView);
	}
	
	public void doStartPreview(SurfaceHolder holder){	
		LogUtil.i(TAG, "doStartPreview holder  " + holder);
		if(mCameraInterface.getCamera() != null){
			mCameraInterface.doStartPreview(holder);
		}
	}
	
	public void doStartPreview(CameraGLSurfaceView[] glSurfaceView){	
		LogUtil.i(TAG, "doStartPreview glSurfaceView =  " + glSurfaceView);
		mGlSurfaceView = glSurfaceView;
		if(mCameraInterface.getCamera() != null){
			if(!CameraInterface.getInstance().isPreviewing()){
				CameraInterface.getInstance().doStartPreview(TextureUtil.getInstance());
			}  	  
		}
	}
	
	public void doStartPreview(CameraGLSurfaceView glSurfaceView){	
		LogUtil.i(TAG, "doStartPreview glSurfaceView  " + glSurfaceView);
		if(mGlSurfaceView == null){
			mGlSurfaceView = new CameraGLSurfaceView[1];
		}
		mGlSurfaceView[0] = glSurfaceView;
		if(mCameraInterface.getCamera() != null){
			if(!CameraInterface.getInstance().isPreviewing()){
				CameraInterface.getInstance().doStartPreview(TextureUtil.getInstance());
			}  	  
		}
	}
	
	public void doStopPreview(){	
		LogUtil.i(TAG, "doStopPreview ");
		if(mCameraInterface.getCamera() != null){
			mCameraInterface.doStopPreview();
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
					mReqOpenCameraMillis = SystemClock.elapsedRealtime();
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
		//回收内存
		CameraInterface.getInstance().addCallbackBuffer(data);
		
		if(SystemClock.elapsedRealtime() - mOpenCameraOkMillis  < 200){
			LogUtil.v(TAG, "Camera check cvbs too fast isCvbsIn =" + isCvbsIn + "  mIsCVBSIn = " + mIsCVBSIn);
			return;
		}
		
		if(mIsCVBSIn != isCvbsIn){
			mIsCVBSIn = isCvbsIn;
			LogUtil.v(TAG, "Camera mIsCVBSIn = " + mIsCVBSIn );
			//有信号变化Camera肯定打开了，设置下状态			
			try {
				setCameraState(myVedioId,1);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
		
		//如果打开摄像头太快等等下等待surfaceview创建成功
		long time = SystemClock.elapsedRealtime() - mReqOpenCameraMillis;		
		if(time  < 300){
			LogUtil.v(TAG, "Camera open  too fast time = " + time);
			synchronized (Thread.currentThread()) {  
				try {
					Thread.sleep(time);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}  			
		}		
		
		for (CameraOpenStateListener al : mCameraObservers) {
			LogUtil.i(TAG, "cameraHasOpened");
			al.onCameraHasOpened();
		}	
		
	}

	//SurfaceTexture 回调
	@Override
	public void onFrameAvailable(SurfaceTexture arg0) {
		
		if(CameraInterface.getInstance().isPreviewing()){
			//LogUtil.i(TAG, "onFrameAvailable...");
			if(mGlSurfaceView != null){
				for(CameraGLSurfaceView glSurfaceView:mGlSurfaceView){
					//LogUtil.i(TAG, "onFrameAvailable...glSurfaceView.isShown = " + glSurfaceView.isShown());
					if(glSurfaceView != null && glSurfaceView.isShown()){						
						glSurfaceView.requestRender();
					}
				}
			}
			
			if(mPipGlSurfaceView != null){
				//LogUtil.i(TAG, "mPipGlSurfaceView ...onFrameAvailable");
				mPipGlSurfaceView.requestRender();
			}
			
		//	LogUtil.i(TAG, "onFrameAvailable...mIsOpenRearScreen = " + mIsOpenRearScreen + "  mIsNeedRearScreen =" + mIsNeedRearScreen + "  mIsShowRearScreen = " + mIsShowRearScreen);
			if(mIsOpenRearScreen && mIsNeedRearScreen && mIsShowRearScreen)
				PresenttationManager.getInstence(mContext).updateDisplay();
		}
		
	}
}
