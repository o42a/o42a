/*
    Copyright (C) 2012-2014 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
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
