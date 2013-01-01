/*
    Copyright (C) 2012,2013 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#include "o42a/fields.h"


#ifndef NDEBUG
const o42a_dbg_type_info2f_t _O42A_DEBUG_TYPE_o42a_kpr_integer = {
	.type_code = 0x042a0200 | O42A_KPR_INTEGER,
	.field_num = 2,
	.name = "o42a_kpr_integer",
	.fields = {
		{
			.data_type = O42A_TYPE_INT8,
			.offset = offsetof(o42a_kpr_integer, flags),
			.name = "flags",
		},
		{
			.data_type = O42A_TYPE_INT64,
			.offset = offsetof(o42a_kpr_integer, value),
			.name = "value",
		},
	},
};
#endif /* NDEBUG */

void o42a_kpr_integer_derive(o42a_obj_ctable_t *const ctable) {
	O42A_ENTER(return);

	o42a_kpr_integer *const to = &ctable->to.fld->kpr_integer;

	to->flags = O42A_VAL_INDEFINITE;
	to->value = 0;

	O42A_RETURN;
}

o42a_bool_t o42a_kpr_integer_is_init(const o42a_fld *const fld) {
	O42A_ENTER(return O42A_FALSE);
	O42A_RETURN ((fld->kpr_integer.flags & O42A_VAL_INDEFINITE)
			? O42A_FALSE : O42A_TRUE);
}
