#!/usr/bin/bash

declare -A tasks
export tasks=(
[syntaxnet]=app_grammarscope_syntaxnet
[udpipe]=app_grammarscope_udpipe
[corenlp]=app_grammarscope_corenlp
)

export apps="${!tasks[@]}"
