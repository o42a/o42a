#    Run-Time Library Build Script
#    Copyright (C) 2010,2011 Ruslan Lopatin
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
# See org.o42a.rt.default.mk for customization options.
#

CFLAGS += $(shell icu-config --cflags-dynamic)
CFLAGS += -std=c99 -fPIC

CPPGLAGS += $(shell icu-config --cppflags-dynamic)

LDFLAGS += $(shell icu-config --ldflags)
LDFLAGS += $(shell icu-config --ldflags-icuio)

headersdir = include/o42a
srcdir = src
build_dir = ../build
default_mk = $(build_dir)/org.o42a.rt.default.mk
common_mk = $(build_dir)/org.o42a.rt.mk
target_dir = ../target
release_so_lib = $(target_dir)/$(so_name)
debug_so_lib = $(target_dir)/$(subst .,_debug.,$(so_name))

release_flags = $(release_cppflags) $(release_cflags)
debug_flags = $(debug_cppflags) $(debug_cflags)

common_deps = $(headersdir)/types.h
common_deps += $(headersdir)/debug.h
common_deps += $(headersdir)/error.h

fld_deps = $(common_deps)
fld_deps += $(headersdir)/object.h
fld_deps += $(headersdir)/field.h
fld_deps += $(headersdir)/fields.h


.PHONY : all clean
.DEFAULT_GOAL : all

include $(default_mk)
-include $(common_mk)

includes += -I include
CFLAGS += -Wall -Winline -W -Wwrite-strings -Wno-unused $(includes)

objects = $(srcdir)/types.o
objects += $(srcdir)/memory.o
objects += $(srcdir)/string.o
objects += $(srcdir)/error.o
objects += $(srcdir)/integer.o
objects += $(srcdir)/float.o
objects += $(srcdir)/object.o
objects += $(srcdir)/fld/link.o
objects += $(srcdir)/fld/obj.o
objects += $(srcdir)/fld/scope.o
objects += $(srcdir)/fld/var.o
objects += $(srcdir)/fields.o
objects += $(srcdir)/lib/io.o

# Make all.
all : $(release_so_lib) $(debug_so_lib)


# Build common settings file if absent.
$(common_mk) :
	@echo "# Put customized settings here." > $(common_mk)


# Clean all generated files.
clean :
	rm -f $(release_so_lib)
	rm -f $(debug_so_lib)
	rm -f $(srcdir)/*.o
	rm -f $(srcdir)/fld/*.o
	rm -f $(srcdir)/lib/*.o


# Build shared library.
$(release_so_lib) : $(subst .o,.rel.o,$(objects))
$(release_so_lib) :
	mkdir -p $(target_dir)
	$(LD) $(LDFLAGS) -shared -o $@ $+

# Build debug shared library.
$(debug_so_lib) : $(srcdir)/debug.o
$(debug_so_lib) : $(subst .o,.dbg.o,$(objects))
$(debug_so_lib) :
	mkdir -p $(target_dir)
	$(LD) $(LDFLAGS) -shared -o $@ $+


# Compile individual files.
$(srcdir)/debug.o : $(headersdir)/types.h
$(srcdir)/debug.o : $(headersdir)/debug.h
$(srcdir)/debug.o : $(srcdir)/debug.c
	$(CC) -c $(CPPFLAGS) $(CFLAGS) $(debug_flags) -o $@ $<

$(srcdir)/types.*.o : $(headersdir)/debug.h
$(srcdir)/types.*.o : $(headersdir)/memory.h
$(srcdir)/types.*.o : $(headersdir)/types.h
$(srcdir)/types.%.o : $(srcdir)/types.c
	$(call compile)

$(srcdir)/memory.*.o : $(headersdir)/types.h
$(srcdir)/memory.*.o : $(headersdir)/debug.h
$(srcdir)/memory.*.o : $(headersdir)/memory.h
$(srcdir)/memory.%.o : $(srcdir)/memory.c
	$(call compile)

$(srcdir)/string.*.o : $(common_deps)
$(srcdir)/string.*.o : $(headersdir)/memory.h
$(srcdir)/string.*.o : $(headersdir)/string.h
$(srcdir)/string.%.o : $(srcdir)/string.c
	$(call compile)

$(srcdir)/error.*.o : $(common_deps)
$(srcdir)/error.*.o : $(headersdir)/string.h
$(srcdir)/error.%.o : $(srcdir)/error.c
	$(call compile)

$(srcdir)/integer.*.o : $(common_deps)
$(srcdir)/integer.*.o : $(headersdir)/string.h
$(srcdir)/integer.*.o : $(headersdir)/integer.h
$(srcdir)/integer.%.o : $(srcdir)/integer.c
	$(call compile)

$(srcdir)/float.*.o : $(common_deps)
$(srcdir)/float.*.o : $(headersdir)/string.h
$(srcdir)/float.*.o : $(headersdir)/float.h
$(srcdir)/float.%.o : $(srcdir)/float.c
	$(call compile)

$(srcdir)/object.*.o : $(common_deps)
$(srcdir)/object.*.o : $(headersdir)/memory.h
$(srcdir)/object.*.o : $(headersdir)/field.h
$(srcdir)/object.*.o : $(headersdir)/object.h
$(srcdir)/object.%.o : $(srcdir)/object.c
	$(call compile)

$(srcdir)/fields.*.o : $(fld_deps)
$(srcdir)/fields.*.o : $(headersdir)/fld/*.h
$(srcdir)/fields.%.o : $(srcdir)/fields.c
	$(call compile)

# Implicit rule for field type descriptor compilation.
$(srcdir)/fld/*.o : $(fld_deps)
$(srcdir)/fld/%.dbg.o : $(srcdir)/fld/%.c $(headersdir)/fld/%.h
	$(call compile)
$(srcdir)/fld/%.rel.o : $(srcdir)/fld/%.c $(headersdir)/fld/%.h
	$(call compile)

# Built-in modules compilation.
$(srcdir)/lib/io.*.o : $(common_deps)
$(srcdir)/lib/io.*.o : $(headersdir)/string.h
$(srcdir)/lib/io.*.o : $(headersdir)/lib/io.h
$(srcdir)/lib/io.%.o : $(srcdir)/lib/io.c
	$(call compile)

# C compiler invocation routine.
define compile
	$(CC) -c $(CPPFLAGS) $(CFLAGS) \
		$(if $(findstring .dbg.,$@),$(debug_flags),$(release_flags)) \
		-o $@ $<
endef
