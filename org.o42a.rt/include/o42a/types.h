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
#ifndef O42A_TYPES_H
#define O42A_TYPES_H

#include <stddef.h>
#include <stdint.h>


#ifndef __cplusplus
#ifndef offsetof
#define offsetof(TYPE, MEMBER)  __builtin_offsetof (TYPE, MEMBER)
#endif
#endif


/** Relative pointer. */
typedef int32_t o42a_rptr_t;

/**
 * Possible o42a_bool_t values.
 */
enum o42a_bool_values {

	/** False. */
	O42A_FALSE = 0,

	/** True. */
	O42A_TRUE = 1

};

/**
 * Boolean type.
 *
 * Possible values are O42A_TRUE and O42A_FALSE.
 */
typedef uint8_t o42a_bool_t;

/**
 * Data allocation.
 *
 * This indicates data allocation size and alignment.
 */
typedef uint32_t o42a_layout_t;

#define O42A_LAYOUT(target) o42a_layout( \
		O42A_ARGS \
		__alignof__ (target), \
		sizeof (target))

enum o42a_data_types {

	O42A_TYPE_VOID = 0x80000000,
	O42A_TYPE_STRUCT = 0,
	O42A_TYPE_SYSTEM = 0x10,
	O42A_TYPE_REL_PTR = 0x02,
	O42A_TYPE_PTR = 0x12,
	O42A_TYPE_SYS_PTR = 0x22,
	O42A_TYPE_DATA_PTR = 0x32,
	O42A_TYPE_CODE_PTR = 0x42,
	O42A_TYPE_FUNC_PTR = 0x52,
	O42A_TYPE_BOOL = 0x01,
	O42A_TYPE_INT8 = 0x11,
	O42A_TYPE_INT16 = 0x11 | (1 << 8),
	O42A_TYPE_INT32 = 0x11 | (2 << 8),
	O42A_TYPE_INT64 = 0x11 | (3 << 8),
	O42A_TYPE_FP32 = 0x21 | (2 << 8),
	O42A_TYPE_FP64 = 0x21 | (3 << 8),

};


typedef struct o42a_dbg_env o42a_dbg_env_t;
typedef struct o42a_dbg_type_info o42a_dbg_type_info_t;

typedef struct __attribute__ ((__packed__)) o42a_dbg_header {

	int32_t type_code;

	o42a_rptr_t enclosing;

	const char *name;

	const o42a_dbg_type_info_t *type_info;

} o42a_dbg_header_t;


#ifdef NDEBUG


#define O42A_HEADER

#define O42A_HEADER_SIZE 0


#define O42A(exp) (exp)

#define O42A_ARGC 0

#define O42A_DECL

#define O42A_DECLS

#define O42A_PARAM

#define O42A_PARAMS

#define O42A_ARG

#define O42A_ARGS

#define O42A_ARG_

#define O42A_ARGS_


#define O42A_START_THREAD

#define O42A_ENTER(return_null)

#define O42A_RETURN return

#define O42A_DEBUG(format, ...)

#define O42A_DO(comment)

#define O42A_DONE


#define o42a_debug(message)

#define o42a_debug_mem_name(prefix, ptr)

#define o42a_debug_func_name(prefix, ptr)

#define o42a_debug_dump_mem(prefix, ptr, depth)


#define __o42a_dbg_env_p__ ((o42a_dbg_env_t*) NULL)

#else /* NDEBUG */


#define O42A_HEADER o42a_dbg_header_t __o42a_dbg_header__

#define O42A_HEADER_SIZE sizeof(o42a_dbg_header_t)


#define O42A(exp) ( \
		__o42a_dbg_env__->stack_frame->line = __LINE__, \
		exp \
	)

#define O42A_ARGC 1

#define O42A_DECL o42a_dbg_env_t *

#define O42A_DECLS O42A_DECL,

#define O42A_PARAM o42a_dbg_env_t *const __o42a_dbg_env_p__

#define O42A_PARAMS O42A_PARAM,

#define O42A_ARG __o42a_dbg_env__

#define O42A_ARGS O42A_ARG,

#define O42A_ARG_ __o42a_dbg_env_p__

#define O42A_ARGS_ O42A_ARG_,


#define O42A_START_THREAD \
	struct o42a_dbg_env __thread_dbg_env__ = { \
		stack_frame: NULL, \
		command: O42A_DBG_CMD_EXEC, \
		indent: 0, \
	}; \
	o42a_dbg_env_t *__o42a_dbg_env_p__ = &__thread_dbg_env__; \
	o42a_dbg_start_thread(&__thread_dbg_env__)

