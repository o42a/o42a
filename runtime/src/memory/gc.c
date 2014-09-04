/*
    Copyright (C) 2012-2014 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#define O42A_DEBUG_TYPE O42A_DBG_TYPE_GC

#include "o42a/memory/gc.h"

#include <assert.h>
#include <pthread.h>
#include <sched.h>
#include <stdlib.h>

#include "o42a/error.h"


/**
 * GC list identifiers.
 *
 * These values can be used to fill o42a_gc_block.list field.
 */
enum o42a_gc_lists {

	/** Statically-allocated block does not belong to any list yet. */
	O42A_GC_LIST_NEW_STATIC = 0,

	/** Newly allocated block does not belong to any list yet. */
	O42A_GC_LIST_NEW_ALLOCATED = 1,

	/** The block have been removed from list. */
	O42A_GC_LIST_NONE = 2,

	/**
	 * A "grey" list flag. The "grey" list is a list of data objects known to be
	 * used and thus couldn't be deallocated.
	 */
	O42A_GC_LIST_GREY_FLAG = 4,

	/**
	 * A list of data blocks used by some thread(s).
	 *
	 * A data block from this list travels to the "white" list once no longer
	 * used.
	 *
	 * This, along with O42A_LIST_STATIC forms a "grey" GC list.
	 */
	O42A_GC_LIST_USED = O42A_GC_LIST_GREY_FLAG | 0,

	/**
	 * A list of statically allocated data blocks.
	 *
	 * This, along with O42A_LIST_USED forms a "grey" GC list.
	 */
	O42A_GC_LIST_STATIC = O42A_GC_LIST_GREY_FLAG | 1,

	/**
	 * A "while" list flag of all the remaining data blocks.
	 *
	 * There are two such lists: an "even" and an "odd" one. The actual list
	 * identifier can be constructed by logical OR of this flag and an oddity
	 * bit.
	 *
	 * These list contain a data blocks not present in any of the previous
	 * ones and not considered as used by current GC iteration.
	 *
	 * Two "white" lists are handled by GC in turn, one at a time. For each list
	 * GC sequentially performs a "mark" pass, and then - a "sweep" one.
	 *
	 * At the "mark" GC pass, each data block encountered used travels to the
	 * list's counterpart, i.e. from "even" to "odd" one and vice versa.
	 *
	 * At the "sweep" GC pass, all the data blocks remaining in the list
	 * are freed.
	 *
	 * After that the GC switches to the list's counterpart and performs
	 * the steps again.
	 */
	O42A_GC_LIST_WHITE_FLAG = 6,

	/**
	 * An "even" "white" list.
	 */
	O42A_GC_LIST_EVEN = O42A_GC_LIST_WHITE_FLAG | 0,

	/**
	 * An "odd" "white" list.
	 */
	O42A_GC_LIST_ODD = O42A_GC_LIST_WHITE_FLAG | 1,

	/**
	 * The first list identifier.
	 *
	 * The identifiers form a contiguous sequence up to O42A_GC_LIST_MAX.
	 */
	O42A_GC_LIST_MIN = O42A_GC_LIST_USED,

	/**
	 * The last list identifier.
	 */
	O42A_GC_LIST_MAX = O42A_GC_LIST_ODD

};

/**
 * A garbage-collected data block flags.
 *
 * These flags are used to fill the o42a_gc_block.flags field.
 */
enum o42a_gc_block_flags {

	/**
	 * The data block is checked at some iteration.
	 *
	 * Shift this flag left by oddity bits to get the flag value for the
	 * corresponding "even" or "odd" list.
	 */
	O42A_GC_BLOCK_CHECKED = 0x01,

	/**
	 * The data blocks links to other data blocks.
	 *
	 * This is only valid for O42A_GC_LIST_USED list. The block with this flag
	 * set won't be deallocated even though the block's use count is zero.
	 *
	 * This flag is set by gc_link and dropped as soon as the block
	 * is used for the first time.
	 */
	O42A_GC_BLOCK_LINKING = 0x04,

};

typedef struct o42a_gc_list {

	o42a_gc_block_t *first;

	o42a_gc_block_t *last;

} o42a_gc_list_t;


