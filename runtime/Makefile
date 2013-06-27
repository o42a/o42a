#    Copyright (C) 2010-2012 Ruslan Lopatin
#
#    This Source Code Form is subject to the terms of the Mozilla Public
#    License, v. 2.0. If a copy of the MPL was not distributed with this
#    file, You can obtain one at http://mozilla.org/MPL/2.0/.

#
# See rt.default.mk for customization options.
#

CFLAGS += $(shell $(icu_config) --cflags-dynamic)
CFLAGS += -std=c99 -pedantic -fPIC -pthread -D_XOPEN_SOURCE=500

CPPGLAGS += $(shell $(icu_config) --cppflags-dynamic)

LDFLAGS += $(shell $(icu_config) --ldflags)
LDFLAGS += $(shell $(icu_config) --ldflags-icuio)

headersdir = include/o42a
srcdir = src
build_dir = ../build
default_mk = $(build_dir)/rt.default.mk
common_mk = $(build_dir)/rt.mk
target_dir = ../target
release_so_lib = $(target_dir)/$(so_name)
debug_so_name = $(subst .,_debug.,$(so_name))
debug_so_lib = $(target_dir)/$(debug_so_name)

release_flags = $(release_cppflags) $(release_cflags)
debug_flags = $(debug_cppflags) $(debug_cflags)

common_deps = $(headersdir)/debug.h
common_deps += $(headersdir)/error.h
common_deps += $(headersdir)/types.h
common_deps += $(headersdir)/value.h

fld_deps = $(common_deps)
fld_deps += $(headersdir)/field.h
fld_deps += $(headersdir)/fields.h
fld_deps += $(headersdir)/memory/gc.h
fld_deps += $(headersdir)/object.h


.PHONY : all clean compile-tests test
.DEFAULT_GOAL : all

include $(default_mk)
-include $(common_mk)

includes += -I include
CFLAGS += -Wall -Winline -W -Wwrite-strings -Wno-unused $(includes)

objects = $(srcdir)/error.o
objects += $(srcdir)/fields.o
objects += $(srcdir)/object.o
objects += $(srcdir)/types.o
objects += $(srcdir)/value.o

objects += $(srcdir)/fld/dep.o
objects += $(srcdir)/fld/link.o
objects += $(srcdir)/fld/obj.o
objects += $(srcdir)/fld/scope.o
objects += $(srcdir)/fld/var.o

objects += $(srcdir)/kpr/array.o
objects += $(srcdir)/kpr/float.o
objects += $(srcdir)/kpr/integer.o
objects += $(srcdir)/kpr/link.o
objects += $(srcdir)/kpr/string.o
objects += $(srcdir)/kpr/void.o

objects += $(srcdir)/lib/io.o

objects += $(srcdir)/memory/gc.o
objects += $(srcdir)/memory/refcount.o

