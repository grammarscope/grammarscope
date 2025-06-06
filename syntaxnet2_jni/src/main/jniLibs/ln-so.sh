#!/bin/bash

#set -e
#set -x

archs="x86_64 x86 armeabi-v7a arm64-v8a"
where=../../../../../libs
where=`readlink -m ${where}`
echo "Source dir is ${where}"
if [ ! -e ${where} ]; then
	echo "Source dir does not exist ${where}"
pwd
	exit 1
fi
echo ${where}

for a in ${archs}; do
	mkdir -p ${a}
	pushd ${a} > /dev/null
	rm -f libsyntaxnet_inference2.so 2> /dev/null
	src=${where}/bazel-out/android-${a}-opt/bin/libsyntaxnet_inference2.so
	if [ ! -e ${src} ]; then
		echo "Source file does not exist ${src}"
		pwd
		exit 1
	fi
	echo "${src} ."
	ln -sf ${src}
	popd > /dev/null
done
ls -lR
