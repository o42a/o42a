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
 * \param object[in] scope object pointer.
 * \param vmtc[in] a pointer to VMT chain the object construction is invoked
 * from. This can be either an original VMT chain from the object, or one of
 * the links of that chain.
 * \param ancestor[in] known ancestor object pointer, or NULL when vmtc belongs
 * to the object. In the latter case the ancestor_data will be evaluated
 * by constructor.
 *
 * \return resulting object reference.
 */
typedef o42a_obj_t *o42a_obj_constructor_ft(
		o42a_obj_t *,
		const o42a_obj_vmtc_t *,
		o42a_obj_t *);


struct o42a_fld_obj {

	O42A_HEADER

	o42a_obj_t *object;

};


#ifndef NDEBUG
extern const o42a_dbg_type_info1f_t _O42A_DEBUG_TYPE_o42a_fld_obj;
#endif /* NDEBUG */

void o42a_fld_obj_reset(o42a_obj_ctable_t*);

o42a_bool_t o42a_fld_obj_is_init(const o42a_fld *);

/**
 * Object constructor stub.
 */
o42a_obj_t *o42a_obj_constructor_stub(
		o42a_obj_t *,
		const o42a_obj_vmtc_t *,
		o42a_obj_t *);

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* O42A_FLD_OBJ_H */
