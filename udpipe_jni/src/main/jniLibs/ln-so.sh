#!/bin/bash

#set -e
#set -x

export BLACK='\u001b[30m'
export RED='\u001b[31m'
export GREEN='\u001b[32m'
export BLUE='\u001b[34m'
export YELLOW='\u001b[33m'
export MAGENTA='\u001b[35m'
export CYAN='\u001b[36m'
export WHITE='\u001b[37m'
export RESET='\u001b[0m'
export R=$RED
export G=$GREEN
export B=$BLUE
export Y=$YELLOW
export M=$MAGENTA
export C=$CYAN
export W=$WHITE
export Z=$RESET

archs="x86_64 x86 armeabi-v7a arm64-v8a"
base=libudpipe_inference.so
where=../../../../../libs
where=`readlink -m ${where}`
echo "Source dir is ${where}"
if [ ! -e ${where} ]; then
	echo -e "${R}Source dir does not exist ${where}${Z}"
pwd
	exit 1
fi
echo -e "${M}${where}${Z}"

for a in ${archs}; do
	mkdir -p ${a}
	pushd ${a} > /dev/null
	src="${where}/bazel-out/android-${a}-opt/bin/${base}"
	if [ ! -e ${src} ]; then
		echo -e "${R}Source file does not exist ${src}${Z}"
		pwd
		exit 1
	fi
	echo -e "${C}${src}${Z}"
	echo -e "${C}$(pwd)/${base}${Z}"
	ln -sf ${src} .
	stat -c "%n %y %s bytes" "${base}"
	echo
	popd > /dev/null
done

