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
#ifndef O42A_FLD_OBJ_H
#define O42A_FLD_OBJ_H

#include "o42a/field.h"


typedef struct o42a_fld_obj o42a_fld_obj;


#ifdef __cplusplus
extern "C" {
#endif

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
typedef o42a_obj_t *o42a_obj_constructor_ft(
		O42A_DECLS
		o42a_obj_t *,
		o42a_fld_obj *);


#ifdef __cplusplus
} /* extern "C" */
#endif


struct o42a_fld_obj {

	O42A_HEADER;

	o42a_obj_t *object;

	o42a_obj_constructor_ft *constructor;

	o42a_fld_obj *previous;

};


#ifdef __cplusplus
extern "C" {
#endif

void o42a_fld_obj_propagate(O42A_DECLS o42a_obj_ctable_t*);

void o42a_fld_obj_inherit(O42A_DECLS o42a_obj_ctable_t*);

/**
 * Object constructor stub.
 */
o42a_obj_body_t *o42a_obj_constructor_stub(
		O42A_DECLS
		o42a_obj_t *,
		struct o42a_fld_obj *);


#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* O42A_FLD_OBJ_H */
