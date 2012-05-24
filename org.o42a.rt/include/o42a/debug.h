/*
    Run-Time Library
    Copyright (C) 2010-2012 Ruslan Lopatin

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


#ifdef __cplusplus
extern "C" {
#endif


enum {

	O42A_DBG_CMD_EXEC = 0,
	O42A_DBG_CMD_REPORT = 1,

};


typedef struct o42a_dbg_stack_frame o42a_dbg_stack_frame_t;

struct o42a_dbg_stack_frame {

	const char *name;

	o42a_dbg_stack_frame_t *prev;

	const char *comment;

	const char *file;

	uint32_t line;

};

struct o42a_dbg_env {

	o42a_dbg_stack_frame_t *stack_frame;

	uint8_t command;

	uint8_t indent;

};


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


extern const struct o42a_dbg {

	const o42a_dbg_type_info_t *rtype_type_info;

} o42a_dbg;


void o42a_dbg_start_thread(struct o42a_dbg_env *);

int32_t o42a_dbg_exec_main(
		int32_t(*)(O42A_DECLS int32_t, char**), int32_t, char**);


const o42a_dbg_header_t *o42a_dbg_header(const void *);


void o42a_dbg_print(O42A_DECLS const char *);

void o42a_dbg_print_wo_prefix(O42A_DECLS const char *);

__attribute__ ((format(printf, O42A_ARGC + 1, O42A_ARGC + 2)))
void o42a_dbg_printf(O42A_DECLS const char *, ...);

void o42a_dbg_mem_name(O42A_DECLS const char *, const void *);

void o42a_dbg_func_name(O42A_DECLS const char *, const void *);

void o42a_dbg_dump_mem(O42A_DECLS const char *, const void *, uint32_t);

o42a_bool_t o42a_dbg_exec_command(o42a_dbg_env_t *);


void o42a_dbg_enter(o42a_dbg_env_t *);

void o42a_dbg_exit(o42a_dbg_env_t *);


void o42a_dbg_print_stack_frame(o42a_dbg_stack_frame_t *);

void o42a_dbg_print_stack_trace(o42a_dbg_stack_frame_t *);


void o42a_dbg_fill_header(
		O42A_DECLS
		const o42a_dbg_type_info_t *,
		o42a_dbg_header_t *,
		const o42a_dbg_header_t *);

void o42a_dbg_copy_header(
		O42A_DECLS
		const o42a_dbg_header_t *,
		o42a_dbg_header_t *,
		const o42a_dbg_header_t *);

void o42a_dbg_fill_field_info(
		O42A_DECLS
		const o42a_dbg_header_t *,
		o42a_dbg_field_info_t *);


#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* O42A_DEBUG_H */
