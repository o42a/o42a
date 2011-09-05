#
# Default build settings.
#
# Copy this to file named org.o42a.backend.llvm.jni.mk and customize.
#

# Path to "javah" executable.
javah = $(JAVA_HOME)/bin/javah

# JNI include path for both "jni.h" and "jni_md.h".
includes += -I $(JAVA_HOME)/include -I $(JAVA_HOME)/include/linux

# JNI linker flags.
# Will be determined automatically if not set.
java_ldflags =

# Shared library name.
so_name = libo42ac_llvm.so

# llvm-config executable.
llvm_config = llvm-config


# Set to non-empty value to statically link against LLVM.
llvm_static = 

# Space-separated LLVM component names to pass to LLVM-config.
# Invoke `llvm-config --components` to see all possible values.
# Only meaningful when llvm_static set.
llvm_components = all

# libLLVM shared object file.
# Will be searched in predefined locations if omitted.
# Only meaningful when llvm_static unset.
llvm_so =

# Uncomment this to disable debug
#debug_cxxflags = -D O42AC_NDEBUG
