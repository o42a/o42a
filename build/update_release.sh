#!/bin/sh

dirname=`dirname $0`
release="$1"

[ -z "${release}" ] && echo "Release number not specified" >&2 && exit 1

update_mf() {
	local mf="${dirname}/../org.o42a.$1/META-INF/MANIFEST.MF"
	sed -i "s/^\(Bundle-Version:\).*/\1 ${release}/;\
		s/\(.*org\.o42a\..*;bundle-version=\"\)[^\"]*\(.*\)/\1${release}\2/" \
		"${mf}"
}

sed -i "s/\(o42a_release *= *\).*/\1${release}/" "${dirname}/o42a.release"
sed -i "s/^\(O42A_RELEASE=\).*/\1\"${release}\"/" "${dirname}/../o42ac"

update_mf analysis
update_mf ast
update_mf backend.constant
update_mf backend.llvm
update_mf cl
update_mf codegen
update_mf common
update_mf compiler
update_mf compiler.test
update_mf core
update_mf intrinsic
update_mf lib.collections
update_mf lib.console
update_mf lib.macros
update_mf lib.test
update_mf parser
update_mf parser.test
update_mf root
update_mf tools
update_mf util
