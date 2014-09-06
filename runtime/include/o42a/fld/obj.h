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
 * Object field construction data.
 */
typedef struct o42a_fld_obj_ctr {

	O42A_HEADER

	/**
	 * Scope object pointer.
	 */
	const o42a_obj_t *owner;

	/**
	 * A pointer to VMT chain the object construction is invoked from.
	 *
	 * This can be either an original VMT chain from the object,
	 * or one of the links of that chain.
	 */
	const o42a_obj_vmtc_t *vmtc;

	/**
	 * Known ancestor object pointer.
	 *
	 * When NULL the ancestor will be evaluated by constructor.
	 */
	const o42a_obj_t *ancestor;

} o42a_fld_obj_ctr_t;

/**
 * Object constructor function.
 *
 * \param fctr field construction data.
 *
 * \return resulting object reference.
 */
typedef o42a_obj_t *o42a_obj_constructor_ft(o42a_fld_obj_ctr_t *);


struct o42a_fld_obj {

	O42A_HEADER

	o42a_obj_t *object;

};


#ifndef NDEBUG

extern const o42a_dbg_type_info1f_t _O42A_DEBUG_TYPE_o42a_fld_obj;

extern const o42a_dbg_type_info3f_t _O42A_DEBUG_TYPE_o42a_fld_obj_ctr;

#endif /* NDEBUG */

void o42a_fld_obj_reset(o42a_obj_ctable_t*);

o42a_bool_t o42a_fld_obj_is_init(const o42a_fld *);

/**
 * Object constructor stub.
 */
o42a_obj_t *o42a_obj_constructor_stub(o42a_fld_obj_ctr_t *);

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* O42A_FLD_OBJ_H */
