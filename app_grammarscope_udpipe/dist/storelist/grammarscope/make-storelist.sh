#!/bin/bash

thisdir="`dirname $(readlink -m $0)`"
thisdir="$(readlink -m ${thisdir})"

d="./"
mkdir -p ${d}

svg=icon.svg
wres=512
hres=512
png="${svg%.svg}.png"
echo "${svg} -> ${d}/${png} @ resolution ${wres}x${hres}"
inkscape ${svg} --export-png=${d}/${png} -h${hres} -w${wres} > /dev/null 2> /dev/null
wres=256
hres=256
png="${svg%.svg}256.png"
echo "${svg} -> ${d}/${png} @ resolution ${wres}x${hres}"
inkscape ${svg} --export-png=${d}/${png} -h${hres} -w${wres} > /dev/null 2> /dev/null

svg=feature-graphics.svg
wres=1024
hres=500
png="${svg%.svg}.png"
echo "${svg} -> ${d}/${png} @ resolution ${wres}x${hres}"
inkscape ${svg} --export-png=${d}/${png} -h${hres} -w${wres} > /dev/null 2> /dev/null

svg=promo-graphics.svg
wres=180
hres=120
png="${svg%.svg}.png"
echo "${svg} -> ${d}/${png} @ resolution ${wres}x${hres}"
inkscape ${svg} --export-png=${d}/${png} -h${hres} -w${wres} > /dev/null 2> /dev/null

