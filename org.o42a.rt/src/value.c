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
