/*
    Copyright (C) 2010-2014 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#include "o42a/fields.h"

#include <assert.h>


#ifndef NDEBUG
const o42a_dbg_type_info1f_t _O42A_DEBUG_TYPE_o42a_fld_owner = {
	.type_code = 0x042a0200 | O42A_FLD_OWNER,
	.field_num = 1,
	.name = "o42a_fld_owner",
	.fields = {
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_fld_obj, object),
			.name = "object",
		},
	},
};
#endif /* NDEBUG */

void o42a_fld_owner_set(o42a_obj_ctable_t *const ctable) {
	O42A_ENTER(return);

	assert(ctable->owner && "Object owner is unknown");
	ctable->to_fld->obj.object = ctable->owner;
	o42a_debug_mem_name("Owner: ", ctable->to_fld->obj.object);

	O42A_RETURN;
}
