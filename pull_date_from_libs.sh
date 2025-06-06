#/bin/bash

dir="."
exec=cp-so.sh
jni_syntaxnet1="syntaxnet1_jni/jniLibs syntaxnet1_jni/src/main/jniLibs"
jni_syntaxnet2="syntaxnet2_jni/jniLibs syntaxnet2_jni/src/main/jniLibs"
jni_udppipe="udpipe_jni/jniLibs udpipe_jni/src/main/jniLibs"
jnilib_dirs="${jni_syntaxnet1} ${jni_syntaxnet2} ${jni_udppipe}"

for d in ${jnilib_dirs}; do
	if [ ! -e "${dir}/${d}" ]; then
		echo "${dir}/${d} does not exist"
		continue
	fi
	pushd ${dir}/${d} > /dev/null
	if [ ! -e "${exec}" ]; then
		echo ".${exec} does not exist"
		continue
	fi
	./${exec}
	popd > /dev/null
done
echo "RECAP"
for d in ${jnilib_dirs}; do
	if [ ! -e "${dir}/${d}" ]; then
		echo "${dir}/${d} does not exist"
		continue
	fi
	#echo -n "${d} "
	#stat -c %y "${dir}/${d}"
	find "${dir}/${d}" -name '*.so' -exec stat -c %y {} \;
	#stat -c %y "${dir}/${d}"
done
