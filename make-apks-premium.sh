#!/bin/bash

case "$1" in
-z)
	echo "clean start"
	./gradlew --stop
	./gradlew clean
	;;
*)
	echo "resuming"
	;;
esac

#specific targets
apps="app_grammarscope_syntaxnet app_grammarscope_udpipe app_grammarscope_corenlp"
for a in $apps; do
	echo $a
	./gradlew :${a}:assemblePremiumRelease
done
