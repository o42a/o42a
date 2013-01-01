/*
    Copyright (C) 2012,2013 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#include "o42a/fields.h"

#include "o42a/memory/refcount.h"


#ifndef NDEBUG
const o42a_dbg_type_info1f_t _O42A_DEBUG_TYPE_o42a_kpr_string = {
	.type_code = 0x042a0200 | O42A_KPR_STRING,
	.field_num = 1,
	.name = "o42a_kpr_string",
	.fields = {
		{
			.data_type = O42A_TYPE_STRUCT,
			.offset = offsetof(o42a_kpr_string, value),
			.name = "value",
			.type_info = (o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_val,
		},
	},
};
#endif /* NDEBUG */

void o42a_kpr_value_derive(o42a_obj_ctable_t *const ctable) {
	O42A_ENTER(return);

	o42a_kpr_string *const to = &ctable->to.fld->kpr_string;

	to->value.flags = O42A_VAL_INDEFINITE;

	O42A_RETURN;
}

void o42a_kpr_value_sweep(o42a_fld *const field) {
	O42A_ENTER(return);

	const volatile o42a_val_t *const value = &field->kpr_string.value;
	const uint32_t flags = value->flags;

	if (!(flags & O42A_VAL_CONDITION)) {
		O42A_RETURN;
	}
	if (flags & O42A_VAL_STATIC) {
		O42A_RETURN;
	}
	if (!(flags & O42A_VAL_EXTERNAL)) {
		O42A_RETURN;
	}

	void *const ptr = value->value.v_ptr;

	if (!ptr) {
		O42A_RETURN;
	}

	o42a_refcount_block_t *const block = o42a_refcount_blockof(ptr);

	if (!__sync_sub_and_fetch(&block->ref_count, 1)) {
		O42A(o42a_refcount_free(block));
	}

	O42A_RETURN;
}

o42a_bool_t o42a_kpr_value_is_init(const o42a_fld *const fld) {
	O42A_ENTER(return O42A_FALSE);
	O42A_RETURN ((fld->kpr_string.value.flags & O42A_VAL_INDEFINITE)
			? O42A_FALSE : O42A_TRUE);
}
