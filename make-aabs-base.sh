#!/bin/bash

source define_colors.sh

case "$1" in
-z)
	echo -e "${C}clean start${Z}"
	./gradlew --stop
	./gradlew clean
	;;
*)
	echo -e "${C}resuming${Z}"
	;;
esac

#specific targets
apps="app_mysyntaxnet app_grammarscope_syntaxnet app_grammarscope_udpipe app_grammarscope_corenlp"
for a in $apps; do
	echo -e "${Y}${a}${Z}"
	./gradlew :${a}:bundleBaseRelease
	echo
done
