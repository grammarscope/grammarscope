#!/bin/bash

packages="
org.grammarscope
org.grammarscope.premium
org.grammarscope.udpipe
org.grammarscope.udpipe.premium
org.grammarscope.corenlp
org.grammarscope.corenlp.premium
org.mysyntaxnet
"

source ./define_colors.sh

echo -e "${M}build.gradle\n$(grep 'version[CN]' build.gradle.kts)${Z}"

for p in ${packages}; do
	echo -e "${Y}${p}${Z}"
	python2 googleplay_list.py ${p}
	echo
done

