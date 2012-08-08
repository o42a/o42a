/*
    Run-Time Library
    Copyright (C) 2011,2012 Ruslan Lopatin

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
#include "o42a/type/array.h"

#include <string.h>

#include "o42a/error.h"
#include "o42a/memory/gc.h"
#include "o42a/memory/refcount.h"


o42a_array_t o42a_array_alloc(o42a_val_t *value, const uint32_t size) {
	O42A_ENTER(return NULL);

	const o42a_array_t array =
			O42A(o42a_refcount_alloc(sizeof(o42a_array_t) * size));

	if (!array) {
		value->flags = O42A_FALSE;
		O42A(o42a_error_printf(
				"Failed to allocate %ld array items",
				(long) size));
		O42A_RETURN NULL;
	}

	value->flags =
			O42A_TRUE | O42A_VAL_EXTERNAL
			| (sizeof(o42a_array_t) == 8 ? 0x30 : 0x20);
	value->length = size;
	value->value.v_ptr = array;

	O42A_RETURN array;
}

void o42a_array_copy(const o42a_val_t *const from, o42a_val_t *const to) {
	O42A_ENTER(return);

	const uint32_t size = from->length;

	if (!size) {
		(*to) = *from;
		O42A_RETURN;
	}

	const o42a_array_t dest = O42A(o42a_array_alloc(to, size));

	if (!dest) {
		O42A_RETURN;
	}

	memcpy(dest, from->value.v_ptr, size * sizeof(o42a_array_t));

	O42A_RETURN;
}

static void o42a_val_mark_array(o42a_obj_data_t *const data) {
	O42A_ENTER(return);

	const volatile o42a_val_t *const value = &data->value;

	if (!value->flags & O42A_VAL_CONDITION) {
		O42A_RETURN;
	}

	const uint32_t length = value->length;

	if (!length) {
		O42A_RETURN;
	}

	void *volatile *const items = (void *volatile *) value->value.v_ptr;

	if (!items) {
		O42A_RETURN;
	}

	for (uint32_t i = 0; i < length; ++i) {

		o42a_obj_t *const item = items[i];

		if (!item) {
			continue;
		}

		o42a_obj_data_t *const item_data = &o42a_obj_type(item)->type.data;

		O42A(o42a_gc_mark(
				o42a_gc_blockof((char *) item_data + item_data->start)));
	}

	O42A_RETURN;
}

static void o42a_val_sweep_array(o42a_obj_data_t *const data) {
	O42A_ENTER(return);

	const volatile o42a_val_t *const value = &data->value;

	if (!value->flags & O42A_VAL_CONDITION) {
		O42A_RETURN;
	}

	const uint32_t length = value->length;

	if (!length) {
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

const o42a_val_type_t o42a_val_type_array =
		O42A_VAL_TYPE("array", &o42a_val_mark_array, &o42a_val_sweep_array);


const o42a_val_type_t o42a_val_type_row =
		O42A_VAL_TYPE("row", &o42a_val_mark_array, &o42a_val_sweep_array);