static inline void* o42a_gc_dataof(o42a_gc_block_t *block) {
	struct _o42a_gc_block *const blk = (struct _o42a_gc_block *) block;
	return (void*) &blk->data;
}

#ifndef NDEBUG
static inline size_t gc_dump_offset(size_t size) {

	const size_t mem_size = sizeof(struct _o42a_gc_block) + size;
	const o42a_layout_t dump_layout = O42A_LAYOUT(o42a_dbg_stack_dump_t);

	return o42a_layout_pad(mem_size, dump_layout);
}
#endif /* NDEBUG */

static inline o42a_gc_block_t *gc_balloc(
		const o42a_gc_desc_t *const desc,
		const size_t size) {
	O42A_ENTER(return NULL);

	size_t mem_size = sizeof(struct _o42a_gc_block) + size;

#ifndef NDEBUG
	const o42a_layout_t dump_layout = O42A_LAYOUT(o42a_dbg_stack_dump_t);
	o42a_dbg_stack_dump_t stack_dump = o42a_dbg_stack_dump(2);
	const size_t dump_offset = gc_dump_offset(size);
	mem_size = dump_offset + stack_dump.size;
#endif /* NDEBUG */

	o42a_gc_block_t *const block = O42A(malloc(mem_size));

	if (!block) {
		O42A(o42a_error_print("Can not allocate memory\n"));
		exit(EXIT_FAILURE);
	}

	block->lock = 0;
	block->list = O42A_GC_LIST_NEW_ALLOCATED;
	block->flags = 0;
	block->use_count = 0;
	block->desc = desc;
	block->prev = NULL;
	block->next = NULL;
	block->size = size;

#ifndef NDEBUG
	o42a_dbg_fill_stack_dump(&stack_dump, ((char *) block) + dump_offset);
	O42A_DEBUG("Allocated: %#lx (%zd bytes)\n", (long) block, size);
#endif /* NDEBUG */

	O42A_RETURN block;
}

o42a_gc_block_t *o42a_gc_balloc(
		const o42a_gc_desc_t *const desc,
		const size_t size) {
	O42A_ENTER(return NULL);
	o42a_gc_block_t *const block = gc_balloc(desc, size);
	O42A_RETURN block;
}

inline void *o42a_gc_alloc(
		const o42a_gc_desc_t *const desc,
		const size_t size) {
	O42A_ENTER(return NULL);
	o42a_gc_block_t *const block = gc_balloc(desc, size);
	O42A_RETURN o42a_gc_dataof(block);
}

#define valid_lock(_block) !(_block->lock & ~1)

inline void o42a_gc_free(o42a_gc_block_t *const block) {
	O42A_ENTER(return);
	O42A_DEBUG("Free: %#lx\n", (long) block);
	assert(valid_lock(block) && "Wrong GC block");
	assert((block->list >= O42A_GC_LIST_MIN
			|| block->list == O42A_GC_LIST_NONE)
			&& "Wrong GC list identifier");
	assert(block->list <= O42A_GC_LIST_MAX && "Wrong GC list identifier");
	assert(
			block->list != O42A_GC_LIST_NEW_STATIC
			&& "Attempt to free a static memory block");
	assert(
			block->list != O42A_GC_LIST_STATIC
			&& "Attempt to free a static memory block");
#ifndef NDEBUG
	block->lock = ~0;
	block->list = ~0;
	if (o42a_debug_ison) {
		o42a_debug("Allocated at:\n");
		o42a_dbg_print_stack_dump(
				((char *) block) + gc_dump_offset(block->size));
	}
#endif /* NDEBUG */
	O42A(free(block));
	O42A_RETURN;
}

inline void o42a_gc_lock_block(o42a_gc_block_t *const block) {
	O42A_ENTER(return);
	O42A_DEBUG("Lock block: %#lx\n", (long) block);
	assert(valid_lock(block) && "Wrong GC block");
	while (__sync_lock_test_and_set(&block->lock, 1)) {
		O42A(sched_yield());
	}
	O42A_RETURN;
}

inline void o42a_gc_unlock_block(o42a_gc_block_t *const block) {
	O42A_ENTER(return);
	O42A_DEBUG("Unlock block: %#lx\n", (long) block);
	assert(valid_lock(block) && "Wrong GC block");
	__sync_lock_release(&block->lock);
	O42A_RETURN;
}


