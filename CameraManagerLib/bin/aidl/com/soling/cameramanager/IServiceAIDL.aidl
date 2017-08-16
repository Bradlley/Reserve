package com.soling.cameramanager;

import com.soling.cameramanager.ILibCallBack;

interface IServiceAIDL
{
	//注册jar包的回调参数
	void registerLibCallback(ILibCallBack callback);	
	
	//注消jar包的回调参数
	void unregisterLibCallback(ILibCallBack callback);
	
	//请求打开摄像头
	void reqStartCamera(int vedioId);
	
	//通知关闭摄像头完成
	void setStopCameraOver(int vedioId);
	
	//获取当前Camera是否可用
	boolean getIsCameraCanUse();
	
	//设置camera状态
	void setCameraState(int vedioId,int state);	
}