objects += $(srcdir)/type/array.o
objects += $(srcdir)/type/float.o
objects += $(srcdir)/type/integer.o
objects += $(srcdir)/type/link.o
objects += $(srcdir)/type/string.o

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
	rm -f $(srcdir)/*/*.o
	rm -rf bin


# Build shared library.
$(release_so_lib) : $(subst .o,.rel.o,$(objects))
$(release_so_lib) :
	mkdir -p $(target_dir)
	$(CC) $(LDFLAGS) -pthread -shared -Wl,-soname=$(so_name) -o $@ $+

# Build debug shared library.
$(debug_so_lib) : $(srcdir)/debug.o
$(debug_so_lib) : $(subst .o,.dbg.o,$(objects))
$(debug_so_lib) :
	mkdir -p $(target_dir)
	$(CC) $(LDFLAGS) -pthread -shared -Wl,-soname=$(debug_so_name) -o $@ $+


# Compile individual files.
$(srcdir)/debug.o : $(headersdir)/types.h
$(srcdir)/debug.o : $(headersdir)/debug.h
$(srcdir)/debug.o : $(srcdir)/debug.c
	$(CC) -c $(CPPFLAGS) $(CFLAGS) $(debug_flags) -o $@ $<

$(srcdir)/fields.*.o : $(fld_deps)
$(srcdir)/fields.*.o : $(headersdir)/fld/*.h
$(srcdir)/fields.*.o : $(headersdir)/kpr/*.h
$(srcdir)/fields.%.o : $(srcdir)/fields.c
	$(call compile)

$(srcdir)/error.*.o : $(common_deps)
$(srcdir)/error.*.o : $(headersdir)/type/string.h
$(srcdir)/error.%.o : $(srcdir)/error.c
	$(call compile)

$(srcdir)/object.*.o : $(common_deps)
$(srcdir)/object.*.o : $(headersdir)/memory/gc.h
$(srcdir)/object.*.o : $(headersdir)/field.h
$(srcdir)/object.*.o : $(headersdir)/object.h
$(srcdir)/object.%.o : $(srcdir)/object.c
	$(call compile)

$(srcdir)/types.*.o : $(headersdir)/debug.h
$(srcdir)/types.*.o : $(headersdir)/types.h
$(srcdir)/types.%.o : $(srcdir)/types.c
	$(call compile)

$(srcdir)/value.*.o : $(headersdir)/debug.h
$(srcdir)/value.*.o : $(headersdir)/memory/refcount.h
$(srcdir)/value.*.o : $(headersdir)/types.h
$(srcdir)/value.*.o : $(headersdir)/value.h
$(srcdir)/value.%.o : $(srcdir)/value.c
	$(call compile)

$(srcdir)/memory/gc.*.o : $(headersdir)/types.h
$(srcdir)/memory/gc.*.o : $(headersdir)/debug.h
$(srcdir)/memory/gc.*.o : $(headersdir)/memory/gc.h
$(srcdir)/memory/gc.%.o : $(srcdir)/memory/gc.c
	$(call compile)

$(srcdir)/memory/refcount.*.o : $(headersdir)/types.h
$(srcdir)/memory/refcount.*.o : $(headersdir)/debug.h
$(srcdir)/memory/refcount.*.o : $(headersdir)/memory/refcount.h
$(srcdir)/memory/refcount.%.o : $(srcdir)/memory/refcount.c
	$(call compile)

$(srcdir)/type/array.*.o : $(common_deps)
$(srcdir)/type/array.*.o : $(headersdir)/memory/gc.h
$(srcdir)/type/array.*.o : $(headersdir)/object.h
$(srcdir)/type/array.*.o : $(headersdir)/type/array.h
$(srcdir)/type/array.%.o : $(srcdir)/type/array.c
	$(call compile)

$(srcdir)/type/float.*.o : $(common_deps)
$(srcdir)/type/float.*.o : $(headersdir)/memory/refcount.h
$(srcdir)/type/float.*.o : $(headersdir)/type/string.h
$(srcdir)/type/float.*.o : $(headersdir)/type/float.h
$(srcdir)/type/float.%.o : $(srcdir)/type/float.c
	$(call compile)

$(srcdir)/type/integer.*.o : $(common_deps)
$(srcdir)/type/integer.*.o : $(headersdir)/memory/refcount.h
$(srcdir)/type/integer.*.o : $(headersdir)/type/string.h
$(srcdir)/type/integer.*.o : $(headersdir)/type/integer.h
$(srcdir)/type/integer.%.o : $(srcdir)/type/integer.c
	$(call compile)

$(srcdir)/type/link.*.o : $(common_deps)
$(srcdir)/type/link.*.o : $(headersdir)/memory/gc.h
$(srcdir)/type/link.*.o : $(headersdir)/object.h
$(srcdir)/type/link.*.o : $(headersdir)/type/link.h
$(srcdir)/type/link.%.o : $(srcdir)/type/link.c
	$(call compile)

$(srcdir)/type/string.*.o : $(common_deps)
$(srcdir)/type/string.*.o : $(headersdir)/memory/refcount.h
$(srcdir)/type/string.*.o : $(headersdir)/object.h
$(srcdir)/type/string.*.o : $(headersdir)/type/string.h
$(srcdir)/type/string.%.o : $(srcdir)/type/string.c
	$(call compile)

$(srcdir)/kpr/array.*.o : $(headersdir)/type/array.h
$(srcdir)/kpr/string.*.o : $(headersdir)/memory/refcount.h

# Implicit rule for field type structures compilation.
$(srcdir)/fld/*.o : $(fld_deps)
$(srcdir)/fld/%.dbg.o : $(srcdir)/fld/%.c $(headersdir)/fld/%.h
	$(call compile)
$(srcdir)/fld/%.rel.o : $(srcdir)/fld/%.c $(headersdir)/fld/%.h
	$(call compile)
	
# Implicit rule for value keeper structures compilation.
$(srcdir)/kpr/*.o : $(fld_deps)
$(srcdir)/kpr/%.dbg.o : $(srcdir)/kpr/%.c $(headersdir)/kpr/%.h
	$(call compile)
$(srcdir)/kpr/%.rel.o : $(srcdir)/kpr/%.c $(headersdir)/kpr/%.h
	$(call compile)

# Implicit rule for state fields structures compilation.
$(srcdir)/ste/*.o : $(fld_deps)
$(srcdir)/ste/%.dbg.o : $(srcdir)/ste/%.c $(headersdir)/ste/%.h
	$(call compile)
$(srcdir)/ste/%.rel.o : $(srcdir)/ste/%.c $(headersdir)/ste/%.h
	$(call compile)


# Built-in modules compilation.
$(srcdir)/lib/io.*.o : $(common_deps)
$(srcdir)/lib/io.*.o : $(headersdir)/type/string.h
$(srcdir)/lib/io.*.o : $(headersdir)/lib/io.h
$(srcdir)/lib/io.%.o : $(srcdir)/lib/io.c
	$(call compile)

# C compiler invocation routine.
define compile
	$(CC) -c $(CPPFLAGS) $(CFLAGS) \
		$(if $(findstring .dbg.,$@),$(debug_flags),$(release_flags)) \
		-o $@ $<
endef

# Tests
tests = gc

compile-tests : $(addprefix bin/,$(tests))

test : $(addprefix bin/,$(tests))
	@echo "===== EXECUTING TESTS ====="
	@for name in "$(tests)"; do \
		echo "=== Executing $${name}" >&2; \
		echo >&2; \
		( bin/$${name} && echo && echo "=== $${name} OK" >&2 ) \
		|| echo "=== $${name} FAILED" >&2;\
	done

$(addprefix bin/,$(tests)) : $(debug_so_lib)
bin/% : test/%.c
	mkdir -p bin
	$(CC) $(CPPFLAGS) $(CFLAGS) \
		$(debug_flags) \
		$(LDFLAGS) -pthread \
		-L$(target_dir) -lo42a_debug \
		-Wl,--no-undefined,-rpath=$(target_dir) -o $@ $<
