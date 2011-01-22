#!/bin/sh

blue=$'\033[1;34m'
green=$'\033[0;32m'
red=$'\033[0;31m'
white=$'\033[1;37m'
dirname=`dirname $0`

filepath="$1"

echo -n "Updating copyright of: ${green}$1${white}"
awk -v progname="$2" -f "${dirname}/copyright.awk" "$filepath" > "$filepath.licensed"

status=$?

if (( status == 42 )); then
    echo "${blue} ... not changed${white}"
    rm -f "$filepath.licensed"
    exit 0
fi
if (( status == 0 )); then
    echo "${blue} ... ${white}UPDATED"
    mv -f "$filepath.licensed" "$filepath" && exit 0
fi

echo "${blue} ... ${red}FAILED${white}" > 2 && exit status