/**
 * GC lock.
 *
 * Should be acquired with o42a_gc_lock for GC list modifications
 * or oddity switch.
 */
static int gc_locked;

/**
 * Locks GC for list modifications.
 */
static inline void gc_lock() {
	O42A_ENTER(return);
	while (__sync_lock_test_and_set(&gc_locked, 1)) {
		O42A(sched_yield());
	}
	O42A_RETURN;
}

/**
 * Unlocks GC previously locked with gc_lock.
 */
static inline void gc_unlock() {
	O42A_ENTER(return);
	__sync_lock_release(&gc_locked);
	O42A_RETURN;
}

typedef struct gc_read gc_read_t;

struct gc_read {

	o42a_bool_t read;
	gc_read_t *prev;
	gc_read_t *next;
	void **const var;
	o42a_gc_block_reader_ft *const reader;
	void *result;

};

static int gc_reads_lock;
static gc_read_t *gc_first_read;
static gc_read_t *gc_last_read;

static inline void gc_lock_reads() {
	O42A_ENTER(return);
	while (__sync_lock_test_and_set(&gc_reads_lock, 1)) {
		O42A(sched_yield());
	}
	O42A_RETURN;
}

static inline void gc_unlock_reads() {
	O42A_ENTER(return);
	__sync_lock_release(&gc_reads_lock);
	O42A_RETURN;
}

static inline void *gc_read(gc_read_t *const read) {
	O42A_ENTER(return NULL);

	void *result;

	if (read->read) {
		// Already read.
		result = read->result;
	} else {
		// Read the pointer.
		result = *read->var;
		read->result = result;
		read->read = O42A_TRUE;

		// Unregister the read.
		gc_read_t *const prev = read->prev;
		gc_read_t *const next = read->next;

		if (next) {
			next->prev = prev;
		} else {
			gc_last_read = prev;
		}
		if (prev) {
			prev->next = next;
		} else {
			gc_first_read = next;
		}
	}

	O42A_RETURN result;
}

/**
 * Next GC oddity, i.e. which of the "white" lists will be processed next.
 *
 * This can be either 0 or 1.
 *
 * Current oddity can be calculated as gc_next_oddity ^ 1.
 */
static unsigned gc_next_oddity;


static o42a_gc_list_t gc_lists[4];

static inline void gc_block_uncheck(o42a_gc_block_t *block) {
	O42A_ENTER(return);
	block->flags &= ~(O42A_GC_BLOCK_CHECKED << gc_next_oddity);
	O42A_RETURN;
}

static inline void gc_list_add(
		o42a_gc_block_t *const block,
		const uint8_t list_id) {
	O42A_ENTER(return);

	assert(
			block->list < O42A_GC_LIST_MIN
			&& "Block already belongs to another GC list");
	assert(list_id >= O42A_GC_LIST_MIN && "Wrong GC list identifier");
	assert(list_id <= O42A_GC_LIST_MAX && "Wrong GC list identifier");
	assert(
			(list_id != O42A_GC_LIST_STATIC
					|| block->list == O42A_GC_LIST_NEW_STATIC)
			&& "Can not add a non-static block to the static list");
	assert(
			(list_id == O42A_GC_LIST_STATIC
					|| block->list != O42A_GC_LIST_NEW_STATIC)
			&& "Can not add the static block to a non-static list");

	o42a_gc_list_t *const list = &gc_lists[list_id - O42A_GC_LIST_MIN];
	o42a_gc_block_t *const last = list->last;

	block->list = list_id;
	block->prev = last;
	block->next = NULL;
	if (!last) {
		list->first = block;
	} else {
		last->next = block;
	}
	list->last = block;

	O42A_RETURN;
}

static inline void gc_list_remove(o42a_gc_block_t *const block) {
	O42A_ENTER(return);

	assert(
			block->list >= O42A_GC_LIST_MIN
			&& block->list <= O42A_GC_LIST_MAX
			&& "Wrong GC list identifier");

	o42a_gc_list_t *const list = &gc_lists[block->list - O42A_GC_LIST_MIN];
	o42a_gc_block_t *const prev = block->prev;
	o42a_gc_block_t *const next = block->next;

	block->list = O42A_GC_LIST_NONE;
	if (next) {
		next->prev = prev;
	} else {
		list->last = prev;
	}
	if (prev) {
		prev->next = next;
	} else {
		list->first = next;
	}

	O42A_RETURN;
}

