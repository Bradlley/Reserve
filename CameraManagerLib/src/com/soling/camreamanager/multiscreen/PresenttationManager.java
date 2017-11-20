package com.soling.camreamanager.multiscreen;


import com.soling.camreamanager.util.LogUtil;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;

public class PresenttationManager implements DisplayListener {
	private static final String TAG = PresenttationManager.class.getSimpleName();
	private final String PAL = "20b";
	private final String NTSC = "20e";
	
	private static PresenttationManager mPresenttationManager;
	private Context mAppContext;
	
	private DisplayManager mDisplayManager;
	private Display[] mDisplays;
	private PresentationService mService;
	private boolean mIsShowingPresent = false; //是否在显示
	private boolean mSetDisplaytyple = false;
	

	public PresenttationManager(Context context){  
		mAppContext = context;  			
		try {
			switchDispFormat(PAL);
			initData();
			startService();
		} catch (Exception e) {
			LogUtil.i(TAG,"PresenttationManager error");
		}
		
    } 		
	
	public static PresenttationManager getInstence(Context context){
		if(mPresenttationManager == null){
			mPresenttationManager = new PresenttationManager(context);
		}
		return mPresenttationManager;
	}
	
	
	public void startService(){
		Intent intent = new Intent(mAppContext, PresentationService.class);
		mAppContext.startService(intent);
		mAppContext.bindService(intent, mServiceConn, Context.BIND_AUTO_CREATE);
	}
	
	public void stopService(){
		mAppContext.unbindService(mServiceConn);
		mDisplayManager.unregisterDisplayListener(this);
	}
	
	public void showAllDisPlay(){
		LogUtil.i(TAG,"upDateDisplay mDisplays.length = " + mDisplays.length);
		if (mDisplays.length >= 2 && mService != null) {
			mIsShowingPresent = true;
			mService.showPresentation(null, mDisplays[1]);
		}
	}
	
	public void disMissDisPlay(){
		LogUtil.i(TAG,"disMissDisPlay");
		if (mDisplays.length >= 2 && mService != null) {
			mService.disMiss();
			mIsShowingPresent = false;
		}
	}
	
	public void updateDisplay(){
		if(mService != null){
			if(!mIsShowingPresent)
				showAllDisPlay();
			mService.requestRender();
		}
	}
	

	private ServiceConnection mServiceConn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LogUtil.i(TAG,"onServiceConnected");
			mService = ((PresentationService.LocalBinder) service).getService();
			mDisplays = mDisplayManager.getDisplays();
			int numDisplays = mDisplays.length;
			for (int i = 0; i < numDisplays; i++) {
				if (i == 0) {
					continue;
				}
				mService.disMiss();
				mIsShowingPresent = false;
			}			
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
		}
	};
	
	
	/**
	 * 数据初始化（屏幕列表、mediaPlayer等）
	 */	
	private void initData() {
		LogUtil.i(TAG,"initData");
		mDisplayManager = (DisplayManager) mAppContext.getSystemService(Context.DISPLAY_SERVICE);
		mDisplayManager.registerDisplayListener(this, null);
		mDisplays = mDisplayManager.getDisplays();
		printDisplays();
      
	}
	
	private void printDisplays(){
		if(mDisplays.length > 0){
			 LogUtil.i(TAG,"mDisplays0="+mDisplays+" length="+mDisplays.length+" name="+mDisplays[0].getName());			  
       }
	   if(mDisplays.length > 1){
        	LogUtil.i(TAG,"mDisplays1="+mDisplays+" length="+mDisplays.length+" name="+mDisplays[1].getName());
       }
	}
	
	//设置屏幕类型
	private void switchDispFormat(final String value) {
		Log.i("cxs","switchDispFormat value="+value);
		new Thread(){
			public void run() {
				try {
					int format = Integer.parseInt(value, 16);
					final DisplayManager dm = (DisplayManager) mAppContext.getSystemService(Context.DISPLAY_SERVICE);
					//int displaytype = android.view.Display.TYPE_HDMI;					
					int displaytype = 2;
					dm.setDisplayOutput(displaytype, format);
				} catch (NumberFormatException e) {

				}
			};
		}.start();
	}

	@Override
	public void onDisplayAdded(int arg0) {
		LogUtil.i(TAG,"onDisplayAdded");
		mDisplays = mDisplayManager.getDisplays();		
	}


	@Override
	public void onDisplayChanged(int arg0) {
		LogUtil.i(TAG,"onDisplayChanged");
		mDisplays = mDisplayManager.getDisplays();
		
	}


	@Override
	public void onDisplayRemoved(int arg0) {
		LogUtil.i(TAG,"onDisplayRemoved");
		mDisplays = mDisplayManager.getDisplays();		
	}

	
}
