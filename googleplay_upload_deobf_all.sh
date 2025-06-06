#!/bin/bash

RELEASE_NAME="Anew"
if [ ! -z "$1" ]; then
	RELEASE_NAME="$1"
	echo "Version name ${RELEASE_NAME}"
fi
RECENT_CHANGES="Fixes"

./googleplay_upload_deobf_grammarscope_syntaxnet.sh		"${RELEASE_NAME}" "${RECENT_CHANGES}"
./googleplay_upload_deobf_grammarscope_udpipe.sh		"${RELEASE_NAME}" "${RECENT_CHANGES}"
./googleplay_upload_deobf_mysyntaxnet.sh				"${RELEASE_NAME}" "${RECENT_CHANGES}"

