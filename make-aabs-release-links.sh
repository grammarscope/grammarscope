#!/bin/bash

DIR=dist/releases/
mkdir -p "$DIR"

aabs=`./find-aabs.sh`
for aab in $aabs; do
	echo "- $aab"
	ln -sf "$aab" "$DIR"
done
