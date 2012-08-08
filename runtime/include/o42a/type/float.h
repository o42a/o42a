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
#ifndef O42A_TYPE_FLOAT_H
#define O42A_TYPE_FLOAT_H

#include "o42a/types.h"
#include "o42a/value.h"


#ifdef __cplusplus
extern "C" {
#endif

/**
 * Float value type descriptor.
 */
extern const o42a_val_type_t o42a_val_type_float;

void o42a_float_by_str(o42a_val_t *, const o42a_val_t *);

int o42a_float_error(o42a_val_t *);

o42a_bool_t o42a_float_to_str(o42a_val_t *, double);


#ifdef __cplusplus
}
#endif

#endif /* O42A_TYPE_FLOAT_H */