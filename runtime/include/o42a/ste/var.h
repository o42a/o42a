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
#ifndef O42A_STE_VAR_H
#define O42A_STE_VAR_H

#include "o42a/field.h"


#ifdef __cplusplus
extern "C" {
#endif

typedef struct {

	O42A_HEADER

	o42a_obj_stype_t *bound;

	o42a_obj_assigner_ft *assigner_f;

} o42a_ste_var;


#ifndef NDEBUG
extern const o42a_dbg_type_info2f_t _O42A_DEBUG_TYPE_o42a_ste_var;
#endif /* NDEBUG */

void o42a_ste_var_propagate(o42a_obj_ctable_t *);

void o42a_ste_var_inherit(o42a_obj_ctable_t *);

void o42a_ste_var_mark(o42a_fld *);


#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* O42A_STE_VAR_H */