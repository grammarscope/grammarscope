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

declare -A apks
packs="org.grammarscope.udpipe org.grammarscope.udpipe.premium"
apks=([org.grammarscope.udpipe]=app_grammarscope_udpipe-base-arm64-v8a-release.apk [org.grammarscope.udpipe.premium]=app_grammarscope_udpipe-premium-arm64-v8a-release.apk)

for p in ${packs}; do
	echo $p
	apk=${apks[$p]}
	echo ${apk}
	#continue
	if adb shell pm list packages | grep "${p}$"; then 
		echo "* uninstalling ${p}";
		adb ${device} uninstall ${p}
	fi
	echo "* installing ${p}";
	adb ${device} install ${DIR}/${apk}
done

