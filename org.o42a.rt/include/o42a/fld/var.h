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
#ifndef O42A_FLD_VAR_H
#define O42A_FLD_VAR_H

#include "o42a/field.h"


/**
 * Variable assigner function.
 *
 * \param object[in] object pointer.
 * \param value[out] value to assign.
 *
 * \return O42A_TRUE on success or O42A_FALSE on failure.
 */
typedef o42a_bool_t o42a_obj_assigner_ft(O42A_DECLS o42a_obj_t *, o42a_obj_t *);


typedef struct {

	O42A_HEADER;

	o42a_obj_t *object;

	o42a_obj_ref_ft *constructor;

	o42a_obj_stype_t *bound;

	o42a_obj_assigner_ft *assigner_f;

} o42a_fld_var;


#ifdef __cplusplus
extern "C" {
#endif


void o42a_fld_var_propagate(O42A_DECLS o42a_obj_ctable_t*);

void o42a_fld_var_inherit(O42A_DECLS o42a_obj_ctable_t*);


#ifdef __cplusplus
}
#endif

#endif /* O42A_FLD_VAR_H */
