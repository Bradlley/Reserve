LOCAL_PATH := $(call my-dir)

# the cameramanager.jar
# ============================================================
include $(CLEAR_VARS)

LOCAL_SRC_FILES :=  $(call all-subdir-java-files)
LOCAL_SDK_VERSION := current

LOCAL_SRC_FILES += $(call all-renderscript-files-under, src) \
	src/com/soling/cameramanager/ILibCallBack.aidl \
	src/com/soling/cameramanager/IServiceAIDL.aidl \
	
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := CameraManagerLib
LOCAL_DX_FLAGS := --core-library


LOCAL_AIDL_INCLUDES += $(LOCAL_PATH)/src/com/cameramanager/

#LOCAL_PROGUARD_ENABLED := disabled
LOCAL_DEX_PREOPT := false
include $(BUILD_JAVA_LIBRARY)

#make xml
#===============================================================
include $(CLEAR_VARS)
LOCAL_MODULE := CameraManagerLib.jar.xml
LOCAL_MODULE_CLASS := ETC
# This will install the file in /system/etc/permissions
LOCAL_MODULE_PATH := $(TARGET_OUT_ETC)/permissions
LOCAL_SRC_FILES := $(LOCAL_MODULE)
include $(BUILD_PREBUILT)

#make apk
#===============================================================



