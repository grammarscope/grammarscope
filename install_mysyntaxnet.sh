#!/bin/bash

#list devices
#adb devices -l
#device=ce12160c903ac05e0c
device=
if [ "-device" == "$1" ]; then
	shift
	device=$1
	shift 
fi
if [ ! -z "${device}" ]; then
	device="-s ${device}"
fi
echo "device=${device}"

DIR=dist/releases
p="org.mysyntaxnet"

if adb shell pm list packages | grep "${p}$"; then 
	echo "* uninstalling ${p}";
	adb ${device} uninstall ${p}
fi

adb ${device} install ${DIR}/app_mysyntaxnet-base-arm64-v8a-release.apk

