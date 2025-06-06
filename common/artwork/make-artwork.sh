#!/bin/bash

source "./lib-artwork.sh"

logo_list="ic_launcher.svg"
other_list="logo_semantikos.svg logo_semantikos_wn.svg logo_semantikos_fn.svg logo_semantikos_vn.svg logo_treebolicwordnet.svg"
googleplay_list="ic_googleplay.svg"

make_mipmap "${logo_list}" 48
make_res "${other_list}" 48
make_res "${googleplay_list}" 192 w

