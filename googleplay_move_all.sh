#!/bin/bash

VERSION_CODE="$1"
TRACK="$2"
RELEASE_NAME="$3"
RECENT_CHANGES="$4"

if [ "$#" -ne 4 ]; then
	echo "VERSION(number) TRACK[alpha,beta,production] NAME([A|B|R]number) CHANGES"
	exit 1
fi

packages="
org.grammarscope
org.grammarscope.premium
org.grammarscope.udpipe
org.grammarscope.udpipe.premium
org.grammarscope.corenlp
org.grammarscope.corenlp.premium
org.mysyntaxnet
"

for PACKAGE in ${packages}; do
	# for split apks
	# ./googleplay_move.sh ${PACKAGE} "10${VERSION_CODE} 20${VERSION_CODE} 30${VERSION_CODE} 40${VERSION_CODE}" ${TRACK} "${RELEASE_NAME}" "${RECENT_CHANGES}"
	./googleplay_move.sh ${PACKAGE} "${VERSION_CODE}" ${TRACK} "${RELEASE_NAME}" "${RECENT_CHANGES}"
	echo
done
