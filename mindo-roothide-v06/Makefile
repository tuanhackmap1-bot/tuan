# RootHide/Dopamine iOS 16.0-16.5
export THEOS_PACKAGE_SCHEME = roothide
ARCHS = arm64
TARGET = iphone:clang:16.5:16.0
FINALPACKAGE = 1

include $(THEOS)/makefiles/common.mk
TWEAK_NAME = MinDoOverlay
MinDoOverlay_FILES = MinDoOverlay.c
MinDoOverlay_CFLAGS = -fno-stack-protector -Wno-cast-function-type
MinDoOverlay_FRAMEWORKS = UIKit Foundation QuartzCore CoreFoundation
MinDoOverlay_LIBRARIES = objc
include $(THEOS_MAKE_PATH)/tweak.mk
