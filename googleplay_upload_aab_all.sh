#!/bin/bash

RELEASE_NAME="$1"
if [ -z "${RELEASE_NAME}" ]; then
	V=`./find-version.sh`
	RELEASE_NAME="I${V}"
	echo "Version name ${RELEASE_NAME}"
fi
RECENT_CHANGES="Fixes"

./googleplay_upload_aab_grammarscope_syntaxnet.sh	"${RELEASE_NAME}" "${RECENT_CHANGES}"
./googleplay_upload_aab_grammarscope_udpipe.sh		"${RELEASE_NAME}" "${RECENT_CHANGES}"
./googleplay_upload_aab_grammarscope_corenlp.sh		"${RELEASE_NAME}" "${RECENT_CHANGES}"
./googleplay_upload_aab_mysyntaxnet.sh			"${RELEASE_NAME}" "${RECENT_CHANGES}"

