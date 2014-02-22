/*
    Copyright (C) 2012-2014 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#include "o42a/fields.h"


#ifndef NDEBUG
const o42a_dbg_type_info1f_t _O42A_DEBUG_TYPE_o42a_fld_link = {
	.type_code = 0x042a0200 | O42A_FLD_LINK,
	.field_num = 1,
	.name = "o42a_fld_link",
	.fields = {
		{
			.data_type = O42A_TYPE_FUNC_PTR,
			.offset = offsetof(o42a_fld_link, constructor),
			.name = "constructor",
		},
	},
};
#endif /* NDEBUG */

void o42a_fld_link_propagate(o42a_obj_ctable_t *const ctable) {
	O42A_ENTER(return);

	const o42a_fld_link *const from = &ctable->from.fld->link;
	o42a_fld_link *const to = &ctable->to.fld->link;

	to->constructor = from->constructor;

	O42A_RETURN;
}

void o42a_fld_link_inherit(o42a_obj_ctable_t *const ctable) {
	O42A_ENTER(return);

	const o42a_fld_link *const from = &ctable->from.fld->link;
	o42a_fld_link *const to = &ctable->to.fld->link;

	o42a_obj_overrider_t *const overrider = O42A(o42a_obj_field_overrider(
			ctable->sample_desc,
			ctable->field));

	if (overrider) {// Field is overridden.
		if (!O42A(o42a_obj_ascendant_of_type(
				ctable->ancestor_data,
				overrider->defined_in))) {
			// The body overrider defined in isn't present in ancestor
			// and thus not overridden there.
			// Use definition from overrider.
			to->constructor = O42A(o42a_fld_by_overrider(
					overrider)->link.constructor);
			O42A_RETURN;
		}
	}

	// Use definition from ancestor.
	to->constructor = from->constructor;

	O42A_RETURN;
}