#define O42A_ENTER(return_null) \
	o42a_dbg_env_t *const __o42a_dbg_env__ = __o42a_dbg_env_p__; \
	struct o42a_dbg_stack_frame __o42a_dbg_stack_frame__ = { \
		name: __func__, \
		prev: __o42a_dbg_env__->stack_frame, \
		comment: NULL, \
		file: __FILE__, \
		line: __LINE__, \
	}; \
	__o42a_dbg_env__->stack_frame = &__o42a_dbg_stack_frame__; \
	if (o42a_dbg_exec_command(__o42a_dbg_env__)) { \
		return_null; \
	} \
	o42a_dbg_enter(O42A_ARG)

#define O42A_RETURN O42A(o42a_dbg_exit(__o42a_dbg_env__)); return

#define O42A_DEBUG(format, args...) \
	o42a_dbg_printf(O42A_ARGS format, ## args)

#define _O42A_DO_(_sf, _comment) \
	__o42a_dbg_env__->stack_frame->line = __LINE__; \
	__o42a_dbg_env__->stack_frame->comment = _comment; \
	o42a_dbg_stack_frame_t _sf = { \
		name: __func__, \
		prev: __o42a_dbg_env__->stack_frame, \
		comment: NULL, \
		file: __FILE__, \
		line: __LINE__, \
	}; \
	o42a_dbg_printf( \
			O42A_ARGS \
			"((( /* %s */ (%s:%lu)\n", \
			__o42a_dbg_env__->stack_frame->comment, \
			__FILE__, \
			(unsigned long) __LINE__); \
	__o42a_dbg_env__->stack_frame = &_sf; \
	++__o42a_dbg_env__->indent

#define __O42A_DO(_sf, _sfend, _comment) \
	_O42A_DO_(__o42a_dbg_stack_frame_##_sf##_sfend, _comment)

#define _O42A_DO(_sf, _sfend, _comment) \
	__O42A_DO(_sf, _sfend, _comment)

#define O42A_DO(comment) _O42A_DO(__LINE__, __, (comment))

#define O42A_DONE \
	do { \
		--__o42a_dbg_env__->indent; \
		o42a_dbg_stack_frame_t *const _prev = \
				__o42a_dbg_env__->stack_frame->prev; \
		o42a_dbg_printf( \
				O42A_ARGS \
				"))) /* %s */ (%s:%lu)\n", \
				_prev->comment, \
				__FILE__, \
				(unsigned long) __LINE__); \
		_prev->line = __LINE__; \
		_prev->comment = NULL; \
		__o42a_dbg_env__->stack_frame = _prev; \
	} while (0)


#define o42a_debug(message) O42A(o42a_dbg_print(O42A_ARGS message))

#define o42a_debug_mem_name(prefix, ptr) \
	O42A(o42a_dbg_mem_name(O42A_ARGS prefix, ptr))

#define o42a_debug_func_name(prefix, ptr) \
	O42A(o42a_dbg_func_name(O42A_ARGS prefix, ptr))

#define o42a_debug_dump_mem(prefix, ptr, depth) \
	O42A(o42a_dbg_dump_mem(O42A_ARGS prefix, ptr, depth))


#include "o42a/debug.h"


#endif /* NDEBUG */


/**
 * Relative pointer to list of known length.
 */
typedef struct o42a_rlist {

	O42A_HEADER;

	/** Relative pointer to the first element of list. */
	o42a_rptr_t list;

	/** The number of list elements. */
	uint32_t size;

} o42a_rlist_t;


#ifdef __cplusplus
extern "C" {
#endif

size_t o42a_layout_size(O42A_DECLS o42a_layout_t);

size_t o42a_layout_array_size(O42A_DECLS o42a_layout_t, size_t);

o42a_layout_t o42a_layout_array(O42A_DECLS o42a_layout_t, size_t);

uint8_t o42a_layout_alignment(O42A_DECLS o42a_layout_t);

size_t o42a_layout_offset(O42A_DECLS size_t, o42a_layout_t);

size_t o42a_layout_pad(O42A_DECLS size_t, o42a_layout_t);

o42a_layout_t o42a_layout_both(O42A_DECLS o42a_layout_t, o42a_layout_t);

o42a_layout_t o42a_layout(O42A_DECLS uint8_t, size_t);

void o42a_init(O42A_DECL);

#ifdef __cplusplus
} /* extern "C" */
#endif


#endif /* O42A_TYPES_H */
