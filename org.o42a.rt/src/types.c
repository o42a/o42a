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
#include "o42a/types.h"

#include "o42a/memory/refcount.h"


/**
 * Size bit-mask within layout value.
 *
 * Highest three bits contains the number of bits to shift 1 left to gain
 * alignment. The rest is size shifted right by value stored in highest three
 * bits. I.e. size is always a multiple of alignment.
 */
static const size_t SIZE_MASK = 0x1FFFFFFF;

static const size_t ALIGNMENT_MASK = 0xFFFFFFFF & ~0x1FFFFFFF;

static const size_t ALL_ONES = -1;

static inline size_t alignment_shift(
		O42A_PARAMS
		const o42a_layout_t layout) {
	return (layout & ALIGNMENT_MASK) >> 29;
}

inline size_t o42a_layout_size(O42A_PARAMS const o42a_layout_t layout) {
	return layout & SIZE_MASK;
}

inline size_t o42a_layout_array_size(
		O42A_PARAMS
		const o42a_layout_t layout,
		const size_t num_elements) {

	const size_t element_size = o42a_layout_pad(
			O42A_ARGS_
			o42a_layout_size(O42A_ARGS_ layout),
			layout);

	return element_size * num_elements;
}

inline o42a_layout_t o42a_layout_array(
		O42A_PARAMS
		const o42a_layout_t layout,
		const size_t num_elements) {
	return o42a_layout_array_size(O42A_ARGS_ layout, num_elements)
			| (layout & ALIGNMENT_MASK);
}

inline uint8_t o42a_layout_alignment(O42A_PARAMS const o42a_layout_t layout) {
	return 1 << alignment_shift(O42A_ARGS_ layout);
}

inline size_t o42a_layout_offset(
		O42A_PARAMS
		const size_t start,
		const o42a_layout_t layout) {

	const uint8_t ashift = alignment_shift(O42A_ARGS_ layout);
	const size_t remainder = start & ~(ALL_ONES << ashift);

	return remainder ? (1 << ashift) - remainder : 0;
}

inline size_t o42a_layout_pad(
		O42A_PARAMS
		const size_t start,
		const o42a_layout_t layout) {
	return start + o42a_layout_offset(O42A_ARGS_ start, layout);
}

o42a_layout_t o42a_layout_both(
		O42A_PARAMS
		const o42a_layout_t layout1,
		const o42a_layout_t layout2) {

	const uint8_t al1 = o42a_layout_alignment(O42A_ARGS_ layout1);
	const uint8_t al2 = o42a_layout_alignment(O42A_ARGS_ layout2);

	return o42a_layout(
			O42A_ARGS_
			al1 >= al2 ? al1 : al2,
			o42a_layout_pad(
					O42A_ARGS_
					o42a_layout_size(O42A_ARGS_ layout1),
					layout2)
			+ o42a_layout_size(O42A_ARGS_ layout2));
}

o42a_layout_t o42a_layout(
		O42A_PARAMS
		const uint8_t alignment,
		const size_t size) {

	uint8_t ashift = 0;
	const int diff = ((int) alignment) - 4;

	if (diff <= 0) {
		if (!diff) {
			ashift = 2;
		} else if (alignment == 2) {
			ashift = 1;
		}
	} else {
		switch (alignment) {
		case 8: ashift = 3; break;
		case 16: ashift = 4; break;
		case 32: ashift = 5; break;
		case 64: ashift = 6; break;
		case 128: ashift = 7; break;
		}
	}

	return size | (ashift << 29);
}


inline size_t o42a_val_ashift(O42A_PARAMS const o42a_val_t *const val) {
	return (val->flags & O42A_VAL_ALIGNMENT_MASK) >> 8;
}

inline size_t o42a_val_alignment(O42A_PARAMS const o42a_val_t *const val) {
	return 1 << o42a_val_ashift(O42A_ARGS_ val);
}

inline void *o42a_val_data(O42A_PARAMS const o42a_val_t *const val) {
	if (val->flags & O42A_VAL_EXTERNAL) {
		return val->value.v_ptr;
	}
	return (void*) &val->value;
}


inline void o42a_val_use(O42A_PARAMS o42a_val_t *const val) {
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

inline void o42a_val_unuse(O42A_PARAMS o42a_val_t *const val) {
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
		O42A(o42a_refcount_free(O42A_ARGS block));
	}

	O42A_RETURN;
}