static inline void gc_static(o42a_gc_block_t *const block) {
	O42A_ENTER(return);

	O42A(gc_lock());
	O42A(gc_list_add(block, O42A_GC_LIST_STATIC));
	O42A(gc_unlock());

	O42A_RETURN;
}

static inline void gc_link(o42a_gc_block_t *const block) {
	O42A_ENTER(return);

	O42A_DEBUG("Link: %#lx\n", (long) block);
	if (block->list == O42A_GC_LIST_NEW_STATIC) {
		// New static block. Register it.
		gc_static(block);
	} else if (block->list == O42A_GC_LIST_NEW_ALLOCATED) {
		// Newly allocated block.
		// Mark it as linking and move it to the list of used blocks.
		O42A_DEBUG("Linking block: %#lx\n", (long) block);
		block->flags |= O42A_GC_BLOCK_LINKING;
		O42A(gc_lock());
		O42A(gc_list_add(block, O42A_GC_LIST_USED));
		O42A(gc_unlock());
	}

	O42A_RETURN;
}

void o42a_gc_link(o42a_gc_block_t *const block) {
	O42A_ENTER(return);
	O42A(o42a_gc_lock_block(block));
	gc_link(block);
	O42A(o42a_gc_unlock_block(block));
	O42A_RETURN;
}

static o42a_bool_t gc_mark(o42a_gc_block_t *block) {
	O42A_ENTER(return O42A_FALSE);

	unsigned next_oddity = gc_next_oddity;
	unsigned oddity = next_oddity ^ 1;

	// Drop the checked flag for the next oddity and mark checked for this one.
	O42A(gc_block_uncheck(block));

	const int checked = O42A_GC_BLOCK_CHECKED << oddity;

	if (block->flags & checked) {
		// Already checked
		O42A_RETURN O42A_FALSE;
	}

	block->flags |= checked;

	// Move to the next oddity's white list.
	if (block->list == (O42A_GC_LIST_WHITE_FLAG | oddity)) {
		O42A(gc_lock());
		O42A(gc_list_remove(block));
		O42A(gc_list_add(block, O42A_GC_LIST_WHITE_FLAG | next_oddity));
		O42A(gc_unlock());
	}

	O42A_RETURN O42A_TRUE;
}

static o42a_bool_t gc_thread_exists;

static volatile o42a_bool_t gc_has_white;

static pthread_t gc_thread;

static pthread_mutex_t gc_mutex = PTHREAD_MUTEX_INITIALIZER;

static pthread_cond_t gc_cond = PTHREAD_COND_INITIALIZER;

static inline void gc_thread_mark_used(
		o42a_gc_block_t *const first,
		o42a_gc_block_t *const last,
		const unsigned oddity) {
	O42A_ENTER(return);

	o42a_gc_block_t *block;

	O42A(gc_lock());
	block = first;
	O42A(gc_unlock());

	while (1) {

		o42a_gc_block_t *next;

		O42A(o42a_gc_lock_block(block));

		next = block->next;
		if (!block->use_count && !(block->flags & O42A_GC_BLOCK_LINKING)) {
			// Block were used, but its use count dropped to zero.
			// Move it to the next oddity "white" list.
			// The next oddity should be used, because this block can be linked
			// by the block, which was added while garbage collection is in
			// process. Such block won't be processed during current iteration,
			// an thus we can't say for sure whether this block is linked to
			// until the next iteration.
			assert(block->list == O42A_GC_LIST_USED);

			O42A(gc_lock());

			O42A_DEBUG("Not used any more: %#lx\n", (long) block);
			O42A(gc_list_remove(block));
			O42A(gc_block_uncheck(block));
			O42A(gc_list_add(
					block,
					O42A_GC_LIST_WHITE_FLAG | (oddity ^ 1)));

			// Process the next oddity unconditionally.
			gc_has_white = O42A_TRUE;

			O42A(gc_unlock());

			O42A(o42a_gc_unlock_block(block));
		} else {

			// Mark the block.
			o42a_bool_t mark = O42A(gc_mark(block));

			O42A(o42a_gc_unlock_block(block));

			// Mark the referenced data.
			if (mark) {
				O42A(block->desc->mark(o42a_gc_dataof(block)));
			}
		}

		if (block == last) {
			break;
		}
		block = next;
	}

	O42A_RETURN;
}

