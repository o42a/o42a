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
	((o42a_gc_block_t *) \
			(((void *) (mem)) - offsetof(struct _o42a_gc_block, data)))

#ifdef __cplusplus
extern "C" {
#endif

typedef struct o42a_gc_block o42a_gc_block_t;

/**
 * Garbage-collected data descriptor.
 *
 * Each data block has a descriptor contained in o42a_gc_block.desc field.
 */
typedef struct o42a_gc_desc {

	/**
	 * A garbage-collected data marker.
	 *
	 * This function is called by GC thread at a "mark" stage to mark all of the
	 * data blocks the given one refers to with a o42a_gc_mark calls.
	 *
	 * The data block is not locked when this function called. Implementation
	 * should be thread safe though.
	 *
	 * \data data pointer.
	 */
	void (*mark) (void *);

	/**
	 * A garbage-collected data sweeper.
	 *
	 * This function is called by GC at "sweep" stage right before the data
	 * block deallocation to release the resources used by it. For example, this
	 * function can decrease a reference counter of the referred data.
	 *
	 * The data is considered unused, so the implementation is not required to
	 * be thread-safe.
	 *
	 * \data data pointer.
	 */
	void (*sweep) (void *);

} o42a_gc_desc_t;


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

	/** A number of uses of this data block. */
	uint32_t use_count;

	/* Data descriptor. */
	const o42a_gc_desc_t *desc;

	/**
	 * Previous block in the same list, or NULL if this is a first one.
	 */
	o42a_gc_block_t *prev;

	/**
	 * Next block in the same list, or NULL if this is a last one.
	 */
	o42a_gc_block_t *next;

};

struct _o42a_gc_block {

	o42a_gc_block_t block;

	char data[0] __attribute__ ((aligned (__BIGGEST_ALIGNMENT__)));

};


/**
 * Allocates a garbage-collected block of data.
 *
 * The allocated block won't be added to any GC list automatically.
 * Use o42a_gc_use after filling the data to make GC aware of it.
 *
 * \param desc allocated data descriptor.
 * \param size the size of data to allocate, excluding the block header.
 *
 * \return data block pointer of NULL if allocation failed.
 */
o42a_gc_block_t *o42a_gc_block_alloc(const o42a_gc_desc_t *, size_t);

/**
 * Allocates a garbage-collected data.
 *
 * This function calls o42a_gc_balloc to allocate the data block and, in
 * contrast to it, returns a pointer to the allocated data instead of a pointer
 * to the data block.
 *
 * \param desc allocated data descriptor.
 * \param size the size of data to allocate.
 *
 * \return allocated data pointer or NULL if allocation failed.
 */
void *o42a_gc_alloc(const o42a_gc_desc_t *, size_t);

/**
 * Frees a garbage-collected data block previously allocated with o42a_gc_alloc
 * or o42a_gc_balloc.
 *
 * \param the data block to free.
 */
void o42a_gc_free(o42a_gc_block_t *);

/**
 * Locks the GC data block.
 */
void o42a_gc_lock_block(o42a_gc_block_t *);

/**
 * Unlocks the GC data block.
 */
void o42a_gc_unlock_block(o42a_gc_block_t *);

/**
 * Submits a statically allocated data block to GC.
 *
 * The data descriptor ("desc" field) should be assigned. All other fields
 * should be set to zero.
 *
 * This function should be called only once per static data block.
 *
 * \param data block.
 */
void o42a_gc_static(o42a_gc_block_t *);

/**
 * Discards a garbage-collected data block.
 *
 * This removes the block from GC. For example, it can be used to remove the
 * block from the list of static blocks.
 *
 * This function is unsafe and should not be used directly, unless you know what
 * you doing.
 */
void o42a_gc_discard(o42a_gc_block_t *);

/**
 * Declares the data block is used by current thread.
 *
 * While the data block is in use, it can not be considered a garbage and thus
 * can not be deallocated.
 *
 * This is an atomic operation. The block should not be locked by current
 * thread.
 */
void o42a_gc_use(o42a_gc_block_t *);

/**
 * Releases the use of data block.
 *
 * If this function releases the last use of the data block, then this data
 * block will became a subject of garbage collection.
 *
 * This is an atomic operation. The block should not be locked by current
 * thread.
 */
void o42a_gc_unuse(o42a_gc_block_t *);

/**
 * Informs the GC that there are data blocks to progress.
 *
 * This function should be called after one ore more o42a_gc_unuse calls.
 *
 * This functions starts the GC thread if necessary.
 */
void o42a_gc_signal();

#ifndef NDEBUG
/**
 * Run the GC manually, while there are blocks in a white list.
 */
void o42a_gc_run();
#endif /* NDEBUG */

/**
 * Marks the garbage-allocated data block and all the blocks it references
 * as used.
 *
 * This function is intended to be called by marker function (o42a_gc_desc.mark)
 * as a primary target of it's work.
 */
void o42a_gc_mark(o42a_gc_block_t *);

#ifdef __cplusplus
} /* externd "C" */
#endif

#endif /* O42A_MEMORY_GC_H */
