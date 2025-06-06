#!/bin/bash

thisdir="`dirname $(readlink -m $0)`"
thisdir="$(readlink -m ${thisdir})"
source ./lib-artwork.sh

scripts=`find . -mindepth 2 -name 'make-artwork.sh'`

for s in ${scripts}; do
	d=`dirname $s`
	echo -e "${MAGENTA}${d}${RESET}"
	pushd ${d} > /dev/null
	if [ ! -L lib-artwork.sh ]; then
		echo -e "${YELLOW}${d} has no lib${RESET}"
	else
		./make-artwork.sh
	fi
	popd > /dev/null
done

echo "C H E C K I N G"
for s in ${scripts}; do
	d=`dirname $s`
	d="$(readlink -m ${d}/../src/main)"
	echo -e "${MAGENTA}${d}${RESET}"
	pushd ${d} > /dev/null

	echo -en "${RED}"
	find -L . -name '*.png' -mmin +10
	echo -en "${RESET}"

	popd > /dev/null
done


