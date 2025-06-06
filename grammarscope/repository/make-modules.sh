#!/bin/bash

dir=/opt/devel/android-jung
modules="jung-api jung-graph-impl jung-algorithms jung-visualization jung-extra glue-geom glue-event glue-visualization"

for m in ${modules}; do
	echo ${m}
	aar=${dir}/${m}/build/outputs/aar/${m}-release.aar
	aar2=${m}.aar
	if [ ! -e "${aar}" ]; then
		echo FAIL
	fi
	ln -sf "${aar}" "${aar2}"
done
