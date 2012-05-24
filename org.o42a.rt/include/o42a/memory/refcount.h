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

/**
 * Obtains the reference-counted data block by data pointer.
 *
 * \param mem a pointer to the data start.
 */
#define o42a_refcount_blockof(mem) \
	((o42a_refcount_block_t*) \
			(((void*) (mem)) - offsetof(struct _o42a_refcount_block, data)))

/**
 * A reference-counted block of data.
 *
 * The data itself resides in memory after this structure and is aligned by the
 * biggest alignment.
 */
typedef struct o42a_refcount_block {

	/**
	 * The number of times this data block is used.
	 *
	 * This counter should be increased or decreased by atomic operations.
	 *
	 * Once this counter reaches zero, the data block should be freed with
	 * o42a_refcount_free.
	 */
	uint64_t ref_count;

} o42a_refcount_block_t;

struct _o42a_refcount_block {

	o42a_refcount_block_t block;

	char data[0] __attribute__ ((aligned (__BIGGEST_ALIGNMENT__)));

};

#ifdef __cplusplus
extern "C" {
#endif

/**
 * Allocates a reference-counted block of data.
 *
 * The allocated block use count shall be zero.
 *
 * \param size the size of data to allocate, excluding the block header.
 *
 * \return data block pointer of NULL if allocation failed.
 */
o42a_refcount_block_t *o42a_refcount_balloc(size_t);

/**
 * Allocates a reference-counted data.
 *
 * This function calls o42a_refcount_balloc to allocate the data block and, in
 * contrast to it, returns a pointer to the allocated data instead of a pointer
 * to the data block.
 *
 * \param size the size of data to allocate.
 *
 * \return allocated data pointer or NULL if allocation failed.
 */
void *o42a_refcount_alloc(size_t);

/**
 * Frees a reference-counted data block previously allocated with
 * o42a_refcount_alloc or o42a_refcount_balloc.
 *
 * \param the data block to free.
 */
void o42a_refcount_free(o42a_refcount_block_t *);

#ifdef __cplusplus
} /* extern "C" */
#endif


#endif /* O42A_MEMORY_REFCOUNT_H */
