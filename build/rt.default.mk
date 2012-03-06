#
# Default build settings.
#
# Copy this to file named rt.mk and customize.
#

includes = -I /usr/include

# Shared library name.
so_name = libo42a.so

# Release preprocessor flags.
release_cppflags = -DNDEBUG

# Debug preprocessor flags.
debug_cppflags = -UNDEBUG

# Release C compiler flags.
release_cflags = -O2

# Debug C compiler flags.
debug_cflags = -O0

# icu-config executable.
icu_config = icu-config
