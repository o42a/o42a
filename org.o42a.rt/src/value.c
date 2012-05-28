/*
    Run-Time Library
    Copyright (C) 2010-2012 Ruslan Lopatin

    This file is part of o42a.

    o42a is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    o42a is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
#include "o42a/value.h"

#include "o42a/memory/refcount.h"
#include "o42a/object.h"


inline size_t o42a_val_ashift(const o42a_val_t *const val) {
	return (val->flags & O42A_VAL_ALIGNMENT_MASK) >> 8;
}

inline size_t o42a_val_alignment(const o42a_val_t *const val) {
	return 1 << o42a_val_ashift(val);
}

inline void *o42a_val_data(const o42a_val_t *const val) {
	if (val->flags & O42A_VAL_EXTERNAL) {
		return val->value.v_ptr;
	}
	return (void*) &val->value;
}


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

const o42a_val_type_t o42a_val_type_void =
		O42A_VAL_TYPE("void", &o42a_val_mark_none, &o42a_val_sweep_none);

const o42a_val_type_t o42a_val_type_directive =
		O42A_VAL_TYPE("directive", &o42a_val_mark_none, &o42a_val_sweep_none);

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

void o42a_val_mark_none(struct o42a_obj_data *data) {
	O42A_ENTER(return);
	O42A_RETURN;
}

void o42a_val_sweep_none(o42a_obj_data_t *const data) {
	O42A_ENTER(return);
	O42A_RETURN;
}

void o42a_val_sweep_external(o42a_obj_data_t *const data) {
	O42A_ENTER(return);

	const volatile o42a_val_t *const value = &data->value;
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
