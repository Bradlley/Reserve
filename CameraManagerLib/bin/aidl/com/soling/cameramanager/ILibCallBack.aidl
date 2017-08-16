package com.soling.cameramanager;

oneway interface ILibCallBack{
	/**
	* 关闭摄像头
	**/
	void stopCamera(int vedioId);
	
	/**
	* 打开摄像头
	**/
	void startCamera(int vedioId);
}