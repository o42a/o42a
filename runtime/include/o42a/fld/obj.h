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

/**
 * Object constructor function.
 *
 * \param vmtc VMT chain of the object.
 * \param ctr field object construction data.
 *
 * \return resulting object reference.
 */
typedef o42a_obj_t *o42a_obj_constructor_ft(
		const o42a_obj_vmtc_t *,
		o42a_obj_ctr_t *);

typedef struct o42a_fld_obj {

	O42A_HEADER

	o42a_obj_t *object;

} o42a_fld_obj;


#ifndef NDEBUG

extern const o42a_dbg_type_info1f_t _O42A_DEBUG_TYPE_o42a_fld_obj;

extern const o42a_dbg_type_info2f_t _O42A_DEBUG_TYPE_o42a_fld_obj_conf;

#endif /* NDEBUG */

/**
 * Object field construction configurator.
 *
 * This function recursively updates the object VMT chain and value function
 * based on the field configurations contained in each VMT in the given chain.
 *
 * When called from client code it is expected that object passed already
 * contains a VMT chain from ancestor.
 *
 * \param vmtc VMT chain to obtain field configuration and previous chain from.
 * \param object allocated object.
 * \param offset an offset of object field configuration pointer in VMT.
 *
 * \return O42A_TRUE if configuration succeed, or O42A_FALSE otherwise,
 * e.g. when VMT chain allocation failed
 */
o42a_bool_t o42a_fld_obj_configure(
		const o42a_obj_vmtc_t *,
		o42a_obj_t *,
		o42a_rptr_t);

/**
 * Object constructor stub.
 */
o42a_obj_t *o42a_obj_constructor_stub(
		const o42a_obj_vmtc_t *,
		o42a_obj_ctr_t *);

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* O42A_FLD_OBJ_H */
