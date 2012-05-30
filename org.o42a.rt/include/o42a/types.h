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

#ifdef __cplusplus
extern "C" {
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
		__alignof__ (target), \
		sizeof (target))

enum o42a_data_types {

	O42A_TYPE_VOID = ~0x7fffffff,
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


#define O42A_HEADER o42a_dbg_header_t __o42a_dbg_header__;

#define O42A_HEADER_SIZE sizeof(o42a_dbg_header_t)


#define O42A(exp) (o42a_dbg_set_line(__LINE__), exp)


#define O42A_START_THREAD \
	struct o42a_dbg_env __thread_dbg_env__ = { \
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

#define O42A_DEBUG(format, ...) \
	o42a_dbg_printf(format, ## __VA_ARGS__)

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


#define o42a_debug(message) O42A(o42a_dbg_print(message))

#define o42a_debug_mem_name(prefix, ptr) \
	O42A(o42a_dbg_mem_name(prefix, ptr))

#define o42a_debug_func_name(prefix, ptr) \
	O42A(o42a_dbg_func_name(prefix, ptr))

#define o42a_debug_dump_mem(prefix, ptr, depth) \
	O42A(o42a_dbg_dump_mem(prefix, ptr, depth))


#include "o42a/debug.h"


#endif /* NDEBUG */


/**
 * Relative pointer to list of known length.
 */
typedef struct o42a_rlist {

	O42A_HEADER

	/** Relative pointer to the first element of list. */
	o42a_rptr_t list;

	/** The number of list elements. */
	uint32_t size;

} o42a_rlist_t;


enum o42a_layout_masks {

	/**
	 * Size bit-mask within layout value.
	 *
	 * Highest three bits contains the number of bits to shift 1 left to gain
	 * alignment. The rest is size shifted right by value stored in highest
	 * three bits. I.e. size is always a multiple of alignment.
	 */
	O42A_LAYOUT_SIZE_MASK = 0x1FFFFFFF,

	O42A_LAYOUT_ALIGNMENT_MASK = (~0) & ~0x1FFFFFFF

};

inline size_t o42a_layout_size(const o42a_layout_t layout) {
	return layout & O42A_LAYOUT_SIZE_MASK;
}

inline uint8_t o42a_layout_ashift(const o42a_layout_t layout) {
	return (layout & O42A_LAYOUT_ALIGNMENT_MASK) >> 29;
}

inline uint8_t o42a_layout_alignment(const o42a_layout_t layout) {
	return 1 << o42a_layout_ashift(layout);
}

inline size_t o42a_layout_offset(
		const size_t start,
		const o42a_layout_t layout) {

	const uint8_t ashift = o42a_layout_ashift(layout);
	const size_t remainder = start & ~((~0) << ashift);

	return remainder ? (1 << ashift) - remainder : 0;
}

inline size_t o42a_layout_pad(const size_t start, const o42a_layout_t layout) {
	return start + o42a_layout_offset(start, layout);
}

inline size_t o42a_layout_array_size(
		const o42a_layout_t layout,
		const size_t num_elements) {

	const size_t element_size =
			o42a_layout_pad(o42a_layout_size(layout), layout);

	return element_size * num_elements;
}

inline o42a_layout_t o42a_layout_array(
		const o42a_layout_t layout,
		const size_t num_elements) {
	return o42a_layout_array_size(layout, num_elements)
			| (layout & O42A_LAYOUT_ALIGNMENT_MASK);
}

inline o42a_layout_t o42a_layout(
		const uint8_t alignment,
		const size_t size) {

	uint8_t ashift = 0;
	const int diff = ((int) alignment) - 4;

	if (diff <= 0) {
		if (!diff) {
			ashift = 2;
		} else if (alignment == 2) {
			ashift = 1;
		}
	} else {
		switch (alignment) {
		case 8: ashift = 3; break;
		case 16: ashift = 4; break;
		case 32: ashift = 5; break;
		case 64: ashift = 6; break;
		case 128: ashift = 7; break;
		}
	}

	return size | (ashift << 29);
}

inline o42a_layout_t o42a_layout_both(
		const o42a_layout_t layout1,
		const o42a_layout_t layout2) {

	const uint8_t al1 = o42a_layout_alignment(layout1);
	const uint8_t al2 = o42a_layout_alignment(layout2);

	return o42a_layout(
			al1 >= al2 ? al1 : al2,
			o42a_layout_pad(o42a_layout_size(layout1), layout2)
			+ o42a_layout_size(layout2));
}

void o42a_init();

#ifdef __cplusplus
} /* extern "C" */
#endif


#endif /* O42A_TYPES_H */
