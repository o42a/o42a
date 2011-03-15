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

#include "o42a/types.h"

#include <stdio.h>


typedef struct o42a_dbg_field_info {

	uint32_t data_type;

	o42a_rptr_t offset;

	const char *name;

	const o42a_dbg_type_info_t *type_info;

} o42a_dbg_field_info_t;

struct o42a_dbg_type_info {

	int32_t type_code;

	uint32_t field_num;

	const char *name;

	o42a_dbg_field_info_t fields[];

};


typedef const struct o42a_dbg_func o42a_dbg_func_t;
typedef const struct o42a_dbg_stack_frame o42a_dbg_stack_frame_t;

struct o42a_dbg_func {
	char *name;
	void *function;
};

struct o42a_dbg_stack_frame {
	const char *name;
	o42a_dbg_stack_frame_t *prev;
};


extern const struct o42a_dbg_info {
	o42a_dbg_func_t *functions;
	uint32_t num_functions;
} o42a_debug_info;

extern const struct {

	const o42a_dbg_type_info_t *rtype_type_info;

} o42a_dbg;


#ifdef __cplusplus
extern "C" {
#endif


int32_t o42a_dbg_exec_main(int32_t(*)(int32_t, char**), int32_t, char**);


const o42a_dbg_header_t *o42a_dbg_header(const void *);


void o42a_dbg_print(const char *);

void o42a_dbg_mem_name(const char *, const void *);

void o42a_dbg_func_name(const char *, const void *);

void o42a_dbg_dump_mem(const void *, uint32_t);

const o42a_dbg_func_t *o42a_dbg_func(const void *);


void o42a_dbg_enter(struct o42a_dbg_stack_frame *);

void o42a_dbg_exit();

o42a_dbg_stack_frame_t *o42a_dbg_stack();

void o42a_dbg_print_stack_frame(o42a_dbg_stack_frame_t *);

void o42a_dbg_print_stack_trace(o42a_dbg_stack_frame_t *);


void o42a_dbg_fill_header(
		const o42a_dbg_type_info_t *,
		o42a_dbg_header_t *,
		const o42a_dbg_header_t *);

void o42a_dbg_copy_header(
		const o42a_dbg_header_t *,
		o42a_dbg_header_t *,
		const o42a_dbg_header_t *);

void o42a_dbg_fill_field_info(
		const o42a_dbg_header_t *,
		o42a_dbg_field_info_t *);


#ifdef __cplusplus
}
#endif

#endif /* O42A_DEBUG_H */
