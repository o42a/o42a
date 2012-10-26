/*
    Run-Time Library
    Copyright (C) 2011,2012 Ruslan Lopatin

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
#ifndef O42A_TYPE_ARRAY_H
#define O42A_TYPE_ARRAY_H

#include "o42a/object.h"


#ifdef __cplusplus
extern "C" {
#endif

typedef o42a_obj_t* o42a_array_t;

/**
 * Mutable array value type descriptor.
 */
extern const o42a_val_type_t o42a_val_type_array;

/**
 * Immutable array (row) value type descriptor.
 */
extern const o42a_val_type_t o42a_val_type_row;

/**
 * Array items GC descriptor.
 */
extern const o42a_gc_desc_t o42a_array_gc_desc;


/**
 * Allocates array.
 *
 * Allocates the necessary amount of memory to store an array in GC-collected
 * block. An array data won't be initialized by this function.
 *
 * The block won't be submitted to the GC automatically. An array data should be
 * filled before submitting to GC.
 *
 * \param value[out] an array value to fill.
 * \param size[in]   the number of items the array should contain.
 *
 * \return pointer to array data, or NULL if array can not be allocated.
 */
o42a_array_t *o42a_array_alloc(o42a_val_t *, uint32_t);

/**
 * Copies one array value to another.
 *
 * \from[in] array value to copy from.
 * \to[out]  array value to copy to.
 */
void o42a_array_copy(const o42a_val_t *, o42a_val_t *);

/**
 * Creates an array of duplicates.
 *
 * Allocates array and fills it with the same element.
 *
 * \param value[out] array value to fill.
 * \param size[in]   the number of items the array should contain.
 * \param item[in]   array item to fill array with.
 */
void o42a_array_of_duplicates(o42a_val_t *, uint32_t, o42a_array_t);

void o42a_array_mark(const volatile o42a_val_t *);

void o42a_array_start_use(o42a_val_t *);

void o42a_array_end_use(o42a_val_t *);


#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* O42A_TYPE_ARRAY_H */
