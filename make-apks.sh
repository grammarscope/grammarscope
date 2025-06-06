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

./make-apks-base.sh
./make-apks-premium.sh

./apk-version.sh

