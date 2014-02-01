/*
    Copyright (C) 2010-2014 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
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
	 * Stateless value bit.
	 *
	 * This is set when the object value is never meant to be stored.
	 */
	O42A_VAL_STATELESS = 4,

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

	O42A_HEADER

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

	O42A_HEADER

	/**
	 * Value type name in lower case.
	 */
	const char *name;

	/**
	 * GC marker function pointer.
	 *
	 * This function is called when GC marks an object to mark the GC data
	 * referenced by its value.
	 *
	 * \param data marked object data pointer.
	 */
	void (* mark) (struct o42a_obj_data *);

	/**
	 * GC sweep function pointer.
	 *
	 * This function is called when GC sweeps an object to sweep the GC data
	 * referenced by its value.
	 *
	 * \param data swept object data pointer.
	 */
	void (* sweep) (struct o42a_obj_data *);

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
	.name = _type_name, \
	.mark = _mark, \
	.sweep = _sweep, \
}

#else /* NDEBUG */

#define O42A_VAL_TYPE(_type_name, _mark, _sweep) { \
	.__o42a_dbg_header__ = { \
		.type_code = 0x042a0003, \
		.enclosing = 0, \
		.name = "o42a_val_type_" _type_name, \
		.type_info = (o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_val_type, \
	}, \
	.name = _type_name, \
	.mark = _mark, \
	.sweep = _sweep, \
}

extern const o42a_dbg_type_info3f_t _O42A_DEBUG_TYPE_o42a_val;

extern const o42a_dbg_type_info3f_t _O42A_DEBUG_TYPE_o42a_val_type;

#endif /* NDEBUG */

/**
 * Void value type descriptor.
 */
extern const o42a_val_type_t o42a_val_type_void;

/**
 * Directive value type descriptor.
 */
extern const o42a_val_type_t o42a_val_type_directive;

/**
 * Macro value type descriptor.
 */
extern const o42a_val_type_t o42a_val_type_macro;


inline size_t o42a_val_ashift(const o42a_val_t *const val) {
	return (val->flags & O42A_VAL_ALIGNMENT_MASK) >> 8;
}

inline size_t o42a_val_alignment(const o42a_val_t *const val) {
	return 1 << o42a_val_ashift(val);
}

inline void *o42a_val_data(const o42a_val_t *const val) {
	if (val->flags & O42A_VAL_EXTERNAL) {
		return val->value.v_ptr;
	}
	return (void*) &val->value;
}

void o42a_val_use(o42a_val_t *);

void o42a_val_unuse(o42a_val_t *);

void o42a_val_gc_none(struct o42a_obj_data *);

#ifdef __cplusplus
} /* extern "C" */
#endif


#endif /* O42A_VALUE_H */
