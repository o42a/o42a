/*
    Copyright (C) 2010-2014 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#include "o42a/fields.h"

#include "o42a/error.h"


#ifndef NDEBUG
const o42a_dbg_type_info3f_t _O42A_DEBUG_TYPE_o42a_fld_obj = {
	.type_code = 0x042a0200 | O42A_FLD_OBJ,
	.field_num = 3,
	.name = "o42a_fld_obj",
	.fields = {
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_fld_obj, object),
			.name = "object",
		},
		{
			.data_type = O42A_TYPE_FUNC_PTR,
			.offset = offsetof(o42a_fld_obj, constructor),
			.name = "constructor",
		},
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_fld_obj, previous),
			.name = "previous",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_fld_obj,
		}
	},
};
#endif /* NDEBUG */

void o42a_fld_obj_propagate(o42a_obj_ctable_t *const ctable) {
	O42A_ENTER(return);

	const o42a_fld_obj *const from = &ctable->from.fld->obj;
	o42a_fld_obj *const to = &ctable->to.fld->obj;

	to->object = NULL;
	to->constructor = from->constructor;
	to->previous = from->previous;

	O42A_RETURN;
}

void o42a_fld_obj_inherit(o42a_obj_ctable_t *const ctable) {
	O42A_ENTER(return);

	o42a_fld_obj *const from = &ctable->from.fld->obj;
	o42a_fld_obj *const to = &ctable->to.fld->obj;

	to->object = NULL;

	o42a_obj_overrider_t *const overrider =
			O42A(o42a_obj_field_overrider(ctable->sample_desc, ctable->field));

	if (overrider) {// Field is overridden.
		if (!O42A(o42a_obj_ascendant_of_type(
				ctable->ancestor_data,
				overrider->defined_in))) {
			// The body where overrider is defined in isn't present in ancestor
			// and thus not overridden there.
			// Use the definition from overrider.
			to->constructor =
					O42A(o42a_fld_by_overrider(overrider))->obj.constructor;
			// Store pointer to previous definition.
			to->previous = from;
			O42A_RETURN;
		}
	}

	// Use definition from ancestor.
	to->constructor = from->constructor;
	to->previous = from->previous;

	O42A_RETURN;
}

o42a_bool_t o42a_fld_obj_is_init(const o42a_fld *const fld) {
	O42A_ENTER(return O42A_FALSE);
	O42A_RETURN fld->obj.object ? O42A_TRUE : O42A_FALSE;
}

o42a_obj_body_t *o42a_obj_constructor_stub(
		o42a_obj_t *scope __attribute__((unused)),
		struct o42a_fld_obj *field __attribute__((unused))) {
	O42A_ENTER(return NULL);
	o42a_error_print("Object constructor stub invoked");
	O42A_RETURN NULL;
}
