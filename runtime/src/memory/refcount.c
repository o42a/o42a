/*
    Run-Time Library
    Copyright (C) 2012 Ruslan Lopatin

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
#include "o42a/memory/refcount.h"

#include <stdlib.h>

#include "o42a/error.h"


o42a_refcount_block_t *o42a_refcount_balloc(const size_t size) {
	O42A_ENTER(return NULL);

	o42a_refcount_block_t *const block =
			O42A(malloc(sizeof(struct _o42a_refcount_block) + size));

	if (!block) {
		O42A(o42a_error_print("Can not allocate memory\n"));
		exit(EXIT_FAILURE);
	}

	block->ref_count = 0;

	O42A_RETURN block;
}

inline void *o42a_refcount_alloc(const size_t size) {
	O42A_ENTER(return NULL);

	struct _o42a_refcount_block *const block =
			(struct _o42a_refcount_block *)
			O42A(o42a_refcount_balloc(size));

	O42A_RETURN (void*) &block->data;
}

void o42a_refcount_free(o42a_refcount_block_t *const block) {
	O42A_ENTER(return);

	O42A(free(block));

	O42A_RETURN;
}
