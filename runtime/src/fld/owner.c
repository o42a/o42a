/*
    Copyright (C) 2010-2014 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#include "o42a/fields.h"


#ifndef NDEBUG
const o42a_dbg_type_info1f_t _O42A_DEBUG_TYPE_o42a_fld_owner = {
	.type_code = 0x042a0200 | O42A_FLD_OWNER,
	.field_num = 1,
	.name = "o42a_fld_owner",
	.fields = {
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_fld_owner, object),
			.name = "object",
		},
	},
};
#endif /* NDEBUG */

void o42a_fld_owner_propagate(o42a_obj_ctable_t *const ctable) {
	O42A_ENTER(return);

	const o42a_fld_owner *const from = &ctable->from.fld->owner;
	o42a_fld_owner *const to = &ctable->to.fld->owner;

	// Update the scope only when propagating field.
	if (ctable->to.body->flags & O42A_OBJ_BODY_PROPAGATED) {
		// Update only the scope of body propagated from field
		// or the one of main body.

		const o42a_obj_data_t *const owner_data = ctable->owner_data;

		if (owner_data) {
			to->object = O42A(o42a_obj_by_data(owner_data));

			o42a_debug_mem_name("Updated owner: ", to->object);

			O42A_RETURN;
		}

		o42a_debug("Object is local\n");
	}

	to->object = from->object;

	o42a_debug_mem_name("Leave the owner unchanged: ", to->object);

	O42A_RETURN;
}

void o42a_fld_owner_inherit(o42a_obj_ctable_t *const ctable) {
	O42A_ENTER(return);

	const o42a_fld_owner *const from = &ctable->from.fld->owner;
	o42a_fld_owner *const to = &ctable->to.fld->owner;

	to->object = from->object;

	o42a_debug_mem_name("Leave the owner unchanged: ", from->object);

	O42A_RETURN;
}
