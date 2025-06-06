#!/bin/bash

source "./lib-artwork.sh"

std="bullet*.svg gradient*.svg globe*.svg"
sun="sun*.svg"
flower="flower*.svg"
asterisk="asterisk*.svg"
sphere="sphere*.svg"

d="${dirassets}/icons"

make_icon "${std}" 32 "${d}/small"
make_icon "${std}" 64 "${d}/base"
make_icon "${std}" 96 "${d}/large"
make_icon "${std}" 128 "${d}/xlarge"

make_icon "${asterisk}" 64 "${d}"
make_icon "${sun}" 64 "${d}"
make_icon "${flower}" 64 "${d}"
make_icon "${sphere}" 16 "${d}"

