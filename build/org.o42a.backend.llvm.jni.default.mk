#
# Default build settings.
#
# Copy this to file named org.o42a.backend.llvm.jni.mk and customize.
#

# Path to "javah" executable.
javah = $(JAVA_HOME)/bin/javah

# JNI include path for both "jni.h" and "jni_md.h".
includes += -I $(JAVA_HOME)/include -I $(JAVA_HOME)/include/linux

# JNI linker flags
java_ldflags = -L $(JAVA_HOME)/jre/lib/amd64 -ljava

# Shared library name.
so_name = libo42ac_llvm.so

# Space-separated LLVM component names to pass to LLVM-config.
# Invoke `llvm-config --components` to see all possible values.
llvm_components = all

# Uncomment this to disable debug
#debug_cxxflags = -D O42AC_NDEBUG
