#!/bin/sh

dir=`dirname $0`

"$dir/../o42ac" "$dir/all_tests.o42a" \
	--vmargs -Djava.library.path="$dir/../target" -ea:org.o42a... \
	-- -Xlinker -rpath="$dir/../target" -Xlinker --no-undefined \
	-L${dir}/../target -lo42a \
	-o"$dir/all_tests"  