static inline void gc_thread_mark_static(
		o42a_gc_block_t *const first,
		o42a_gc_block_t *const last) {
	O42A_ENTER(return);

	o42a_gc_block_t *block;

	O42A(gc_lock());
	block = first;
	O42A(gc_unlock());

	while (1) {
		assert(block->list == O42A_GC_LIST_STATIC);

		o42a_gc_block_t *next;
		o42a_bool_t mark;

		// Mark the static block.
		O42A(o42a_gc_lock_block(block));
		next = block->next;
		mark = O42A(gc_mark(block));
		O42A(o42a_gc_unlock_block(block));

		// Mark the referenced data.
		if (mark) {
			O42A(block->desc->mark(o42a_gc_dataof(block)));
		}

		if (block == last) {
			break;
		}
		block = next;
	}

	O42A_RETURN;
}

static inline void gc_thread_mark_reads() {
	O42A_ENTER(return);

	while (1) {

		// Perform the first mutable read and remove it from the list.
		O42A(gc_lock_reads());
		gc_read_t *const read = gc_first_read;
		void *address;
		o42a_gc_block_reader_ft *reader;
		if (read) {
			address = O42A(gc_read(read));
			reader = read->reader;
		}
		O42A(gc_unlock_reads());

		if (!read) {
			// No pending reads.
			O42A_RETURN;
		}

		if (address) {
			// Mark the read block.
			O42A(o42a_gc_mark(reader(address)));
		}
	}

	O42A_RETURN;
}

static inline void gc_thread_sweep(unsigned oddity) {
	O42A_ENTER(return);

	o42a_gc_list_t *const list =
			gc_lists + (O42A_GC_LIST_WHITE_FLAG | oddity) - O42A_GC_LIST_MIN;
	// No need to synchronize, as the white list entries are not referenced.
	o42a_gc_block_t *block = list->first;

	if (!block) {
		// White list is empty.
		O42A_RETURN;
	}
	do {

		o42a_gc_block_t *next = block->next;

		O42A(block->desc->sweep(o42a_gc_dataof(block)));
		O42A(o42a_gc_free(block));

		block = next;
	} while (block);

	// Clean the white list.
	list->first = NULL;
	list->last = NULL;

	O42A_RETURN;
}

static inline void gc_mark_and_sweep() {
	O42A_ENTER(return);

	o42a_gc_list_t *const used_list =
			gc_lists + O42A_GC_LIST_USED - O42A_GC_LIST_MIN;
	o42a_gc_list_t *const static_list =
			gc_lists + O42A_GC_LIST_STATIC - O42A_GC_LIST_MIN;

	O42A(gc_lock());

	unsigned oddity = gc_next_oddity;

	gc_next_oddity = oddity ^ 1;

	o42a_gc_block_t *const first_used = used_list->first;
	o42a_gc_block_t *const last_used = used_list->last;
	o42a_gc_block_t *const first_static = static_list->first;
	o42a_gc_block_t *const last_static = static_list->last;

	O42A(gc_unlock());

	if (first_used) {
		O42A(gc_thread_mark_used(first_used, last_used, oddity));
	}
	if (first_static) {
		O42A(gc_thread_mark_static(first_static, last_static));
	}
	O42A(gc_thread_mark_reads());
	O42A(gc_thread_sweep(oddity));

	O42A_RETURN;
}

#if !defined(NDEBUG) || defined(O42A_GC_SYNC)
void o42a_gc_run() {
	O42A_ENTER(return);
	O42A_DO("Mark and sweep");

	size_t cycles = 0;

	while (gc_has_white) {
		O42A_DEBUG("GC oddity: %d\n", gc_next_oddity);
		assert(cycles <= 1 && "Infinite GC loop");
		++cycles;
		gc_has_white = O42A_FALSE;
		O42A(gc_mark_and_sweep());
	}

	O42A_DONE;
	O42A_RETURN;
}
#endif /* !NDEBUG || O42A_GC_SYNC */

