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
#ifndef O42A_TYPES_H
#define O42A_TYPES_H

#include <stddef.h>
#include <stdint.h>


typedef struct o42a_obj_body o42a_obj_body_t;
typedef struct o42a_obj_methods o42a_obj_methods_t;
typedef struct o42a_obj_data o42a_obj_data_t;
typedef union o42a_obj_type o42a_obj_type_t;
typedef struct o42a_obj_stype o42a_obj_stype_t;
typedef struct o42a_obj_rtype o42a_obj_rtype_t;
typedef struct o42a_obj_ascendant o42a_obj_ascendant_t;
typedef struct o42a_obj_sample o42a_obj_sample_t;
typedef const struct o42a_obj_field o42a_obj_field_t;
typedef const struct o42a_obj_overrider o42a_obj_overrider_t;
typedef struct o42a_obj_ctable o42a_obj_ctable_t;
struct o42a_fld_obj;
typedef union o42a_fld o42a_fld;

/** Object represented by it's body. */
typedef o42a_obj_body_t o42a_obj_t;

/** Relative pointer. */
typedef int32_t o42a_rptr_t;

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

/**
 * Relative pointer to list of known length.
 */
typedef struct o42a_rlist {

	/** Relative pointer to the first element of list. */
	o42a_rptr_t list;

	/** The number of list elements. */
	uint32_t size;

} o42a_rlist_t;

enum o42a_data_types {

	O42A_TYPE_STRUCT = 0,
	O42A_TYPE_REL_PTR = 0x12,
	O42A_TYPE_DATA_PTR = 0x22,
	O42A_TYPE_CODE_PTR = 0x32,
	O42A_TYPE_INT32 = 0x11 | (4 << 8),
	O42A_TYPE_INT64 = 0x11 | (8 << 8),
	O42A_TYPE_FP64 = 0x21 | (8 << 8),

};

/**
 * Value flags.
 *
 * Used in o42a_val.flags field.
 */
enum o42a_val_flags {

	/**
	 * Value condition is false, which means that value does not exist.
	 */
	O42A_FALSE = 0,

	/**
	 * Value condition is true, which means that value exists.
	 */
	O42A_TRUE = 1,

	/**
	 * If value condition is false (O42A_FALSE), then this bit set means that
	 * the value is unknown and expected to be computed later.
	 */
	O42A_UNKNOWN = 2,

	/**
	 * A bit meaning the value is not yet calculated. This is only applicable to
	 * o42a_obj_data.value and should be set along with O42A_UNKNOWN.
	 */
	O42A_INDEFINITE = 4,

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

};


/**
 * Unified object value.
 */
typedef struct o42a_val {

	/**
	 * Value flags.
	 *
	 * A bit-mask consisting of o42a_val_flags enum values.
	 */
	uint32_t flags;

	/**
	 * Value length in bytes.
	 *
	 * This can be meaningful for externally stored values and for
	 * variable-length value, fully contained in value field, such as string.
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
 * Object value calculator function.
 *
 * \param result[out] object value to fill by function.
 * \param object[in] object pointer.
 */
typedef void o42a_obj_val_ft(o42a_val_t*, o42a_obj_t *);

/**
 * Object condition calculator function.
 *
 * \param object[in] object pointer.
 *
 * \return condition.
 */
typedef o42a_bool_t o42a_obj_cond_ft(o42a_obj_t *);

/**
 * Object reference function.
 *
 * \param scope[in] scope object pointer.
 *
 * \return resulting object reference.
 */
typedef o42a_obj_t *o42a_obj_ref_ft(o42a_obj_t *);

/**
 * Object constructor function.
 *
 * \param scope[in] scope object pointer.
 * \param fld[in] pointer to field, which object construction invoked for. This
 * may be a field from object different from scope (see o42a_fld_obj.previous),
 * but is always belongs to compatible body of that object.
 *
 * \return resulting object reference.
 */
typedef o42a_obj_t *o42a_obj_constructor_ft(o42a_obj_t *, struct o42a_fld_obj *);

/**
 * Variable assigner function.
 *
 * \param object[in] object containing variable.
 * \param value[in] value to assign.
 *
 * \return assigned object of valid type or NULL if assignment failed.
 */
typedef o42a_obj_t *o42a_obj_assigner_ft(o42a_obj_t*, o42a_obj_t*);


#define o42a_layout(target) _o42a_layout(__alignof__ (target), sizeof (target))

#ifdef __cplusplus
extern "C" {
#endif


size_t o42a_layout_size(o42a_layout_t);

size_t o42a_layout_array_size(o42a_layout_t, size_t);

o42a_layout_t o42a_layout_array(o42a_layout_t, size_t);

uint8_t o42a_layout_alignment(o42a_layout_t);

size_t o42a_layout_offset(size_t, o42a_layout_t);

size_t o42a_layout_pad(size_t, o42a_layout_t);

o42a_layout_t o42a_layout_both(o42a_layout_t, o42a_layout_t);


o42a_layout_t _o42a_layout(uint8_t, size_t);


size_t o42a_val_ashift(const o42a_val_t*);

size_t o42a_val_alignment(const o42a_val_t*);

void *o42a_val_data(const o42a_val_t*);


#ifdef __cplusplus
}
#endif

#endif
