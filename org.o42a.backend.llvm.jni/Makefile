#    Compiler JNI Bindings to LLVM Build Script
#    Copyright (C) 2010-2013 Ruslan Lopatin
#
#    This file is part of o42a.
#
#    o42a is free software: you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation, either version 3 of the License, or
#    (at your option) any later version.
#
#    o42a is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with this program.  If not, see <http://www.gnu.org/licenses/>.

#
# See org.o42a.backend.llvm.jni.default.mk for customization options.
#

headersdir = include/o42ac/llvm
o42a_include_dir = ../runtime/include
srcdir = src
java_classes_dir = ../org.o42a.backend.llvm/bin
java_package_dir = $(java_classes_dir)/org/o42a/backend/llvm
java_package = org.o42a.backend.llvm
javah_classpath = ../org.o42a.util/bin:../org.o42a.analysis/bin:../org.o42a.codegen/bin:$(java_classes_dir)
build_dir = ../build
default_mk = $(build_dir)/llvm.default.mk
common_mk = $(build_dir)/llvm.mk
target_dir = ../target
so_lib = $(target_dir)/$(so_name)

common_deps = $(headersdir)/*.h

.PHONY : all clean javah clean_javah
.DEFAULT_GOAL : all

include $(default_mk)
-include $(common_mk)


# Determine Java LDFLAGS if not set.
ifndef java_ldflags

uname_arch = $(shell uname -m)

ifeq ($(uname_arch),x86_64)
java_arch = amd64
else ifeq ($(uname_arch),i686)
java_arch = i386
else ifeq ($(uname_arch),i586)
java_arch = i386
else ifeq ($(uname_arch),i486)
java_arch = i386
else
java_arch = $(uname_arch)
endif

java_ldflags = -L $(JAVA_HOME)/jre/lib/$(java_arch) -l java

endif

includes += -I $(o42a_include_dir) -I include
CXXFLAGS += -pthread -D_XOPEN_SOURCE=500
CXXFLAGS += -Wall -Winline -W -Wwrite-strings
CXXFLAGS := $(CXXFLAGS) $(shell $(llvm_config) --cxxflags) $(debug_cxxflags) $(includes)
LDFLAGS := $(LDFLAGS) $(java_ldflags) $(shell $(llvm_config) --ldflags)

# Make all.
all : $(so_lib)


# Build common settings file if absent.
$(common_mk) :
	@echo "# Put customized settings here." > $(common_mk)


# Clean all generated files but JNI headers.
clean :
	rm -f $(so_lib)
	rm -f $(srcdir)/*.o


# Generate all JNI headers.
javah : $(srcdir)/jni_module.h
javah : $(srcdir)/jni_id.h
javah : $(srcdir)/jni_data.h
javah : $(srcdir)/jni_function.h
javah : $(srcdir)/jni_int.h
javah : $(srcdir)/jni_fp.h
javah : $(srcdir)/jni_ptr.h
javah : $(srcdir)/jni_code.h


# Clean generated JNI headers.
clean-javah :
	rm -f $(srcdir)/jni_*.h


ifdef llvm_static

llvm_ldflags = $(shell $(llvm_config) --libs $(llvm_components))

else

llvm_version = $(shell $(llvm_config) --version)

ifeq ($(strip $(llvm_so)),)

llvm_ldflags = -Wl,-rpath=$(shell $(llvm_config) --libdir) -lLLVM-$(llvm_version)

else

llvm_ldflags = -l:$(llvm_so)

endif

endif

# Build shared library.
$(so_lib) : $(srcdir)/debug.o
$(so_lib) : $(srcdir)/BackendModule.o
$(so_lib) : $(srcdir)/jni_module.o
$(so_lib) : $(srcdir)/jni_id.o
$(so_lib) : $(srcdir)/jni_data.o
$(so_lib) : $(srcdir)/jni_function.o
$(so_lib) : $(srcdir)/jni_int.o
$(so_lib) : $(srcdir)/jni_fp.o
$(so_lib) : $(srcdir)/jni_ptr.o
$(so_lib) : $(srcdir)/jni_code.o
$(so_lib) :
	mkdir -p $(target_dir)
	$(CXX) $(LDFLAGS) -Wl,--no-undefined -shared -o $@ $+ $(llvm_ldflags)


# Compile commons.
$(srcdir)/debug.o : $(headersdir)/debug.h
$(srcdir)/debug.o : $(srcdir)/debug.cc
	$(call compile)

$(srcdir)/BackendModule.o : $(headersdir)/debug.h
$(srcdir)/BackendModule.o : $(headersdir)/BackendModule.h
$(srcdir)/BackendModule.o : $(srcdir)/BackendModule.cc
	$(call compile)


# Generate individual JNI headers.
$(srcdir)/jni_module.h : $(java_package_dir)/data/LLVMModule.class
	$(call make_javah)

$(srcdir)/jni_id.h : $(java_package_dir)/id/LLVMId.class
	$(call make_javah)

$(srcdir)/jni_data.h : $(java_package_dir)/data/SystemTypeInfo.class
$(srcdir)/jni_data.h : $(java_package_dir)/data/LLVMDataAllocator.class
$(srcdir)/jni_data.h : $(java_package_dir)/data/LLVMDataWriter.class
$(srcdir)/jni_data.h :
	$(call make_javah)

$(srcdir)/jni_function.h : $(java_package_dir)/code/LLSignatureWriter.class
$(srcdir)/jni_function.h : $(java_package_dir)/code/LLFunction.class
$(srcdir)/jni_function.h : $(java_package_dir)/code/op/LLFunc.class
$(srcdir)/jni_function.h :
	$(call make_javah)

$(srcdir)/jni_int.h : $(java_package_dir)/code/op/IntLLOp.class
	$(call make_javah)

$(srcdir)/jni_fp.h : $(java_package_dir)/code/op/FpLLOp.class
	$(call make_javah)

$(srcdir)/jni_ptr.h : $(java_package_dir)/code/LLVMCodeBackend.class
$(srcdir)/jni_ptr.h : $(java_package_dir)/code/op/PtrLLOp.class
$(srcdir)/jni_ptr.h : $(java_package_dir)/code/op/RelLLOp.class
$(srcdir)/jni_ptr.h :
	$(call make_javah)


$(srcdir)/jni_code.h : $(java_package_dir)/code/LLCode.class
$(srcdir)/jni_code.h :
	$(call make_javah)


# Compile JNI bindings.
$(srcdir)/jni_data.o : $(o42a_include_dir)/o42a/memory/gc.h
$(srcdir)/jni_data.o : $(o42a_include_dir)/o42a/types.h

# Implicit rule for JNI bindings compilation.
$(srcdir)/jni_*.o : $(common_deps)
$(srcdir)/jni_%.o : $(srcdir)/jni_%.cc $(srcdir)/jni_%.h
	$(call compile)


# JNI header generation routine.
define make_javah
	@rm -f $@
	$(javah) -classpath $(javah_classpath) -o $@ \
		$(subst /,.,$(patsubst $(java_classes_dir)/%.class,%,$+))
endef

# C++ compiler invocation routine.
define compile
	$(CXX) -c $(CPPFLAGS) $(CXXFLAGS) -o $@ $<
endef