static void *o42a_gc_thread(void *data __attribute__((unused))) {
	O42A_START_THREAD("GC");
	O42A_ENTER(return NULL);

	while (1) {
		O42A(pthread_mutex_lock(&gc_mutex));
		while (!gc_has_white) {
			O42A(pthread_cond_wait(&gc_cond, &gc_mutex));
		}
		gc_has_white = O42A_FALSE;
		O42A(pthread_mutex_unlock(&gc_mutex));

		gc_mark_and_sweep();
	}

	O42A_RETURN NULL;
}

void o42a_gc_signal() {
	O42A_ENTER(return);
#ifdef O42A_GC_SYNC
	O42A_RETURN;
#endif /* O42A_GC_SYNC */
	if (!gc_has_white) {
		// Nothing to report.
		O42A_RETURN;
	}

	O42A(pthread_mutex_lock(&gc_mutex));
	if (gc_thread_exists) {
		// Wake up the to GC thread.
		O42A(pthread_cond_signal(&gc_cond));
	} else if (O42A(pthread_create(&gc_thread, NULL, &o42a_gc_thread, NULL))) {
		o42a_error_print("Failed to create a GC thread");
	} else {
		// GC thread created.
		gc_thread_exists = O42A_TRUE;
	}
	O42A(pthread_mutex_unlock(&gc_mutex));

	O42A_RETURN;
}

int o42a_gc_static(o42a_gc_block_t *const block) {
	O42A_ENTER(0);

	if (block->list == O42A_GC_LIST_STATIC) {
		// The block already registered. Do nothing.
		O42A_DEBUG("Static block: %#lx\n", (long) block);
		O42A_RETURN -1;
	}

	O42A(o42a_gc_lock_block(block));

	if (block->list == O42A_GC_LIST_STATIC) {
		// Second check after the memory barrier.
		O42A_DEBUG("Static block: %#lx\n", (long) block);
		O42A(o42a_gc_unlock_block(block));
		O42A_RETURN -1;
	}

	O42A_DEBUG("Register static block: %#lx\n", (long) block);
	assert(block->list == O42A_GC_LIST_NEW_STATIC && "Block is not static");

	O42A(gc_static(block));

	O42A(o42a_gc_unlock_block(block));

	O42A_RETURN 1;
}

void o42a_gc_discard(o42a_gc_block_t *const block) {
	O42A_ENTER(return);

	if (!block->list) {
		O42A_RETURN;
	}

	O42A(gc_lock());
	O42A(gc_list_remove(block));
	O42A(gc_unlock());

	O42A_RETURN;
}

o42a_bool_t o42a_gc_use(o42a_gc_block_t *const block) {
	O42A_ENTER(return O42A_FALSE);

	O42A_DEBUG("Use block: %#lx\n", (long) block);

	if (block->list == O42A_GC_LIST_STATIC) {
		O42A_DEBUG("Don't use a static block %#lx\n", (long) block);
		// Block already registered. Do nothing.
		O42A_RETURN O42A_FALSE;
	}

	O42A(o42a_gc_lock_block(block));
	assert(block->list <= O42A_GC_LIST_MAX && "Wrong GC list");
	assert(block->list != O42A_GC_LIST_NONE && "Illegal block state");

	switch (block->list) {
	case O42A_GC_LIST_NEW_STATIC:
		// Static block not initialized yet. Add to static list.
		O42A_DEBUG(
				"Register static block %#lx and don't use it\n",
				(long) block);
		O42A(gc_static(block));
		O42A(o42a_gc_unlock_block(block));
		O42A_RETURN O42A_FALSE;
	case O42A_GC_LIST_NEW_ALLOCATED:
		// Not initialized yet. Add to used list.
		block->use_count = 1;

		O42A_DEBUG("First use of new block: %#lx\n", (long) block);
		O42A(gc_lock());
		O42A(gc_list_add(block, O42A_GC_LIST_USED));
		O42A(gc_unlock());

		O42A(o42a_gc_unlock_block(block));

		O42A_RETURN O42A_TRUE;
	case O42A_GC_LIST_STATIC:
		// Static data is "grey".
		O42A_DEBUG("Don't use a static block %#lx\n", (long) block);
		O42A(o42a_gc_unlock_block(block));
		O42A_RETURN O42A_FALSE;
	case O42A_GC_LIST_USED:
		// Already used by some thread.
		// Drop the linking flag.
		block->flags &= ~O42A_GC_BLOCK_LINKING;
		// Increase the uses count.
		++block->use_count;
		O42A_DEBUG("Use #%d of %#lx\n", block->use_count, (long) block);
		O42A(o42a_gc_unlock_block(block));
		O42A_RETURN O42A_TRUE;
	}

	// Data block is in the white list.
	// Move it to the used list.
	O42A(gc_lock());

	O42A(gc_list_remove(block));

	// Drop the checked flag for the next oddity.
	O42A(gc_block_uncheck(block));
	O42A_DEBUG("First use of block: %#lx\n", (long) block);
	block->use_count = 1;

	O42A(gc_list_add(block, O42A_GC_LIST_USED));

	O42A(gc_unlock());

	O42A(o42a_gc_unlock_block(block));

	O42A_RETURN O42A_TRUE;
}

