/*
    Copyright (C) 2012,2013 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#include "o42a/fields.h"

#include "o42a/memory/gc.h"


#ifndef NDEBUG
const o42a_dbg_type_info1f_t _O42A_DEBUG_TYPE_o42a_ste_var = {
	.type_code = 0x042a0200 | O42A_STE_VAR,
	.field_num = 1,
	.name = "o42a_ste_var",
	.fields = {
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_ste_var, object),
			.name = "object",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_stype,
		},
	},
};
#endif /* NDEBUG */

void o42a_ste_var_derive(o42a_obj_ctable_t *const ctable) {
	O42A_ENTER(return);

	const o42a_ste_var *const from = &ctable->from.fld->ste_var;
	o42a_ste_var *const to = &ctable->to.fld->ste_var;

	to->object = NULL;

	O42A_RETURN;
}

void o42a_ste_var_mark(o42a_fld *const field) {
	O42A_ENTER(return);

	volatile o42a_ste_var *const fld = &field->ste_var;
	o42a_obj_t *const object = fld->object;

	if (object) {

		o42a_obj_data_t *const data = O42A(&o42a_obj_type(object)->type.data);

		O42A(o42a_gc_mark(o42a_gc_blockof((char *) data + data->start)));
	}

	O42A_RETURN;
}
