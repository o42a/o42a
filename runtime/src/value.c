/*
    Copyright (C) 2010-2013 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#include "o42a/value.h"

#include "o42a/memory/refcount.h"
#include "o42a/object.h"


extern size_t o42a_val_ashift(const o42a_val_t *);

extern size_t o42a_val_alignment(const o42a_val_t *);

extern void *o42a_val_data(const o42a_val_t *);


inline void o42a_val_use(o42a_val_t *const val) {
	O42A_ENTER(return);

	const uint32_t flags = val->flags;

	if (!(flags & O42A_VAL_EXTERNAL)) {
		O42A_RETURN;
	}
	if (flags & O42A_VAL_STATIC) {
		O42A_RETURN;
	}

	o42a_refcount_block_t *const block =
			O42A(o42a_refcount_blockof(val->value.v_ptr));

	__sync_fetch_and_add(&block->ref_count, 1);

	O42A_RETURN;
}

inline void o42a_val_unuse(o42a_val_t *const val) {
	O42A_ENTER(return);

	const uint32_t flags = val->flags;

	if (!(flags & O42A_VAL_EXTERNAL)) {
		O42A_RETURN;
	}
	if (flags & O42A_VAL_STATIC) {
		O42A_RETURN;
	}

	o42a_refcount_block_t *const block =
			O42A(o42a_refcount_blockof(val->value.v_ptr));

	if (!__sync_sub_and_fetch(&block->ref_count, 1)) {
		O42A(o42a_refcount_free(block));
	}

	O42A_RETURN;
}

void o42a_val_gc_none(o42a_obj_data_t *const data __attribute__((unused))) {
	O42A_ENTER(return);
	O42A_RETURN;
}

const o42a_val_type_t o42a_val_type_void = O42A_VAL_TYPE(
		"void",
		o42a_val_gc_none,
		o42a_val_gc_none);

const o42a_val_type_t o42a_val_type_directive = O42A_VAL_TYPE(
		"directive",
		o42a_val_gc_none,
		o42a_val_gc_none);

const o42a_val_type_t o42a_val_type_macro = O42A_VAL_TYPE(
		"macro",
		o42a_val_gc_none,
		o42a_val_gc_none);

#ifndef NDEBUG

const o42a_dbg_type_info3f_t _O42A_DEBUG_TYPE_o42a_val = {
	.type_code = 0x042a0002,
	.field_num = 3,
	.name = "o42a_val_t",
	.fields = {
		{
			.data_type = O42A_TYPE_INT32,
			.offset = offsetof(o42a_val_t, flags),
			.name = "flags",
		},
		{
			.data_type = O42A_TYPE_INT32,
			.offset = offsetof(o42a_val_t, length),
			.name = "length",
		},
		{
			.data_type = O42A_TYPE_INT64,
			.offset = offsetof(o42a_val_t, value),
			.name = "value",
		}
	},
};

const o42a_dbg_type_info3f_t _O42A_DEBUG_TYPE_o42a_val_type = {
	.type_code = 0x042a0003,
	.field_num = 3,
	.name = "o42a_val_type_t",
	.fields = {
		{
			.data_type = O42A_TYPE_PTR,
			.offset = offsetof(o42a_val_type_t, name),
			.name = "name",
		},
		{
			.data_type = O42A_TYPE_FUNC_PTR,
			.offset = offsetof(o42a_val_type_t, mark),
			.name = "mark",
		},
		{
			.data_type = O42A_TYPE_FUNC_PTR,
			.offset = offsetof(o42a_val_type_t, sweep),
			.name = "sweep",
		}
	},
};

#endif /* NDEBUG */
