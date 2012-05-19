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
#ifndef O42A_MEMORY_GC_H
#define O42A_MEMORY_GC_H

#include "o42a/types.h"

/**
 * Obtains the garbage-allocated data block by data pointer.
 *
 * \param mem a pointer to the data start.
 */
#define o42a_gc_blockof(mem) \
	((o42a_gc_block_t*) \
			(((void*) (mem)) - offsetof(struct _o42a_gc_block, data)))

typedef struct o42a_gc_block o42a_gc_block_t;

typedef struct o42a_gc_use o42a_gc_use_t;

/**
 * A garbage-collected block of data.
 *
 * The data itself resides in memory after this structure and is aligned by the
 * biggest alignment.
 */
struct o42a_gc_block {

	/**
	 * An atomic (spin) lock of this block.
	 *
	 * A lock should be acquired with o42a_gc_lock_block prior to any operations
	 * on this block and released after that with o42a_gc_unlock_block.
	 */
	uint8_t lock;

	/** A GC list identifier this block belongs to. */
	uint8_t list;

	/** Block status flags. */
	uint16_t flags;

	/** A data describing the details of this block belonging to a GC list. */
	union {

		/**
		 * This variant is used when the block belongs to a general GC list.
		 */
		struct {

			/**
			 * Previous block in the same list, or NULL if this is a first one.
			 */
			o42a_gc_block_t *prev;

			/**
			 * Next block in the same list, or NULL if this is a last one.
			 */
			o42a_gc_block_t *next;

		} list;

		/**
		 * This variant is used when the block is used by some thread.
		 */
		struct {

			/**
			 * A pointer to the first block use descriptor.
			 */
			o42a_gc_use_t *first;

		} uses;

	} belonging;

};

/**
 * The descriptor of the use of data block by some thread.
 *
 * This structure should be allocated on stack and the block field should be
 * filled with a pointer to the block to use. The use can be claimed with
 * o42a_gc_use function and freed with o42a_gc_unuse one.
 *
 * The same block can be used by multiple threads, thats one a list is needed.
 */
struct o42a_gc_use {

	/**
	 * The used data block.
	 */
	o42a_gc_block_t *block;

	/**
	 * Previous use descriptor in the list, or NULL if this is a first one.
	 */
	o42a_gc_use_t *prev;

	/**
	 * Next use descriptor in the list, or NULL if this is a last one.
	 */
	o42a_gc_use_t *next;

};

struct _o42a_gc_block {

	o42a_gc_block_t block;

	char data[0] __attribute__ ((aligned (__BIGGEST_ALIGNMENT__)));

};

#ifdef __cplusplus
extern "C" {
#endif

/**
 * Allocates a garbage-collected block of data.
 *
 * The allocated block won't be added to any GC list automatically.
 * Use o42a_gc_use right after allocation in order to do that.
 *
 * \param size the size of data to allocate, excluding the block header.
 *
 * \return data block pointer of NULL if allocation failed.
 */
o42a_gc_block_t *o42a_gc_block_alloc(O42A_DECLS size_t);

/**
 * Allocates a garbage-collected data.
 *
 * This function calls o42a_gc_balloc to allocate the data block and, in
 * contrast to it, returns a pointer to the allocated data instead of a pointer
 * to the data block.
 *
 * \param size the size of data to allocate.
 *
 * \return allocated data pointer or NULL if allocation failed.
 */
void *o42a_gc_alloc(O42A_DECLS size_t);

/**
 * Frees a garbage-collected data block previously allocated with o42a_gc_alloc
 * or o42a_gc_balloc.
 *
 * \param the data block to free.
 */
void o42a_gc_free(O42A_DECLS o42a_gc_block_t *);

/**
 * Locks the GC data block.
 */
void o42a_gc_lock_block(O42A_DECLS o42a_gc_block_t *);

/**
 * Unlocks the GC data block.
 */
void o42a_gc_unlock_block(O42A_DECLS o42a_gc_block_t *);

/**
 * Declares the data block is used by current thread.
 *
 * While the data block is in use, it can not be considered a garbage and thus
 * can not be deallocated.
 *
 * This is an atomic operation. The block should not be locked by current
 * thread.
 */
void o42a_gc_use(O42A_DECLS o42a_gc_use_t *);

/**
 * Releases the use of data block.
 *
 * If this function releases the last use of the data block, then this data
 * block will be submitted to GC and thus became a subject of garbage
 * collection.
 *
 * This is an atomic operation. The block should not be locked by current
 * thread.
 */
void o42a_gc_unuse(O42A_DECLS o42a_gc_use_t *);

#ifdef __cplusplus
} /* externd "C" */
#endif


#endif /* O42A_MEMORY_GC_H */
