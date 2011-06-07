/*
    Run-Time Library
    Copyright (C) 2011 Ruslan Lopatin

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
#include <stdlib.h>

#include "o42a/error.h"
#include "o42a/memory.h"


o42a_mem_block_t *o42a_mem_alloc_rc_block(O42A_PARAMS const size_t size) {
	O42A_ENTER(return NULL);

	o42a_mem_block_t *const block =
			O42A(malloc(sizeof(o42a_mem_block_t) + size));

	if (!block) {
		O42A(o42a_error_print(O42A_ARGS "Can not allocate memory\n"));
		exit(EXIT_FAILURE);
	}

	block->flags = O42A_MEM_RC;
	block->hdr.rc.ref_count = 0;

	O42A_RETURN block;
}

o42a_mem_block_t *o42a_mem_alloc_gc_block(O42A_PARAMS const size_t size) {
	O42A_ENTER(return NULL);

	o42a_mem_block_t *const block =
			O42A(malloc(sizeof(o42a_mem_block_t) + size));

	if (!block) {
		O42A(o42a_error_print(O42A_ARGS "Can not allocate memory\n"));
		exit(EXIT_FAILURE);
	}

	block->flags = O42A_MEM_GC;

	O42A_RETURN block;
}

void o42a_mem_free_block(O42A_PARAMS o42a_mem_block_t *const block) {
	O42A_ENTER(return);

	O42A(free(block));

	O42A_RETURN;
}


inline void *o42a_mem_alloc_rc(O42A_PARAMS const size_t size) {
	O42A_ENTER(return NULL);

	o42a_mem_block_t *const block =
			O42A(o42a_mem_alloc_rc_block(O42A_ARGS size));

	O42A_RETURN (void*) &block->data;
}

inline void *o42a_mem_alloc_gc(O42A_PARAMS const size_t size) {
	O42A_ENTER(return NULL);

	o42a_mem_block_t *const block =
			O42A(o42a_mem_alloc_gc_block(O42A_ARGS size));

	O42A_RETURN (void*) &block->data;
}

inline void o42a_mem_free(O42A_PARAMS void *const mem) {
	O42A_ENTER(return);

	O42A(o42a_mem_free(O42A_ARGS o42a_mem_block(mem)));

	O42A_RETURN;
}
