/*
    Copyright (C) 2012 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#include "o42a/fields.h"

#include "o42a/type/array.h"


#ifndef NDEBUG
const o42a_dbg_type_info1f_t _O42A_DEBUG_TYPE_o42a_kpr_array = {
	.type_code = 0x042a0200 | O42A_KPR_ARRAY,
	.field_num = 1,
	.name = "o42a_kpr_array",
	.fields = {
		{
			.data_type = O42A_TYPE_STRUCT,
			.offset = offsetof(o42a_kpr_array, value),
			.name = "value",
			.type_info = (o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_val,
		},
	},
};
#endif /* NDEBUG */

void o42a_kpr_array_mark(o42a_fld *const fld) {
	O42A_ENTER(return);
	O42A(o42a_array_mark(&fld->kpr_array.value));
	O42A_RETURN;
}
