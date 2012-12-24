/*
    Copyright (C) 2012 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#include "o42a/fields.h"


#ifndef NDEBUG
const o42a_dbg_type_info1f_t _O42A_DEBUG_TYPE_o42a_kpr_link = {
	.type_code = 0x042a0200 | O42A_KPR_LINK,
	.field_num = 2,
	.name = "o42a_kpr_link",
	.fields = {
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_kpr_link, object),
			.name = "object",
		},
	},
};
#endif /* NDEBUG */

void o42a_kpr_link_derive(o42a_obj_ctable_t *const ctable) {
	O42A_ENTER(return);

	o42a_kpr_link *const to = &ctable->to.fld->kpr_link;

	to->object = NULL;

	O42A_RETURN;
}
