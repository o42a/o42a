/*
    Copyright (C) 2012 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#include "o42a/fields.h"


#ifndef NDEBUG
const o42a_dbg_type_info1f_t _O42A_DEBUG_TYPE_o42a_kpr_void = {
	.type_code = 0x042a0200 | O42A_KPR_VOID,
	.field_num = 2,
	.name = "o42a_kpr_void",
	.fields = {
		{
			.data_type = O42A_TYPE_INT8,
			.offset = offsetof(o42a_kpr_void, flags),
			.name = "flags",
		},
	},
};
#endif /* NDEBUG */

void o42a_kpr_void_derive(o42a_obj_ctable_t *const ctable) {
	O42A_ENTER(return);

	o42a_kpr_void *const to = &ctable->to.fld->kpr_void;

	to->flags = O42A_VAL_INDEFINITE;

	O42A_RETURN;
}

o42a_bool_t o42a_kpr_void_is_init(const o42a_fld *const fld) {
	O42A_ENTER(return O42A_FALSE);
	O42A_RETURN ((fld->kpr_void.flags & O42A_VAL_INDEFINITE)
			? O42A_FALSE : O42A_TRUE);
}
