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

struct o42a_obj;

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
	 * Eagerly evaluated value bit.
	 *
	 * This is set when object's value is eagerly evaluated.
	 */
	O42A_VAL_EAGER = 4,

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
	 * Value copy function pointer.
	 *
	 * \param from[in] value to copy contents from. Never false.
	 * \param to value to copy contents to.
	 */
	void (* copy) (const o42a_val_t *, o42a_val_t *);

	/**
	 * Value use function pointer.
	 *
	 * It should copy the value in a thread-safe manner and mark it used.
	 *
	 * \param from[in] value to copy contents from. Never false.
	 * \param to value to copy contents to.
	 */
	void (* use) (const o42a_val_t *, o42a_val_t *);

	/**
	 * Value discard function pointer.
	 *
	 * This is used e.g. when evaluating an object condition to discard
	 * the value returned from object value evaluation function.
	 *
	 * \param value the value to discard. Never false.
	 */
	void (* discard) (const o42a_val_t *);

	/**
	 * GC marker function pointer.
	 *
	 * This function is called when GC marks an object to mark the GC data
	 * referenced by its value.
	 *
	 * \param data marked object data pointer.
	 */
	void (* mark) (struct o42a_obj *);

	/**
	 * GC sweep function pointer.
	 *
	 * This function is called when GC sweeps an object to sweep the GC data
	 * referenced by its value.
	 *
	 * \param data swept object data pointer.
	 */
	void (* sweep) (struct o42a_obj *);

} o42a_val_type_t;

#ifdef NDEBUG

/**
 * Value type descriptor initializer macro.
 *
 * \param _name string containing the type name.
 * \param _copy value copy function pointer.
 * \param _copy value use function pointer.
 * \param _discard value discard function pointer.
 * \param _mark mark function pointer.
 * \param _sweep sweep function pointer.
 */
#define O42A_VAL_TYPE(_name, _copy, _use, _discard, _mark, _sweep) { \
	.name = _name, \
	.copy = _copy, \
	.use = _use, \
	.discard = _discard, \
	.mark = _mark, \
	.sweep = _sweep, \
}

#else /* NDEBUG */

#define O42A_VAL_TYPE(_name, _copy, _use, _discard, _mark, _sweep) { \
	.__o42a_dbg_header__ = { \
		.type_code = 0x042a0003, \
		.enclosing = 0, \
		.name = "o42a_val_type_" _name, \
		.type_info = (o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_val_type, \
	}, \
	.name = _name, \
	.copy = _copy, \
	.use = _use, \
	.discard = _discard, \
	.mark = _mark, \
	.sweep = _sweep, \
}

extern const o42a_dbg_type_info3f_t _O42A_DEBUG_TYPE_o42a_val;

extern const struct _O42A_DEBUG_TYPE_o42a_val_type {
	O42A_DBG_TYPE_INFO
	o42a_dbg_field_info_t fields[6];
} _O42A_DEBUG_TYPE_o42a_val_type;

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

/**
 * Copies the value as is without any additional actions.
 *
 * \param from[in] value to copy contents from.
 * \param to value to copy contents to.
 */
void o42a_val_copy_as_is(const o42a_val_t *, o42a_val_t *);

/**
 * Value discard function, which does nothing.
 *
 * Used for for simple types.
 */
void o42a_val_discard_none(const o42a_val_t *);

/**
 * Garbage-collected value discard function.
 */
void o42a_val_discard_gc(const o42a_val_t *);

void o42a_val_use(const o42a_val_t *);

void o42a_val_unuse(const o42a_val_t *);

void o42a_val_gc_none(struct o42a_obj *);

#ifdef __cplusplus
} /* extern "C" */
#endif


#endif /* O42A_VALUE_H */
