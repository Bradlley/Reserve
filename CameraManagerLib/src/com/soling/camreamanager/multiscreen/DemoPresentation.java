package com.soling.camreamanager.multiscreen;

import com.soling.camreamanager.preview.CameraGLSurfaceView;
import com.soling.camreamanager.util.LogUtil;

import android.app.Presentation;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Display;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.LinearLayout;

/**
 * Created by Administrator on 2017/4/11.
 */

public class DemoPresentation extends Presentation {
    private CameraGLSurfaceView mGLSurfaceView;
    private LinearLayout mLayout;

    public DemoPresentation(Context outerContext, Display display) {
        super(outerContext, display);
        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
//        mContext = context;
    }

    public DemoPresentation(Context outerContext, Display display, int theme) {
        super(outerContext, display, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createLayout();
        setContentView(mLayout);
        LogUtil.d("DemoPresentation", "-onCreate------------> mGLSurfaceView = " + mGLSurfaceView);
    }

    public CameraGLSurfaceView getGLSurfaceView() {
    	 LogUtil.d("DemoPresentation", "-getGLSurfaceView------------> mGLSurfaceView = " + mGLSurfaceView);
        return mGLSurfaceView;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
    
    private void createLayout(){
	   mLayout = new LinearLayout(getContext());
	   LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(      
	          LinearLayout.LayoutParams.MATCH_PARENT,      
	          LinearLayout.LayoutParams.MATCH_PARENT
	   );     
	   mLayout.setLayoutParams(p);
	   mGLSurfaceView = new CameraGLSurfaceView(getContext());
	   mGLSurfaceView.setLayoutParams(p);
	   mLayout.addView(mGLSurfaceView);
    }
}
