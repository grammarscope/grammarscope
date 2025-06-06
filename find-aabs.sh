#!/bin/bash

RELEASE=build/outputs/bundle/release
BASERELEASE=build/outputs/bundle/baseRelease
PREMIUMRELEASE=build/outputs/bundle/premiumRelease
all="
app_mysyntaxnet/${BASERELEASE}/app_mysyntaxnet-base-release.aab

app_grammarscope_syntaxnet/${BASERELEASE}/app_grammarscope_syntaxnet-base-release.aab
app_grammarscope_syntaxnet/${PREMIUMRELEASE}/app_grammarscope_syntaxnet-premium-release.aab

app_grammarscope_udpipe/${BASERELEASE}/app_grammarscope_udpipe-base-release.aab
app_grammarscope_udpipe/${PREMIUMRELEASE}/app_grammarscope_udpipe-premium-release.aab
"

RED='\u001b[31m'
GREEN='\u001b[32m'
YELLOW='\u001b[33m'
BLUE='\u001b[34m'
MAGENTA='\u001b[35m'
CYAN='\u001b[36m'
RESET='\u001b[0m'

function get_aabs() {
	for aab in $all; do
		src=`readlink -m ${aab}`
		>&2 echo -n ${src}
		if [ -e "${src}" ]; then
			echo "${src}"
			>&2 echo -e "${GREEN} EXISTS${RESET}"
		else
			>&2 echo -e "${YELLOW} !EXISTS${RESET}"
		fi
	done
}

#get_aabs

export aabS=`get_aabs`
echo ${aabS}

