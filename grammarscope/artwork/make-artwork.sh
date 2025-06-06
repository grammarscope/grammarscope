#!/bin/bash

source "./lib-artwork.sh"

launch_list="ic_launcher.svg ic_launcher_round.svg"
logo_list="logo_app.svg"
splash_list="ic_splash_app.svg"
icon_list="ic_error.svg"
button_list="ic_dependency.svg ic_semantics.svg"
settings_list="ic_settings_general.svg ic_settings_download.svg ic_settings_graph.svg ic_settings_text.svg ic_settings_semantics.svg ic_settings_system.svg ic_settings_provider.svg ic_settings_palette.svg"
lang_list="english.svg french.svg"
provider_list="ic_bind.svg ic_unbind.svg ic_bound.svg ic_pending.svg ic_ok.svg ic_warn.svg"
tips_list="tip_*.svg"
item_list="item.svg"

make_mipmap "${launch_list}" 48
make_res "${logo_list}" 48
make_res "${splash_list}" 72 w
make_res "${icon_list}" 32
make_res "${button_list}" 24
make_res "${settings_list}" 24
make_res "${lang_list}" 16
make_res "${provider_list}" 24
make_res "${item_list}" 32

make_app "ic_launcher.svg" 512

make_helpen "${tips_list}" 32
make_helpen "${logo_list}" 256

check
