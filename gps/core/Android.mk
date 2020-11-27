LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := libloc_core
LOCAL_VENDOR_MODULE := true

LOCAL_MODULE_TAGS := optional

LOCAL_SHARED_LIBRARIES := \
    liblog \
    libutils \
    libcutils \
    libgps.utils \
    libdl

LOCAL_SRC_FILES += \
    LocApiBase.cpp \
    LocAdapterBase.cpp \
    ContextBase.cpp \
    LocDualContext.cpp \
    loc_core_log.cpp

LOCAL_CFLAGS += \
    -fno-short-enums \
    -D_ANDROID_

LOCAL_C_INCLUDES:= \
    $(TARGET_OUT_HEADERS)/gps.utils \
    $(TARGET_OUT_HEADERS)/libflp

include $(BUILD_SHARED_LIBRARY)
