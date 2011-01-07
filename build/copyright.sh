#!/bin/sh

#
# Assigns missing copyright notices to each source file.
#

green=$'\033[0;32m'
blue=$'\033[1;34m'
white=$'\033[1;37m'
dirname=`dirname $0`

assign_copyright() {
    dir="$1"
    name="$2"
    pattern="$3"
    echo "${blue}***${white} Processing ${green}${dir}${white}"
    find "${dirname}/../$dir" -name "$pattern" \
	-exec sh "${dirname}/copyright_file.sh" \{\} "$name" \;
}

assign_copyright org.o42a.ast/src "Abstract Syntax Tree" "*.java"
assign_copyright org.o42a.backend.llvm/src "Compiler LLVM Back-end" "*.java"
assign_copyright org.o42a.backend.llvm.jni/include "Compiler JNI Bindings to LLVM" "*.h"
assign_copyright org.o42a.backend.llvm.jni/src "Compiler JNI Bindings to LLVM" "*.cc"
assign_copyright org.o42a.backend.llvm.test/src "Compiler LLVM Back-end Tests" "*.java"
assign_copyright org.o42a.cl/src "Compiler Command-Line Interface" "*.java"
assign_copyright org.o42a.codegen/src "Compiler Code Generator" "*.java"
assign_copyright org.o42a.compiler/src "Compiler" "*.java"
assign_copyright org.o42a.compiler.test/src "Compiler Tests" "*.java"
assign_copyright org.o42a.core/src "Compiler Core" "*.java"
assign_copyright org.o42a.intrinsic/src "Intrinsics" "*.java"
assign_copyright org.o42a.intrinsic/o42a "Intrinsics" "*.o42a"
assign_copyright org.o42a.lib.console/src "Console Module" "*.java"
assign_copyright org.o42a.lib.console/o42a "Console Module" "*.o42a"
assign_copyright org.o42a.parser/src "Parser" "*.java"
assign_copyright org.o42a.parser.test/src "Parser Tests" "*.java"
assign_copyright org.o42a.rt/include "Run-Time Library" "*.h"
assign_copyright org.o42a.rt/src "Run-Time Library" "*.c"
assign_copyright org.o42a.util/src "Utilities" "*.java"
