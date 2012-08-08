/*
    Run-Time Library
    Copyright (C) 2011,2012 Ruslan Lopatin

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


o42a_array_t o42a_array_alloc(o42a_val_t *, uint32_t);

void o42a_array_copy(const o42a_val_t *, o42a_val_t *);


#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* O42A_TYPE_ARRAY_H */