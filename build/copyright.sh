#!/bin/sh

#
# Assigns missing copyright notices to each source file.
#

blue=$'\033[1;34m'
green=$'\033[0;32m'
red=$'\033[0;31m'
white=$'\033[1;37m'
dirname=`dirname $0`

write_if_not_empty() {
	local filepath="$1"
	local head=$(dd bs=1 count=1 2>/dev/null; echo a)
	head=${head%a}
	if [ "x$head" != x"" ]; then
		{
			printf %s "$head";
			cat;
		} | {
			cat > "${filepath}.licensed" && \
				mv -f "${filepath}.licensed" "${filepath}" && \
				echo "Updated copyright of: ${green}${filepath}${white}" >&2
		}
	fi
}

process_file() {
	local filepath="$2"
	awk -v license="$1" -v progname="$3" -f "${dirname}/copyright.awk" \
		"${filepath}" | \
			write_if_not_empty "${filepath}"
}

process_list() {
	while read -r filepath; do
		process_file "$1" "${filepath}" "$2"
	done
}

assign_copyright() {
	local license="$1"
	local file_extension="$2"
	local dir="$3"
	local name="$4"
	local pattern="*.${file_extension}"
	echo "Processing ${blue}${dir}${white}" >&2
	find "${dirname}/../${dir}" -name "$pattern" | \
		process_list "${license}.${file_extension}" "${name}"
}

assign_copyright PUB o42a examples/o42a
assign_copyright GPL java org.o42a.analysis/src "Compilation Analysis"
assign_copyright GPL java org.o42a.ast/src "Abstract Syntax Tree"
assign_copyright GPL java org.o42a.backend.constant/src "Constant Handler Compiler Back-end"
assign_copyright GPL java org.o42a.backend.llvm/src "Compiler LLVM Back-end"
assign_copyright GPL h    org.o42a.backend.llvm.jni/include "Compiler JNI Bindings to LLVM"
assign_copyright GPL cc   org.o42a.backend.llvm.jni/src "Compiler JNI Bindings to LLVM"
assign_copyright GPL java org.o42a.cl/src "Compiler Command-Line Interface"
assign_copyright GPL java org.o42a.codegen/src "Compiler Code Generator"
assign_copyright GPL java org.o42a.common/src "Modules Commons"
assign_copyright GPL java org.o42a.compiler/src "Compiler"
assign_copyright PUB java org.o42a.compiler.test/src
assign_copyright GPL java org.o42a.core/src "Compiler Core"
assign_copyright GPL java org.o42a.intrinsic/src "Intrinsics"
assign_copyright GPL java org.o42a.root/src "Root Object Definition"
assign_copyright MPL o42a org.o42a.root/o42a
assign_copyright GPL java org.o42a.lib.collections/src "Collections Library"
assign_copyright MPL o42a org.o42a.lib.collections/o42a
assign_copyright GPL java org.o42a.lib.console/src "Console Module"
assign_copyright MPL o42a org.o42a.lib.console/o42a
assign_copyright GPL java org.o42a.lib.macros/src "Standard Macros"
assign_copyright MPL o42a org.o42a.lib.macros/o42a
assign_copyright GPL java org.o42a.lib.test/src "Test Framework"
assign_copyright MPL o42a org.o42a.lib.test/o42a
assign_copyright GPL java org.o42a.parser/src "Parser"
assign_copyright PUB java org.o42a.parser.test/src
assign_copyright GPL java org.o42a.tools/src "Build Tools"
assign_copyright GPL java org.o42a.util/src "Utilities"
assign_copyright MPL h    runtime/include
assign_copyright MPL c    runtime/src
assign_copyright PUB c    runtime/test
assign_copyright PUB o42a tests/o42a
