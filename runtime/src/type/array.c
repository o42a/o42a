/*
    Copyright (C) 2011-2013 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#include "o42a/type/array.h"

#include <string.h>

#include "o42a/error.h"
#include "o42a/memory/gc.h"


// Last array items consists of all ones.
#define O42A_ARRAY_END ((const o42a_array_t) ~0L)

const o42a_val_type_t o42a_val_type_array = O42A_VAL_TYPE("array");

const o42a_val_type_t o42a_val_type_row = O42A_VAL_TYPE("row");

static void o42a_array_gc_marker(void *data) {
	O42A_ENTER(return);

	o42a_obj_t *volatile *items = data;

	while (1) {

		o42a_obj_t *const item = *items;

		if (item == O42A_ARRAY_END) {
			break;
		}
		if (item) {

			o42a_obj_data_t *const item_data = &o42a_obj_type(item)->type.data;

			O42A(o42a_gc_mark(
					o42a_gc_blockof((char *) item_data + item_data->start)));
		}
		++items;
	}

	O42A_RETURN;
}

const o42a_gc_desc_t o42a_array_gc_desc = {
	.mark = &o42a_array_gc_marker,
	.sweep = &o42a_gc_noop,
};

o42a_array_t *o42a_array_alloc(o42a_val_t *value, const uint32_t size) {
	O42A_ENTER(return NULL);

	o42a_array_t *const array = O42A(o42a_gc_alloc(
			&o42a_array_gc_desc,
			sizeof(o42a_array_t) * (size + 1)));

	if (!array) {
		value->flags = O42A_FALSE;
		O42A(o42a_error_printf(
				"Failed to allocate %ld array items",
				(long) size));
		O42A_RETURN NULL;
	}

	array[size] = O42A_ARRAY_END;
	value->value.v_ptr = array;
	value->length = size;
	value->flags = O42A_TRUE;

	O42A_RETURN array;
}

void o42a_array_copy(const o42a_val_t *const from, o42a_val_t *const to) {
	O42A_ENTER(return);

	const uint32_t size = from->length;

	if (!size) {
		(*to) = *from;
		O42A_RETURN;
	}

	o42a_array_t *const dest = O42A(o42a_array_alloc(to, size));

	if (!dest) {
		O42A_RETURN;
	}

	memcpy(dest, from->value.v_ptr, (size + 1) * sizeof(o42a_array_t));

	O42A_RETURN;
}

void o42a_array_of_duplicates(
		o42a_val_t *const value,
		uint32_t size,
		o42a_array_t item) {
	O42A_ENTER(return);

	if (!size) {
		value->flags = O42A_TRUE;
		value->length = 0;
		O42A_RETURN;
	}

	o42a_array_t *array = o42a_array_alloc(value, size);

	if (!array) {
		O42A_RETURN;
	}

	o42a_array_t *const end = array + size;

	do {
		(*array) = item;
		++array;
	} while (array < end);

	O42A_RETURN;
}

o42a_bool_t o42a_array_copy_elements(
		const o42a_val_t *const source,
		const int64_t source_from,
		const int64_t source_to,
		o42a_val_t *const target,
		const int64_t target_start) {
	O42A_ENTER(return O42A_FALSE);

	const uint32_t source_len = source->length;
	const uint32_t target_len = target->length;

	if (source_from < 0 || source_to > source_len) {
		O42A_RETURN O42A_FALSE;
	}
	if (target_start < 0 || target_start >= target_len) {
		O42A_RETURN O42A_FALSE;
	}

	const int64_t elements_to_copy = source_to - source_from;

	if (elements_to_copy <= 0) {
		if (!elements_to_copy) {
			// Nothing to copy.
			O42A_RETURN O42A_TRUE;
		}
		O42A_RETURN O42A_FALSE;
	}

	const o42a_array_t *const source_data = source->value.v_ptr;
	o42a_array_t *const target_data = target->value.v_ptr;

	O42A(memmove(
			target_data + target_start,
			source_data + source_from,
			(size_t) (elements_to_copy * sizeof(o42a_array_t))));

	O42A_RETURN O42A_TRUE;
}

void o42a_array_mark(const volatile o42a_val_t *const value) {
	O42A_ENTER(return);

	if (!(value->flags & O42A_VAL_CONDITION)) {
		O42A_RETURN;
	}

	const uint32_t length = value->length;

	if (!length) {
		O42A_RETURN;
	}

	o42a_array_t *const items = value->value.v_ptr;

	if (items) {
		O42A(o42a_gc_mark(o42a_gc_blockof(items)));
	}

	O42A_RETURN;
}

void o42a_array_start_use(o42a_val_t *const val) {
	O42A_ENTER(return);

	if (!(val->flags & O42A_VAL_CONDITION)) {
		O42A_RETURN;
	}
	if (!val->flags) {
		O42A_RETURN;
	}

	o42a_array_t *const array = val->value.v_ptr;

	if (!array) {
		O42A_RETURN;
	}

	O42A_DEBUG("Start array use: <0x%lx>\n", (long) array);
	O42A(o42a_gc_use(o42a_gc_blockof(array)));

	O42A_RETURN;
}

void o42a_array_end_use(o42a_val_t *const val) {
	O42A_ENTER(return);

	if (!(val->flags & O42A_VAL_CONDITION)) {
		O42A_RETURN;
	}
	if (!val->length) {
		O42A_RETURN;
	}

	o42a_array_t *const array = val->value.v_ptr;

	if (!array) {
		O42A_RETURN;
	}

	O42A_DEBUG("End array use: <0x%lx>\n", (long) array);
	O42A(o42a_gc_unuse(o42a_gc_blockof(array)));

	O42A_RETURN;
}
