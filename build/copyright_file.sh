#!/bin/sh

green=$'\033[0;32m'
red=$'\033[0;31m'
white=$'\033[1;37m'
dirname=`dirname $0`


( echo "Updating copyright of: ${green}$1${white}" && \
    awk -v progname="$2" -f "${dirname}/copyright.awk" "$1" > "$1.licensed" && \
    mv -f "$1.licensed" "$1" ) \
    || ( echo "${red}Failed to process ${green}$1${white}" > 2 && exit 1 )
