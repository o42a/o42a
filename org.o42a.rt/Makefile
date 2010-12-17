#    Run-Time Library Build Script
#    Copyright (C) 2010 Ruslan Lopatin
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

CFLAGS += -fPIC -std=c99

headersdir = include/o42a
srcdir = src
build_dir = ../build
default_mk = $(build_dir)/org.o42a.rt.default.mk
common_mk = $(build_dir)/org.o42a.rt.mk
target_dir = ../target
so_lib = $(target_dir)/$(so_name)

common_deps = $(headersdir)/types.h
common_deps = $(headersdir)/debug.h

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

# Make all.
all : $(so_lib)


# Build common settings file if absent.
$(common_mk) :
	@echo "# Put customized settings here." > $(common_mk)


# Clean all generated files.
clean :
	rm -f $(so_lib)
	rm -f $(srcdir)/*.o
	rm -f $(srcdir)/fld/*.o
	rm -f $(srcdir)/lib/*.o


# Build shared library.
$(so_lib) : $(srcdir)/debug.o
$(so_lib) : $(srcdir)/types.o
$(so_lib) : $(srcdir)/string.o
$(so_lib) : $(srcdir)/values.o
$(so_lib) : $(srcdir)/object.o
$(so_lib) : $(srcdir)/fld/link.o
$(so_lib) : $(srcdir)/fld/obj.o
$(so_lib) : $(srcdir)/fld/scope.o
$(so_lib) : $(srcdir)/fld/var.o
$(so_lib) : $(srcdir)/fields.o
$(so_lib) : $(srcdir)/lib/io.o
$(so_lib) :
	mkdir -p $(target_dir)
	$(LD) $(LDFLAGS) -shared -o $@ $+

# Compile individual files.
$(srcdir)/types.o : $(headersdir)/types.h
$(srcdir)/debug.o : $(srcdir)/debug.c
	$(call compile)

$(srcdir)/types.o : $(headersdir)/types.h
$(srcdir)/types.o : $(srcdir)/types.c
	$(call compile)

$(srcdir)/string.o : $(common_deps)
$(srcdir)/string.o : $(headersdir)/string.h
$(srcdir)/string.o : $(srcdir)/string.c
	$(call compile)

$(srcdir)/object.o : $(common_deps)
$(srcdir)/object.o : $(headersdir)/field.h
$(srcdir)/object.o : $(headersdir)/object.h
$(srcdir)/object.o : $(srcdir)/object.c
	$(call compile)

$(srcdir)/values.o : $(common_deps)
$(srcdir)/values.o : $(headersdir)/object.h
$(srcdir)/values.o : $(headersdir)/values.h
$(srcdir)/values.o : $(srcdir)/values.c
	$(call compile)

$(srcdir)/fields.o : $(fld_deps)
$(srcdir)/fields.o : $(headersdir)/fld/*.h
$(srcdir)/fields.o : $(srcdir)/fields.c
	$(call compile)

# Implicit rule for field type descriptor compilation.
$(srcdir)/fld/*.o : $(fld_deps)
$(srcdir)/fld/%.o : $(srcdir)/fld/%.c $(headersdir)/fld/%.h
	$(call compile)

# Built-in modules compilation.
$(srcdir)/lib/io.o : $(common_deps)
$(srcdir)/lib/io.o : $(headersdir)/string.h
$(srcdir)/lib/io.o : $(headersdir)/lib/io.h
$(srcdir)/lib/io.o : $(srcdir)/lib/io.c
	$(call compile)

# C compiler invocation routine.
define compile
	$(CC) -c $(CPPFLAGS) $(CFLAGS) -o $@ $<
endef
