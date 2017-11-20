package com.soling.camreamanager.multiscreen;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Binder;
import android.os.IBinder;
import android.view.Display;

import java.util.ArrayList;
import java.util.List;

import com.soling.camreamanager.preview.CameraGLSurfaceView;
import com.soling.camreamanager.util.LogUtil;


public class PresentationService extends Service {
    public final String TAG = getClass().getName();

    private IBinder mBinder = new LocalBinder();

    private List<DemoPresentation> mPresentations;

    private CameraGLSurfaceView glsurfaceView;
    private DemoPresentation mDemoPresentation;

    private boolean isSync = false;

    class LocalBinder extends Binder {
        public PresentationService getService() {
            return PresentationService.this;
        }
    }

    public PresentationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogUtil.d(TAG, "PresentationService --------------> onBind()");
        return mBinder;
    }

    public void showPresentation(Context context, Display display) {
    	LogUtil.d(TAG, "PresentationService --------------> showPresentation()");
    	if(mDemoPresentation == null)
    		mDemoPresentation = new DemoPresentation(this, display);
        glsurfaceView = mDemoPresentation.getGLSurfaceView();
        mDemoPresentation.show();
        isSync = true;
    }
    
    public void requestRender(){
    	/*LogUtil.d(TAG, "PresentationService --------------> glsurfaceView = " + glsurfaceView);*/
    	if(mDemoPresentation != null && glsurfaceView == null){
    		glsurfaceView = mDemoPresentation.getGLSurfaceView();
    	}
    	if(glsurfaceView != null){
    		//LogUtil.d(TAG, "PresentationService --------------> requestRender()");
         	glsurfaceView.requestRender();
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
    	LogUtil.d(TAG, "PresentationService --------------> onUnbind()");
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.d(TAG, "PresentationService --------------> onCreate()");          
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "PresentationService --------------> onDestroy()");
    }   

    public void disMiss() {   
    	LogUtil.d(TAG, "PresentationService --------------> disMiss()");
        if(mDemoPresentation != null){
        	mDemoPresentation.dismiss();
            stopForeground(true);
        }
        isSync = false;
    }
}
