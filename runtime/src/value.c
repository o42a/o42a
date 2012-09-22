/*
    Run-Time Library
    Copyright (C) 2010-2012 Ruslan Lopatin

    This file is part of o42a.

    o42a is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License
    as published by the Free Software Foundation, either version 3
    of the License, or (at your option) any later version.

    o42a is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

const o42a_val_type_t o42a_val_type_void = O42A_VAL_TYPE("void");

const o42a_val_type_t o42a_val_type_directive = O42A_VAL_TYPE("directive");

const o42a_val_type_t o42a_val_type_macro = O42A_VAL_TYPE("macro");

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

const o42a_dbg_type_info1f_t _O42A_DEBUG_TYPE_o42a_val_type = {
	.type_code = 0x042a0003,
	.field_num = 3,
	.name = "o42a_val_type_t",
	.fields = {
		{
			.data_type = O42A_TYPE_PTR,
			.offset = offsetof(o42a_val_type_t, name),
			.name = "name",
		},
	},
};

#endif /* NDEBUG */
