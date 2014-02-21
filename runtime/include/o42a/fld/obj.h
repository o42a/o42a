/*
    Copyright (C) 2010-2014 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#ifndef O42A_FLD_OBJ_H
#define O42A_FLD_OBJ_H

#include "o42a/field.h"


#ifdef __cplusplus
extern "C" {
#endif

typedef struct o42a_fld_obj o42a_fld_obj;

/**
 * Object constructor function.
 *
 * \param scope[in] scope object pointer.
 * \param fld[in] pointer to field, which object construction invoked for. This
 * may be a field from object different from scope (see o42a_fld_obj.previous),
 * but is always belongs to compatible body of that object.
 * \param ancestor_data[in] known ancestor data or NULL when fld belongs
 * to scope. In the latter case an ancestor_type will be evaluated
 * by constructor.
 *
 * \return resulting object reference.
 */
typedef o42a_obj_t *o42a_obj_constructor_ft(
		o42a_obj_t *,
		o42a_fld_obj *,
		o42a_obj_data_t *);


struct o42a_fld_obj {

	O42A_HEADER

	o42a_obj_t *object;

	o42a_obj_constructor_ft *constructor;

	o42a_fld_obj *previous;

};


#ifndef NDEBUG
extern const o42a_dbg_type_info3f_t _O42A_DEBUG_TYPE_o42a_fld_obj;
#endif /* NDEBUG */

void o42a_fld_obj_propagate(o42a_obj_ctable_t*);

void o42a_fld_obj_inherit(o42a_obj_ctable_t*);

o42a_bool_t o42a_fld_obj_is_init(const o42a_fld *);

/**
 * Object constructor stub.
 */
o42a_obj_body_t *o42a_obj_constructor_stub(o42a_obj_t *, struct o42a_fld_obj *);

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* O42A_FLD_OBJ_H */
