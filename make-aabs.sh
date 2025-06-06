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

./make-aabs-base.sh
./make-aabs-premium.sh

echo -e "${M}"
echo "current version is"
./find-version.sh
echo -e "${Z}"

