/*
    Copyright (C) 2012-2014 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#include "o42a/fields.h"


#ifndef NDEBUG
const o42a_dbg_type_info1f_t _O42A_DEBUG_TYPE_o42a_fld_dep = {
	.type_code = 0x042a0200 | O42A_FLD_DEP,
	.field_num = 1,
	.name = "o42a_fld_dep",
	.fields = {
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_fld_dep, object),
			.name = "object",
		},
	},
};
#endif /* NDEBUG */

void o42a_fld_dep_copy(o42a_obj_ctable_t *const ctable) {
	O42A_ENTER(return);

	const o42a_fld_dep *const from = &ctable->from_fld->dep;
	o42a_fld_dep *const to = &ctable->to_fld->dep;

	to->object = from->object;

	O42A_RETURN;
}
