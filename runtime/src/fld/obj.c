/*
    Copyright (C) 2010-2014 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#include "o42a/fields.h"

#include "o42a/error.h"


#ifndef NDEBUG
const o42a_dbg_type_info1f_t _O42A_DEBUG_TYPE_o42a_fld_obj = {
	.type_code = 0x042a0200 | O42A_FLD_OBJ,
	.field_num = 1,
	.name = "o42a_fld_obj",
	.fields = {
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_fld_obj, object),
			.name = "object",
		},
	},
};
#endif /* NDEBUG */

void o42a_fld_obj_reset(o42a_obj_ctable_t *const ctable) {
	O42A_ENTER(return);

	o42a_fld_obj *const to = &ctable->to_fld->obj;

	to->object = NULL;

	O42A_RETURN;
}

o42a_bool_t o42a_fld_obj_is_init(const o42a_fld *const fld) {
	O42A_ENTER(return O42A_FALSE);
	O42A_RETURN fld->obj.object ? O42A_TRUE : O42A_FALSE;
}

o42a_obj_t *o42a_obj_constructor_stub(
		o42a_obj_t *object __attribute__((unused)),
		const o42a_obj_vmtc_t *vmtc __attribute__((unused)),
		o42a_obj_t *ancestor_data __attribute__((unused))) {
	O42A_ENTER(return NULL);
	o42a_error_print("Object constructor stub invoked");
	O42A_RETURN NULL;
}