void *o42a_gc_use_mutable(
		void **const var,
		o42a_gc_block_reader_ft *const reader) {
	O42A_ENTER(return NULL);

	gc_read_t read = {
		.read = O42A_FALSE,
		.prev = NULL,
		.next = NULL,
		.var = var,
		.reader = reader,
		.result = NULL,
	};

	// Register the mutable read.
	O42A(gc_lock_reads());
	if (!gc_last_read) {
		gc_first_read = &read;
	} else {
		read.prev = gc_last_read;
		gc_last_read->next = &read;
	}
	gc_last_read = &read;
	O42A(gc_unlock_reads());

	// Perform the read and unregister it.
	O42A(gc_lock_reads());
	void *const result = O42A(gc_read(&read));
	if (result) {
		O42A(o42a_gc_use(reader(result)));
	}
	O42A(gc_unlock_reads());

	O42A_RETURN result;
}

void o42a_gc_unuse(o42a_gc_block_t *const block) {
	O42A_ENTER(return);

	if (block->list == O42A_GC_LIST_STATIC) {
		O42A_DEBUG("Don't unuse a static block %#lx\n", (long) block);
		// Skip static blocks.
		O42A_RETURN;
	}

	// The block will be moved to the "white" list by GC thread if use count
	// drops to zero.
	O42A_DEBUG("Unuse block %#lx\n", (long) block);
	O42A(o42a_gc_lock_block(block));
	if (block->list != O42A_GC_LIST_USED) {
		assert(
				((block->list & O42A_GC_LIST_GREY_FLAG)
						|| block->list == O42A_GC_LIST_NEW_STATIC)
				&& "The block is not in a \"grey\" list");
		o42a_debug("Grey block\n");
	} else {
		assert(block->use_count && "Block not used");
		if (!(--block->use_count)) {
			gc_has_white = O42A_TRUE;
			o42a_debug("Not used any more\n");
		} else {
			O42A_DEBUG("Still used %d times\n", block->use_count);
		}
	}
	O42A(o42a_gc_unlock_block(block));

#ifdef O42A_GC_SYNC
	if (gc_has_white) {
		O42A(o42a_gc_run());
	}
#endif /* O42A_GC_SYNC */

	O42A_RETURN;
}

void o42a_gc_mark(o42a_gc_block_t *block) {
	O42A_ENTER(return);

	O42A_DEBUG("Mark used: %#lx\n", (long) block);
	O42A(o42a_gc_lock_block(block));

	if (block->list < O42A_GC_LIST_MIN) {
		// The new block is linked by some other one.
		// It also can link to other blocks.
		O42A(gc_link(block));
	}

	o42a_bool_t mark = O42A(gc_mark(block));
	O42A(o42a_gc_unlock_block(block));

	// Mark the linked blocks.
	if (mark) {
		O42A_DEBUG("Mark contents: %#lx\n", (long) block);
		O42A(block->desc->mark(o42a_gc_dataof(block)));
	} else {
		O42A_DEBUG("Already marked: %#lx\n", (long) block);
	}

	O42A_RETURN;
}

void o42a_gc_noop(void *data __attribute__((unused))) {
	O42A_ENTER(return);
	O42A_RETURN;
}
