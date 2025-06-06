#!/bin/bash

source "./lib-artwork.sh"

launch_list="ic_launcher.svg ic_launcher_round.svg"
splash_list="ic_splash_app.svg"
logo_list="logo_app.svg"
icon_list="ic_settings_corenlp.svg"
make_mipmap "${launch_list}" 48
make_res "${logo_list}" 48
make_res "${splash_list}" 72 w
make_res "${icon_list}" 24
make_app "ic_launcher.svg" 512
make_helpen "logo_app.svg" 256

