/*
    Copyright (C) 2010-2014 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#ifndef O42A_DEBUG_H
#define O42A_DEBUG_H

#include "o42a/types.h"

#include <stdio.h>


#ifdef __cplusplus
extern "C" {
#endif


#ifndef O42A_DEBUG_TYPE
#define O42A_DEBUG_TYPE O42A_DBG_TYPE_DEFAULT
#endif


#define O42A_HEADER_SIZE sizeof(o42a_dbg_header_t)

#define O42A(exp) (o42a_dbg_set_line(__LINE__), exp)

#define O42A_START_THREAD(_thread_name) \
	struct o42a_dbg_env __thread_dbg_env__ = { \
		.thread_name = (_thread_name), \
		.stack_frame = NULL, \
		.command = O42A_DBG_CMD_EXEC, \
		.indent = 0, \
	}; \
	o42a_dbg_start_thread(&__thread_dbg_env__)

#define O42A_ENTER(return_null) \
	struct o42a_dbg_stack_frame __o42a_dbg_stack_frame__ = { \
		.name = __func__, \
		.comment = NULL, \
		.file = __FILE__, \
		.line = __LINE__, \
	}; \
	if (!o42a_dbg_enter(&__o42a_dbg_stack_frame__)) { \
		return_null; \
	}

#define O42A_RETURN O42A(o42a_dbg_exit()); return

#define o42a_debug_ison o42a_dbg_ison(O42A_DEBUG_TYPE)

#define O42A_DEBUG(format, ...) \
	if (o42a_debug_ison) o42a_dbg_printf(format, ## __VA_ARGS__)

#define _O42A_DO_(_sf, _comment) \
	o42a_dbg_stack_frame_t _sf = { \
		.comment = NULL, \
		.file = __FILE__, \
		.line = __LINE__, \
	}; \
	o42a_dbg_do(&_sf, _comment)

#define __O42A_DO(_sf, _sfend, _comment) \
	_O42A_DO_(__o42a_dbg_stack_frame_##_sf##_sfend, _comment)

#define _O42A_DO(_sf, _sfend, _comment) \
	__O42A_DO(_sf, _sfend, _comment)

#define O42A_DO(comment) _O42A_DO(__LINE__, __, (comment))

#define O42A_DONE o42a_dbg_done(__LINE__)

#define o42a_debug(message) \
	if (o42a_debug_ison) O42A(o42a_dbg_print(message))

#define o42a_debug_mem_name(prefix, ptr) \
	if (o42a_debug_ison) O42A(o42a_dbg_mem_name(prefix, ptr))

#define o42a_debug_func_name(prefix, ptr) \
	if (o42a_debug_ison) O42A(o42a_dbg_func_name(prefix, ptr))

#define o42a_debug_dump_mem(prefix, ptr, depth) \
	if (o42a_debug_ison) O42A(o42a_dbg_dump_mem(prefix, ptr, depth))


enum o42a_dbg_types {

	O42A_DBG_TYPE_ALL = ~0,
	O42A_DBG_TYPE_DEFAULT = 0x1,
	O42A_DBG_TYPE_GC = 0x2,

};

enum o42a_dbg_commands {

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

typedef struct o42a_dbg_options {

	o42a_bool_t quiet;

	o42a_bool_t no_debug_messages;

	o42a_bool_t debug_blocks_omitted;

	o42a_bool_t silent_calls;

} o42a_dbg_options_t;

struct o42a_dbg_env {

	const char *thread_name;

	o42a_dbg_stack_frame_t *stack_frame;

	FILE *output;

	uint8_t command;

	uint8_t indent;

	o42a_dbg_options_t options;

	uint32_t enabled_debug_types;

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

/**
 * A dump of the call stack.
 *
 * Can be obtained with o42a_dbg_stack_dump.
 */
typedef struct o42a_dbg_stack_dump {

	/**
	 * The size of the dump, i.e. a number of bytes to allocate
	 * before attempting to fill it with o42a_dbg_fill_stack_dump.
	 */
	size_t size;

	/**
	 * A top-level stack frame.
	 */
	const o42a_dbg_stack_frame_t *stack_frame;

} o42a_dbg_stack_dump_t;

extern const o42a_dbg_type_info2f_t _O42A_DEBUG_TYPE_o42a_rlist;

extern const o42a_dbg_options_t o42a_dbg_default_options;

o42a_bool_t o42a_dbg_ison(uint32_t);

void o42a_dbg_start_thread(struct o42a_dbg_env *);

int32_t o42a_dbg_exec_main(
		int32_t(*)(int32_t, char**), int32_t, char**);


const o42a_dbg_header_t *o42a_dbg_header(const void *);


void o42a_dbg_print(const char *);

void o42a_dbg_print_nl(const char *);

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

/**
 * Creates a dump of the call stack.
 *
 * \param skip_frame number of frames to skip.
 */
o42a_dbg_stack_dump_t o42a_dbg_stack_dump(size_t);

/**
 * Fills a dump of the stack with data.
 *
 * \param in stack_dump a pointer to the stack dump previously created by
 * o42a_dbg_stack_dump.
 * \param data a pointer to the memory to fill. The size of this memory block
 * should be at least stack_dump->size bytes.
 */
void o42a_dbg_fill_stack_dump(const o42a_dbg_stack_dump_t *, void *);

/**
 * Prints a stack frame data to the given file.
 *
 * \param data stack dump data filled with o42a_dbg_fill_stack_dump.
 */
void o42a_dbg_print_stack_dump(void *);

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* O42A_DEBUG_H */
