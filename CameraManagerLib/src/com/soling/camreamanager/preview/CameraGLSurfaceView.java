package com.soling.camreamanager.preview;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.soling.camreamanager.util.LogUtil;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;

public class CameraGLSurfaceView extends GLSurfaceView implements Renderer{	

	private static final String TAG = "yanzi";
	Context mContext;
	int mTextureID = -1;
	DirectDrawer mDirectDrawer;
	boolean isOnResume = false;
	
	public CameraGLSurfaceView(Context context) {
		super(context);
		Log.i(TAG, "CameraGLSurfaceView..super(context).");
		mContext = context;
		setEGLContextClientVersion(2);
		setRenderer(this);
		setRenderMode(RENDERMODE_WHEN_DIRTY);
	}	
	
	public CameraGLSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LogUtil.i(TAG, "CameraGLSurfaceView..super(context, attrs).");
		mContext = context;
		setEGLContextClientVersion(2);
		setRenderer(this);
		setRenderMode(RENDERMODE_WHEN_DIRTY);
	}
	
	
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		// TODO Auto-generated method stub
		LogUtil.i(TAG, "onSurfaceCreated...");
		mTextureID = createTextureID();
		mDirectDrawer = new DirectDrawer(mTextureID);

	}
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		// TODO Auto-generated method stub
		LogUtil.i(TAG, "onSurfaceChanged...");
		GLES20.glViewport(0, 0, width, height);	

	}
	@Override
	public void onDrawFrame(GL10 gl) {
		// TODO Auto-generated method stub
		//Log.i(TAG, "onDrawFrame...");
		TextureUtil.draw(mDirectDrawer,mTextureID);
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();	
	}
	
	public int createTextureID() {
		int[] texture = new int[1];
		GLES20.glGenTextures(1, texture, 0);
		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
//		GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);
		GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
//		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

		return texture[0];
	}
}
