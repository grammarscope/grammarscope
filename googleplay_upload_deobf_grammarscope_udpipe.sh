#!/bin/bash

RELEASE_NAME="$1"
if [ -z "${RELEASE_NAME}" ]; then
	RELEASE_NAME="Anew"
	echo "Version name ${RELEASE_NAME}"
fi
RECENT_CHANGES="$2"
if [ -z "${RECENT_CHANGES}" ]; then
	RECENT_CHANGES="Fixes"
fi

./googleplay_upload_deobf.sh 'org.grammarscope.udpipe' 'grammarscope_udpipe' base ${RELEASE_NAME} ${RECENT_CHANGES}
./googleplay_upload_deobf.sh 'org.grammarscope.udpipe.premium' 'grammarscope_udpipe' premium ${RELEASE_NAME} ${RECENT_CHANGES}

