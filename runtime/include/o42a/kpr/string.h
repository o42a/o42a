/*
    Run-Time Library
    Copyright (C) 2012 Ruslan Lopatin

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
#ifndef O42A_KPR_STRING_H
#define O42A_KPR_STRING_H

#include "o42a/field.h"


#ifdef __cplusplus
extern "C" {
#endif

typedef struct {

	O42A_HEADER

	o42a_val_t value;

} o42a_kpr_string;


#ifndef NDEBUG
extern const o42a_dbg_type_info1f_t _O42A_DEBUG_TYPE_o42a_kpr_string;
#endif /* NDEBUG */


void o42a_kpr_value_derive(o42a_obj_ctable_t *);

void o42a_kpr_value_sweep(o42a_fld *);

o42a_bool_t o42a_kpr_value_is_init(const o42a_fld *);

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* O42A_KPR_STRING_H */
