#!/bin/bash

R='\u001b[31m'
G='\u001b[32m'
Y='\u001b[33m'
B='\u001b[34m'
M='\u001b[35m'
C='\u001b[36m'
Z='\u001b[0m'

RELEASE_NAME="$1"
if [ -z "${RELEASE_NAME}" ]; then
	V=`./find-version.sh`
	RELEASE_NAME="I${V}"
	echo "Version name ${RELEASE_NAME}"
fi
RECENT_CHANGES="$2"
if [ -z "${RECENT_CHANGES}" ]; then
	RECENT_CHANGES="Fixes"
fi
DIR=dist/releases
AAB=app_grammarscope_udpipe
declare -A PACKAGES
PACKAGES=([premium]=org.grammarscope.udpipe.premium)
FLAVORS="${!PACKAGES[@]}"

for flavor in ${FLAVORS}; do
	package=${PACKAGES[${flavor}]}
	aab=${DIR}/${AAB}-${flavor}-release.aab
	echo -e "${Y}${flavor} ${M}${package} ${C}${aab}${Z}"
	python2 googleplay_upload_aab.py \
		${package} \
		"${RELEASE_NAME}" \
		"${RECENT_CHANGES}" \
		${DIR}/${AAB}-${flavor}-release.aab
done
