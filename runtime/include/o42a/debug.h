/*
    Run-Time Library
    Copyright (C) 2010-2012 Ruslan Lopatin

    This file is part of o42a.

    o42a is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License
    as published by the Free Software Foundation, either version 3
    of the License, or (at your option) any later version.

    o42a is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
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

	const char *thread_name;

	o42a_dbg_stack_frame_t *stack_frame;

	FILE *output;

	uint8_t command;

	uint8_t indent;

};


typedef struct o42a_dbg_field_info {

	uint32_t data_type;

	o42a_rptr_t offset;

	const char *name;

	const o42a_dbg_type_info_t *type_info;

} o42a_dbg_field_info_t;

#define O42A_DBG_TYPE_INFO \
	int32_t type_code; \
	uint32_t field_num; \
	const char *name; \

/**
 * Run-time debug type info.
 */
struct o42a_dbg_type_info {

	/** Type code. */
	int32_t type_code;

	/** The number of fields. */
	uint32_t field_num;

	/* Type name. */
	const char *name;

	/**
	 * Array of type fields info.
	 *
	 * This array contains exactly field_num fields.
	 */
	o42a_dbg_field_info_t fields[];

};

/** Type info with one field. */
typedef struct o42a_dbg_type_info1f {
	O42A_DBG_TYPE_INFO
	o42a_dbg_field_info_t fields[1];
} o42a_dbg_type_info1f_t;

/** Type info with two fields. */
typedef struct o42a_dbg_type_info2f {
	O42A_DBG_TYPE_INFO
	o42a_dbg_field_info_t fields[2];
} o42a_dbg_type_info2f_t;

/** Type info with 3 fields. */
typedef struct o42a_dbg_type_info3f {
	O42A_DBG_TYPE_INFO
	o42a_dbg_field_info_t fields[3];
} o42a_dbg_type_info3f_t;

/** Type info with 4 fields. */
typedef struct o42a_dbg_type_info4f {
	O42A_DBG_TYPE_INFO
	o42a_dbg_field_info_t fields[4];
} o42a_dbg_type_info4f_t;

/** Type info with 5 fields. */
typedef struct o42a_dbg_type_info5f {
	O42A_DBG_TYPE_INFO
	o42a_dbg_field_info_t fields[5];
} o42a_dbg_type_info5f_t;


extern const o42a_dbg_type_info2f_t _O42A_DEBUG_TYPE_o42a_rlist;

void o42a_dbg_start_thread(struct o42a_dbg_env *);

int32_t o42a_dbg_exec_main(
		int32_t(*)(int32_t, char**), int32_t, char**);


const o42a_dbg_header_t *o42a_dbg_header(const void *);


void o42a_dbg_print(const char *);

void o42a_dbg_print_wo_prefix(const char *);

__attribute__ ((format(printf, 1, 2)))
void o42a_dbg_printf(const char *, ...);

void o42a_dbg_mem_name(const char *, const void *);

void o42a_dbg_func_name(const char *, const void *);

void o42a_dbg_dump_mem(const char *, const void *, uint32_t);


o42a_bool_t o42a_dbg_enter(o42a_dbg_stack_frame_t *);

void o42a_dbg_exit();

void o42a_dbg_do(o42a_dbg_stack_frame_t *, const char *comment);

void o42a_dbg_done(uint32_t line);

void o42a_dbg_set_line(uint32_t line);


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
} /* extern "C" */
#endif

#endif /* O42A_DEBUG_H */
