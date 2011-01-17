/*
    Run-Time Library
    Copyright (C) 2010,2011 Ruslan Lopatin

    This file is part of o42a.

    o42a is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    o42a is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
#ifndef O42A_DEBUG_H
#define O42A_DEBUG_H

#include <assert.h>


#ifdef NDEBUG


#define O42A_ENTER

#define O42A_RETURN return

#define O42A_DEBUG(format, args...)


#define o42a_debug(message)

#define o42a_debug_mem_name(prefix, ptr)

#define o42a_debug_func_name(prefix, ptr);

#define o42a_dbg_dump_mem(ptr, depth)

#define o42a_dbg_dump_field(ptr, field, depth)

#define o42a_dbg_print_stack_trace(dump)


#else


#include "o42a/types.h"

#include <stdio.h>
#include <wchar.h>


#define O42A_ENTER \
	struct o42a_dbg_stack_frame __o42a_dbg_stack_frame__ = {__func__, NULL}; \
	o42a_dbg_enter(&__o42a_dbg_stack_frame__)

#define O42A_RETURN o42a_dbg_exit(); return

#define O42A_DEBUG(format, args...) \
	fwprintf(stderr, L"[%s] ", o42a_dbg_stack()->name); \
	fwprintf(stderr, format, ## args)


typedef const struct o42a_dbg_func o42a_dbg_func_t;
typedef const struct o42a_dbg_field o42a_dbg_field_t;
typedef const struct o42a_dbg_struct o42a_dbg_struct_t;
typedef const struct o42a_dbg_global o42a_dbg_global_t;
typedef const struct o42a_dbg_stack_frame o42a_dbg_stack_frame_t;

struct o42a_dbg_func {
	char *name;
	void *function;
};

struct o42a_dbg_field {
	char *name;
	o42a_dbg_struct_t *dbg_struct;
	uint32_t data_type;
	o42a_rptr_t offset;
};

struct o42a_dbg_struct {
	char *name;
	o42a_layout_t layout;
	uint32_t size;
	o42a_dbg_field_t fields[0];
};

struct o42a_dbg_global {
	char *name;
	void *start;
	o42a_dbg_field_t content;
};

struct o42a_dbg_stack_frame {
	const char *name;
	o42a_dbg_stack_frame_t *prev;
};


extern const struct o42a_dbg_info {
	o42a_dbg_func_t *functions;
	o42a_dbg_global_t *globals;
	uint32_t num_functions;
	uint32_t num_globals;
} o42a_debug_info;


#ifdef __cplusplus
extern "C" {
#endif


int32_t o42a_dbg_exec_main(int32_t(*)(int32_t, char**), int32_t, char**);

void o42a_debug(const wchar_t*);

void o42a_debug_mem_name(const wchar_t*, const void*);

void o42a_debug_func_name(const wchar_t*, const void*);

void o42a_dbg_dump_mem(const void*, uint32_t);

void o42a_dbg_dump_field(const void*, o42a_dbg_field_t*, uint32_t);

void o42a_dbg_dump_struct(const void*, o42a_dbg_struct_t*, uint32_t);

const o42a_dbg_func_t *o42a_dbg_func(const void*);

const o42a_dbg_global_t *o42a_dbg_mem(const void*, o42a_dbg_field_t**);

const o42a_dbg_field_t *o42a_dbg_field(const void*);

const o42a_dbg_field_t *o42a_dbg_subfield(o42a_dbg_field_t*, ...);

void o42a_dbg_enter(struct o42a_dbg_stack_frame*);

void o42a_dbg_exit();

o42a_dbg_stack_frame_t* o42a_dbg_stack();

void o42a_dbg_print_stack_frame(o42a_dbg_stack_frame_t*);

void o42a_dbg_print_stack_trace(o42a_dbg_stack_frame_t*);


#ifdef __cplusplus
}
#endif

#endif

#endif
