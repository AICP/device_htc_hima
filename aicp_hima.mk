# Inherit some common AICP stuff.
$(call inherit-product, vendor/aicp/config/common_full_phone.mk)

$(call inherit-product, device/htc/hima/full_hima.mk)

PRODUCT_NAME := aicp_hima

# AICP Device Maintainers
PRODUCT_BUILD_PROP_OVERRIDES += \
    DEVICE_MAINTAINERS="Julian Veit (Claymore1297), Ali B (eyosen), Joel Stein (k4y0z)"

# Boot animation
TARGET_SCREEN_HEIGHT := 1920
TARGET_SCREEN_WIDTH := 1080
-include vendor/aicp/config/bootanimation.mk
