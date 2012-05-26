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
#ifndef O42A_VALUE_H
#define O42A_VALUE_H

#include "o42a/types.h"


#ifdef __cplusplus
extern "C" {
#endif

struct o42a_obj_data;

/**
 * Value flags.
 *
 * Used in o42a_val.flags field.
 */
enum o42a_val_flags {

	/**
	 * Value condition mask. The same as O42A_TRUE.
	 */
	O42A_VAL_CONDITION = O42A_TRUE,

	/**
	 * A bit meaning the value is not yet calculated.
	 */
	O42A_VAL_INDEFINITE = 2,

	/**
	 * A bit meaning the value assignment is in progress.
	 *
	 * This is only used by variables.
	 *
	 * When the code wishes to assign a new value, it should acquire the memory
	 * barrier and atomically set this flag. If the flag already set, then
	 * assignment should be canceled. Otherwise the code should:
	 * - modify the value,
	 * - release the memory barrier, and
	 * - atomically drop this flag.
	 */
	O42A_VAL_ASSIGN = 4,

	/**
	 * Value alignment mask.
	 *
	 * This is a number of bits to shift the 1 left to gain alignment in bytes.
	 *
	 * For strings alignment also means the number of bytes containing unicode
	 * character.
	 *
	 * Note, that it can be useful for both externally stored value and for
	 * variable-length value fully contained in o42a_val.value field,
	 * such as string.
	 */
	O42A_VAL_ALIGNMENT_MASK = 0x700,

	/**
	 * Value is stored externally.
	 *
	 * This means that o42a_val.value contains pointer to external storage
	 * and o42a_val.length contains data length in bytes.
	 */
	O42A_VAL_EXTERNAL = 0x800,

	/**
	 * Value is statically allocated.
	 *
	 * This is meaningful when value is stored externally.
	 *
	 * When value is stored externally, but this flag isn't set, the value
	 * is allocated with o42a_mem_alloc_* function and is part of memory block
	 * (o42a_mem_block_t).
	 */
	O42A_VAL_STATIC = 0x1000,

};


/**
 * Unified object value.
 */
typedef struct o42a_val {

	O42A_HEADER;

	/**
	 * Value flags.
	 *
	 * A bit-mask consisting of o42a_val_flags enum values.
	 */
	uint32_t flags;

	/**
	 * Value length.
	 *
	 * The length quantity depends on value type.
	 */
	uint32_t length;

	/**
	 * Contains plain value.
	 *
	 * The value type should be known to the user.
	 *
	 * This is only meaningful when value condition is true.
	 */
	union {

		/** 32-bit integer value. */
		int32_t v_int32;

		/** Integer value. */
		int64_t v_integer;

		/** Floating point value. */
		double v_float;

		/** Pointer to externally stored value, e.g. to string. */
		void *v_ptr;

	} value;

} o42a_val_t;


/**
 * Value type descriptor.
 */
typedef struct o42a_val_type {

	O42A_HEADER;

	/**
	 * Value type name in lower case.
	 */
	const char *name;

	/**
	 * An object value marker.
	 *
	 * This function is called during the garbage-collected object mark to
	 * mark all data blocks referenced by object value (see o42a_gc_desc.mark).
	 *
	 * \data sweeping object data.
	 */
	void (*mark) (struct o42a_obj_data *);

	/**
	 * An object value sweeper.
	 *
	 * This function is called during the garbage-collected object sweep to
	 * release the resources used by object value (see o42a_gc_desc.sweep).
	 *
	 * \data sweeping object data.
	 */
	void (*sweep) (struct o42a_obj_data *);

} o42a_val_type_t;

#ifdef NDEBUG

/**
 * Value type descriptor initializer macro.
 *
 * \param _name string containing the type name.
 * \param _mark mark function pointer.
 * \param _sweep sweep function pointer.
 */
#define O42A_VAL_TYPE(_type_name, _mark, _sweep) { \
	name: _type_name, \
	mark: _mark, \
	sweep: _sweep, \
}

#else /* NDEBUG */

#define O42A_VAL_TYPE(_type_name, _mark, _sweep) { \
	__o42a_dbg_header__: { \
		type_code: 0x042a0001, \
		enclosing: 0, \
		name: "o42a_val_type_" _type_name, \
		type_info : &_O42A_DEBUG_TYPE_o42a_val_type, \
	}, \
	name: _type_name, \
	mark: _mark, \
	sweep: _sweep, \
}

extern const o42a_dbg_type_info_t _O42A_DEBUG_TYPE_o42a_val_type;

#endif /* NDEBUG */

/**
 * Void value type descriptor.
 */
extern const o42a_val_type_t o42a_val_type_void;

/**
 * Directive value type descriptor.
 */
extern const o42a_val_type_t o42a_val_type_directive;


size_t o42a_val_ashift(const o42a_val_t *);

size_t o42a_val_alignment(const o42a_val_t *);

void *o42a_val_data(const o42a_val_t *);

void o42a_val_use(o42a_val_t *);

void o42a_val_unuse(o42a_val_t *);


/**
 * The object value marker which does nothing.
 */
void o42a_val_mark_none(struct o42a_obj_data *);

/**
 * The object value sweeper which does nothing.
 */
void o42a_val_sweep_none(struct o42a_obj_data *);

/**
 * The object value sweeper supporting the external, reference counted values.
 *
 * This function decreases the reference count of externally stored data
 * if O42A_VAL_CONDITION and O42A_VAL_EXTERNAL flags are set and O42A_VAL_STATIC
 * one is not. It deallocates the data if it is no longer used.
 */
void o42a_val_sweep_external(struct o42a_obj_data *);

#ifdef __cplusplus
} /* extern "C" */
#endif


#endif /* O42A_VALUE_H */
