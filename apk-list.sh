#!/bin/bash

RELEASE=build/outputs/apk/release
BASERELEASE=build/outputs/apk/base/release
PREMIUMRELEASE=build/outputs/apk/premium/release
all="
app_mysyntaxnet/${BASERELEASE}/app_mysyntaxnet-base-armeabi-v7a-release.apk
app_mysyntaxnet/${BASERELEASE}/app_mysyntaxnet-base-arm64-v8a-release.apk
app_mysyntaxnet/${BASERELEASE}/app_mysyntaxnet-base-x86_64-release.apk
app_mysyntaxnet/${BASERELEASE}/app_mysyntaxnet-base-x86-release.apk

app_grammarscope_syntaxnet/${BASERELEASE}/app_grammarscope_syntaxnet-base-arm64-v8a-release.apk
app_grammarscope_syntaxnet/${BASERELEASE}/app_grammarscope_syntaxnet-base-armeabi-v7a-release.apk
app_grammarscope_syntaxnet/${BASERELEASE}/app_grammarscope_syntaxnet-base-x86-release.apk
app_grammarscope_syntaxnet/${BASERELEASE}/app_grammarscope_syntaxnet-base-x86_64-release.apk

app_grammarscope_syntaxnet/${PREMIUMRELEASE}/app_grammarscope_syntaxnet-premium-arm64-v8a-release.apk
app_grammarscope_syntaxnet/${PREMIUMRELEASE}/app_grammarscope_syntaxnet-premium-armeabi-v7a-release.apk
app_grammarscope_syntaxnet/${PREMIUMRELEASE}/app_grammarscope_syntaxnet-premium-x86-release.apk
app_grammarscope_syntaxnet/${PREMIUMRELEASE}/app_grammarscope_syntaxnet-premium-x86_64-release.apk

app_grammarscope_udpipe/${BASERELEASE}/app_grammarscope_udpipe-base-x86_64-release.apk
app_grammarscope_udpipe/${BASERELEASE}/app_grammarscope_udpipe-base-arm64-v8a-release.apk
app_grammarscope_udpipe/${BASERELEASE}/app_grammarscope_udpipe-base-x86-release.apk
app_grammarscope_udpipe/${BASERELEASE}/app_grammarscope_udpipe-base-armeabi-v7a-release.apk

app_grammarscope_udpipe/${PREMIUMRELEASE}/app_grammarscope_udpipe-premium-x86_64-release.apk
app_grammarscope_udpipe/${PREMIUMRELEASE}/app_grammarscope_udpipe-premium-arm64-v8a-release.apk
app_grammarscope_udpipe/${PREMIUMRELEASE}/app_grammarscope_udpipe-premium-x86-release.apk
app_grammarscope_udpipe/${PREMIUMRELEASE}/app_grammarscope_udpipe-premium-armeabi-v7a-release.apk
"

RED='\u001b[31m'
GREEN='\u001b[32m'
YELLOW='\u001b[33m'
BLUE='\u001b[34m'
MAGENTA='\u001b[35m'
CYAN='\u001b[36m'
RESET='\u001b[0m'

function get_apks() {
	for apk in $all; do
		src=`readlink -m ${apk}`
		>&2 echo -n ${src}
		if [ -e "${src}" ]; then
			echo "${src}"
			>&2 echo -e "${GREEN} EXISTS${RESET}"
		else
			>&2 echo -e "${YELLOW} !EXISTS${RESET}"
		fi
	done
}

#get_apks

export APKS=`get_apks`
echo -e "${BLUE}${APKS}${RESET}"
