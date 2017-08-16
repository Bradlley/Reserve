LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
   
LOCAL_SRC_FILES := $(call all-subdir-java-files, src)

LOCAL_JAVA_LIBRARIES := CameraManagerLib

LOCAL_PACKAGE_NAME := CameraManagerSdk

include $(BUILD_PACKAGE)