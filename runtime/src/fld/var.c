/*
    Copyright (C) 2010-2014 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#include "o42a/fields.h"

#include "o42a/memory/gc.h"


#ifndef NDEBUG
const o42a_dbg_type_info2f_t _O42A_DEBUG_TYPE_o42a_fld_var = {
	.type_code = 0x042a0200 | O42A_FLD_VAR,
	.field_num = 2,
	.name = "o42a_fld_var",
	.fields = {
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_fld_var, object),
			.name = "object",
		},
		{
			.data_type = O42A_TYPE_FUNC_PTR,
			.offset = offsetof(o42a_fld_var, constructor),
			.name = "constructor",
		},
	},
};
#endif /* NDEBUG */

void o42a_fld_var_propagate(o42a_obj_ctable_t *const ctable) {
	O42A_ENTER(return);

	const o42a_fld_var *const from = &ctable->from.fld->var;
	o42a_fld_var *const to = &ctable->to.fld->var;

	to->object = NULL;
	to->constructor = from->constructor;

	O42A_RETURN;
}

void o42a_fld_var_inherit(o42a_obj_ctable_t *const ctable) {
	O42A_ENTER(return);

	const o42a_fld_var *const from = &ctable->from.fld->var;
	o42a_fld_var *const to = &ctable->to.fld->var;

	to->object = NULL;

	o42a_obj_overrider_t *const overrider =
			O42A(o42a_obj_field_overrider(
					ctable->sample_type,
					ctable->field));

	if (overrider) {// Field is overridden.
		if (!O42A(o42a_obj_ascendant_of_type(
				&ctable->ancestor_type->type.data,
				overrider->defined_in))) {
			// The body overrider defined isn't present in ancestor
			// and thus not overridden there.
			// Use definition from overrider.

			const o42a_fld_var *const ovr =
					O42A(&o42a_fld_by_overrider(overrider)->var);

			to->constructor = ovr->constructor;

			O42A_RETURN;
		}
	}

	// Use definition from ancestor.
	to->constructor = from->constructor;

	O42A_RETURN;
}

void o42a_fld_var_mark(o42a_fld *const field) {
	O42A_ENTER(return);

	volatile o42a_fld_var *const fld = &field->var;
	o42a_obj_t *const object = fld->object;

	if (object) {

		o42a_obj_data_t *const data = O42A(&o42a_obj_type(object)->type.data);

		O42A(o42a_gc_mark(o42a_gc_blockof((char *) data + data->start)));
	}

	O42A_RETURN;
}
