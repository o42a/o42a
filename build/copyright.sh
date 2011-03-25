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
	local filepath="$1"
	awk -v progname="$2" -f "${dirname}/copyright.awk" "${filepath}" | \
		write_if_not_empty "${filepath}"
}

process_list() {
	while read -r filepath; do
		process_file "${filepath}" "$1"
	done
}

assign_copyright() {
	local dir="$1"
	local name="$2"
	local pattern="$3"
	echo "Processing ${blue}${dir}${white}" >&2
	find "${dirname}/../${dir}" -name "$pattern" | process_list "${name}"
}

assign_copyright org.o42a.ast/src "Abstract Syntax Tree" "*.java"
assign_copyright org.o42a.backend.llvm/src "Compiler LLVM Back-end" "*.java"
assign_copyright org.o42a.backend.llvm.jni/include "Compiler JNI Bindings to LLVM" "*.h"
assign_copyright org.o42a.backend.llvm.jni/src "Compiler JNI Bindings to LLVM" "*.cc"
assign_copyright org.o42a.backend.llvm.test/src "Compiler LLVM Back-end Tests" "*.java"
assign_copyright org.o42a.cl/src "Compiler Command-Line Interface" "*.java"
assign_copyright org.o42a.codegen/src "Compiler Code Generator" "*.java"
assign_copyright org.o42a.common/src "Modules Commons" "*.java"
assign_copyright org.o42a.compiler/src "Compiler" "*.java"
assign_copyright org.o42a.compiler.test/src "Compiler Tests" "*.java"
assign_copyright org.o42a.core/src "Compiler Core" "*.java"
assign_copyright org.o42a.intrinsic/src "Intrinsics" "*.java"
assign_copyright org.o42a.intrinsic/o42a "Intrinsics" "*.o42a"
assign_copyright org.o42a.lib.console/src "Console Module" "*.java"
assign_copyright org.o42a.lib.console/o42a "Console Module" "*.o42a"
assign_copyright org.o42a.lib.test/src "Test Framework" "*.java"
assign_copyright org.o42a.lib.test/o42a "Test Framework" "*.o42a"
assign_copyright org.o42a.parser/src "Parser" "*.java"
assign_copyright org.o42a.parser.test/src "Parser Tests" "*.java"
assign_copyright org.o42a.rt/include "Run-Time Library" "*.h"
assign_copyright org.o42a.rt/src "Run-Time Library" "*.c"
assign_copyright org.o42a.test "Tests" "*.o42a"
assign_copyright org.o42a.util/src "Utilities" "*.java"
