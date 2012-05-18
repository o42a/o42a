/*
    Run-Time Library
    Copyright (C) 2012 Ruslan Lopatin

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
#ifndef O42A_MEMORY_REFCOUNT_H
#define O42A_MEMORY_REFCOUNT_H

#include "o42a/types.h"


#define o42a_refcount_blockof(mem) \
	((o42a_refcount_block_t*) \
			(((void*) (mem)) - offsetof(struct _o42a_refcount_block, data)))


typedef struct o42a_refcount_block {

	uint64_t ref_count;

	char data[0];

} o42a_refcount_block_t;

struct _o42a_refcount_block {

	o42a_refcount_block_t block;

	char data[0] __attribute__ ((aligned (__BIGGEST_ALIGNMENT__)));

};

#ifdef __cplusplus
extern "C" {
#endif


o42a_refcount_block_t *o42a_refcount_alloc_block(O42A_DECLS size_t);

void o42a_refcount_free_block(O42A_DECLS o42a_refcount_block_t *);


void *o42a_refcount_alloc(O42A_DECLS size_t);

void o42a_refcount_free(O42A_DECLS void *);


#ifdef __cplusplus
}
#endif


#endif /* O42A_MEMORY_REFCOUNT_H */
