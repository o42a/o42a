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
#include "o42a/memory/gc.h"

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

	/** No list. This is only possible for newly created blocks. */
	O42A_GC_LIST_NONE = 0,

	/**
	 * A "gray" list flag. The "gray" list is a list of data objects known to be
	 * used and thus couldn't be deallocated.
	 */
	O42A_GC_LIST_GRAY_FLAG = 0x10,

	/**
	 * A list of data blocks used by some thread(s).
	 *
	 * A data block from this list travels to the "white" list once no longer
	 * used.
	 *
	 * This, along with O42A_LIST_STATIC forms a "gray" GC list.
	 */
	O42A_GC_LIST_USED = O42A_GC_LIST_GRAY_FLAG | 0,

	/**
	 * A list of statically allocated data blocks.
	 *
	 * This, along with O42A_LIST_USED forms a "gray" GC list.
	 */
	O42A_GC_LIST_STATIC = O42A_GC_LIST_GRAY_FLAG | 1,

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
	O42A_GC_LIST_WHITE_FLAG = 0x20,

	/**
	 * An "even" "white" list.
	 */
	O42A_GC_LIST_EVEN = O42A_GC_LIST_WHITE_FLAG | 0,

	/**
	 * An "odd" "white" list.
	 */
	O42A_GC_LIST_ODD = O42A_GC_LIST_WHITE_FLAG | 1,

};

/**
 * A garbage-collected data block flags.
 *
 * These flags are used to fill the o42a_gc_block.flags field.
 */
enum o42a_gc_block_flags {

	/**
	 * The data block is checked a some iteration.
	 *
	 * Shift this flag left by oddity bits to get the flag value for the
	 * corresponding "even" or "odd" list.
	 */
	O42A_GC_BLOCK_CHECKED = 0x01,

};

typedef struct o42a_gc_list {

	o42a_gc_block_t *first;

	o42a_gc_block_t *last;

} o42a_gc_list_t;


static inline void* o42a_gc_dataof(o42a_gc_block_t *block) {
	struct _o42a_gc_block *const blk = (struct _o42a_gc_block *) block;
	return (void*) &blk->data;
}

o42a_gc_block_t *o42a_gc_balloc(
		O42A_PARAMS
		const o42a_gc_desc_t *const desc,
		const size_t size) {
	O42A_ENTER(return NULL);

	o42a_gc_block_t *const block =
			O42A(malloc(sizeof(struct _o42a_gc_block) + size));

	if (!block) {
		O42A(o42a_error_print(O42A_ARGS "Can not allocate memory\n"));
		exit(EXIT_FAILURE);
	}

	block->lock = 0;
	block->list = O42A_GC_LIST_NONE;
	block->flags = 0;
	block->use_count = 0;
	block->desc = desc;
	block->prev = NULL;
	block->next = NULL;

	O42A_RETURN block;
}

inline void *o42a_gc_alloc(
		O42A_PARAMS
		const o42a_gc_desc_t *const desc,
		const size_t size) {
	O42A_ENTER(return NULL);
	O42A_RETURN o42a_gc_dataof(o42a_gc_balloc(O42A_ARGS desc, size));
}

inline void o42a_gc_free(O42A_PARAMS o42a_gc_block_t *const block) {
	O42A_ENTER(return);
	O42A(free(block));
	O42A_RETURN;
}


inline void o42a_gc_lock_block(O42A_PARAMS o42a_gc_block_t *const block) {
	O42A_ENTER(return);
	while (__sync_val_compare_and_swap(&block->lock, 0, 1)) {
		O42A(sched_yield());
	}
	__sync_lock_test_and_set(&block->lock, 1);
	O42A_RETURN;
}

inline void o42a_gc_unlock_block(O42A_PARAMS o42a_gc_block_t *const block) {
	O42A_ENTER(return);
	__sync_lock_release(&block->lock);
	block->lock = 0;
	O42A_RETURN;
}


/**
 * GC lock.
 *
 * Should be acquired with o42a_gc_lock for GC list modifications
 * or oddity switch.
 */
static int gc_lock;

/**
 * Locks GC for list modifications.
 */
static inline void o42a_gc_lock(O42A_PARAM) {
	O42A_ENTER(return);
	while (__sync_val_compare_and_swap(&gc_lock, 0, 1)) {
		O42A(sched_yield());
	}
	__sync_lock_test_and_set(&gc_lock, 1);
	O42A_RETURN;
}

/**
 * Unlocks GC previously locked with gc_lock.
 */
static inline void o42a_gc_unlock(O42A_PARAM) {
	O42A_ENTER(return);
	__sync_lock_release(&gc_lock, 0);
	gc_lock = 0;
	O42A_RETURN;
}


/**
 * Next GC oddity, i.e. which of the "white" lists will be processed next.
 *
 * This can be either 0 or 1.
 *
 * Current oddity can be calculated as gc_next_oddity ^ 1.
 */
static unsigned gc_next_oddity;


static o42a_gc_list_t gc_static_list;

static o42a_gc_list_t gc_used_list;

static o42a_gc_list_t gc_white_lists[2];


static inline void o42a_gc_block_uncheck(O42A_PARAMS o42a_gc_block_t *block) {
	O42A_ENTER(return);
	block->flags &= ~(O42A_GC_BLOCK_CHECKED << gc_next_oddity);
	O42A_RETURN;
}

static inline void o42a_gc_list_add(
		O42A_PARAMS
		o42a_gc_list_t *const list,
		o42a_gc_block_t *const block) {
	O42A_ENTER(return);

	o42a_gc_block_t *const last = list->last;

	block->prev = last;
	block->next = NULL;
	if (!last) {
		list->first = block;
	}
	list->last = block;

	O42A_RETURN;
}

static inline void o42a_gc_list_remove(
		O42A_PARAMS
		o42a_gc_list_t *const list,
		o42a_gc_block_t *const block) {
	O42A_ENTER(return);

	o42a_gc_block_t *const prev = block->prev;
	o42a_gc_block_t *const next = block->next;

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

static o42a_bool_t o42a_gc_do_mark(O42A_PARAMS o42a_gc_block_t *block) {
	O42A_ENTER(return O42A_FALSE);

	unsigned next_oddity = gc_next_oddity;
	unsigned oddity = next_oddity ^ 1;

	// Drop the checked flag for the next oddity and mark checked for this one.
	O42A(o42a_gc_block_uncheck(O42A_ARGS block));

	const int checked = O42A_GC_BLOCK_CHECKED << oddity;

	if (block->flags & checked) {
		// Already checked
		O42A_RETURN O42A_FALSE;
	}

	block->flags |= checked;

	// Move to the next oddity's white list.
	if (block->list == (O42A_GC_LIST_WHITE_FLAG | oddity)) {
		O42A(o42a_gc_lock(O42A_ARG));
		O42A(o42a_gc_list_remove(O42A_ARGS &gc_white_lists[oddity], block));
		O42A(o42a_gc_list_add(O42A_ARGS &gc_white_lists[next_oddity], block));
		O42A(o42a_gc_unlock(O42A_ARG));
	}

	O42A_RETURN O42A_TRUE;
}

static o42a_bool_t gc_thread_exists;

static o42a_bool_t gc_has_white;

static pthread_t gc_thread;

static pthread_mutex_t gc_mutex = PTHREAD_MUTEX_INITIALIZER;

static pthread_cond_t gc_cond = PTHREAD_COND_INITIALIZER;

static void o42a_gc_thread_mark_used(O42A_PARAM) {
	O42A_ENTER(return);

	o42a_gc_block_t *block;

	O42A(o42a_gc_lock(O42A_ARG));
	block = gc_used_list.first;
	O42A(o42a_gc_unlock(O42A_ARG));

	while (block) {

		o42a_gc_block_t *next;

		O42A(o42a_gc_lock_block(O42A_ARGS block));

		next = block->next;
		if (!block->use_count) {
			// Block were used, but it's use count dropped to zero.
			O42A(o42a_gc_lock(O42A_ARG));

			// Remove the block from the used list.
			O42A(o42a_gc_list_remove(O42A_ARGS &gc_used_list, block));

			const unsigned oddity = gc_next_oddity ^ 1;

			if (block->flags & (O42A_GC_BLOCK_CHECKED << oddity)) {
				// Block already marked as used.
				// Move it to the next oddity "white" list.
				block->list = O42A_GC_LIST_WHITE_FLAG | gc_next_oddity;
				O42A(o42a_gc_block_uncheck(O42A_ARGS block));
				O42A(o42a_gc_list_add(
						O42A_ARGS
						&gc_white_lists[gc_next_oddity],
						block));
				// Process the next oddity unconditionally.
				gc_has_white = O42A_TRUE;
			} else {
				// Block not marked as used yet.
				// Move it to the current "white" list.
				block->list = O42A_GC_LIST_WHITE_FLAG | oddity;
				O42A(o42a_gc_list_add(
						O42A_ARGS
						&gc_white_lists[oddity],
						block));
			}

			O42A(o42a_gc_unlock(O42A_ARG));

			O42A(o42a_gc_unlock_block(O42A_ARGS block));

			block = next;

			continue;
		}

		// Mark the block.
		o42a_bool_t mark = O42A(o42a_gc_do_mark(O42A_ARGS block));

		O42A(o42a_gc_unlock_block(O42A_ARGS block));

		// Mark the referenced data.
		if (mark) {
			O42A(block->desc->mark(O42A_ARGS o42a_gc_dataof(block)));
		}

		block = next;
	}

	O42A_RETURN;
}

static void o42a_gc_thread_mark_static(O42A_PARAM) {
	O42A_ENTER(return);

	o42a_gc_block_t *block;

	O42A(o42a_gc_lock(O42A_ARG));
	block = gc_static_list.first;
	O42A(o42a_gc_unlock(O42A_ARG));

	while (block) {

		o42a_gc_block_t *next;
		o42a_bool_t mark;

		// Mark the static block.
		O42A(o42a_gc_lock_block(O42A_ARGS block));
		next = block->next;
		mark = O42A(o42a_gc_do_mark(O42A_ARGS block));
		O42A(o42a_gc_unlock_block(O42A_ARGS block));

		// Mark the referenced data.
		if (mark) {
			O42A(block->desc->mark(O42A_ARGS o42a_gc_dataof(block)));
		}

		block = next;
	}

	O42A_RETURN;
}

static void o42a_gc_thread_sweep(O42A_PARAMS o42a_gc_list_t *list) {
	O42A_ENTER(return);

	// No need to synchronize, as the white list entries are not referenced.
	o42a_gc_block_t *block = list->first;

	if (!block) {
		// White list is empty.
		O42A_RETURN;
	}
	do {

		o42a_gc_block_t *next = block->next;

		O42A(block->desc->sweep(O42A_ARGS o42a_gc_dataof(block)));
		O42A(o42a_gc_free(O42A_ARGS block));

		block = next;
	} while (block);

	// Clean the white list.
	list->first = NULL;
	list->last = NULL;

	O42A_RETURN;
}

static void *o42a_gc_thread(void *data) {
#ifndef NDEBUG
	struct o42a_dbg_env debug_env = {
		stack_frame: NULL,
		command: O42A_DBG_CMD_EXEC,
		indent: 0,
	};
	o42a_dbg_env_t *__o42a_dbg_env_p__ = &debug_env;
	O42A_ENTER(return NULL);
#endif /* NDEBUG */

	while (1) {
		O42A(pthread_mutex_lock(&gc_mutex));
		while (!gc_has_white) {
			O42A(pthread_cond_wait(&gc_cond, &gc_mutex));
		}
		gc_has_white = O42A_FALSE;
		O42A(pthread_mutex_unlock(&gc_mutex));

		unsigned oddity;

		O42A(o42a_gc_lock(O42A_ARG));
		oddity = gc_next_oddity;
		gc_next_oddity = oddity ^ 1;
		O42A(o42a_gc_unlock(O42A_ARG));

		O42A(o42a_gc_thread_mark_used(O42A_ARG));
		O42A(o42a_gc_thread_mark_static(O42A_ARG));
		O42A(o42a_gc_thread_sweep(O42A_ARGS &gc_white_lists[oddity]));
	}

	O42A_RETURN NULL;
}

void o42a_gc_signal(O42A_PARAM) {
	O42A_ENTER(return);

	O42A(pthread_mutex_lock(&gc_mutex));
	gc_has_white = O42A_TRUE;
	if (gc_thread_exists) {
		O42A(pthread_cond_signal(&gc_cond));
	} else if (O42A(pthread_create(&gc_thread, NULL, &o42a_gc_thread, NULL))) {
		o42a_error_print(O42A_ARGS "Failed to create a GC thread");
	} else {
		gc_thread_exists = O42A_TRUE;
	}
	O42A(pthread_mutex_unlock(&gc_mutex));

	O42A_RETURN;
}

void o42a_gc_static(O42A_PARAMS o42a_gc_block_t *const block) {
	O42A_ENTER(return);

	O42A(o42a_gc_lock(O42A_ARG));
	block->flags = O42A_GC_LIST_STATIC;
	O42A(o42a_gc_list_add(O42A_ARGS &gc_static_list, block));
	O42A(o42a_gc_unlock(O42A_ARG));

	O42A_RETURN;
}

void o42a_gc_use(O42A_PARAMS o42a_gc_block_t *const block) {
	O42A_ENTER(return);

	O42A(o42a_gc_lock_block(O42A_ARGS block));

	switch (block->list) {
	case O42A_GC_LIST_NONE:
		// Not initialized yet. Add to used list.
		block->use_count = 1;
		block->list = O42A_GC_LIST_USED;

		O42A(o42a_gc_lock(O42A_ARG));
		O42A(o42a_gc_list_add(O42A_ARGS &gc_used_list, block));
		O42A(o42a_gc_unlock(O42A_ARG));

		O42A(o42a_gc_unlock_block(O42A_ARGS block));

		O42A_RETURN;
	case O42A_GC_LIST_STATIC:
		// Static data is "grey".
		O42A(o42a_gc_unlock_block(O42A_ARGS block));
		O42A_RETURN;
	case O42A_GC_LIST_USED:
		// Already used by some thread. Increase the uses count.
		++block->use_count;
		O42A(o42a_gc_unlock_block(O42A_ARGS block));
		O42A_RETURN;
	}

	// Data block is in the white list.
	// Move it to the used list.
	O42A(o42a_gc_lock(O42A_ARG));

	O42A(o42a_gc_list_remove(
			O42A_ARGS
			&gc_white_lists[block->list & 1],
			block));

	// Drop the checked flag for the next oddity.
	O42A(o42a_gc_block_uncheck(O42A_ARGS block));
	block->use_count = 0;
	block->list = O42A_GC_LIST_USED;

	O42A(o42a_gc_list_add(O42A_ARGS &gc_used_list, block));

	O42A(o42a_gc_unlock(O42A_ARG));

	O42A(o42a_gc_unlock_block(O42A_ARGS block));

	O42A_RETURN;
}

void o42a_gc_unuse(O42A_PARAMS o42a_gc_block_t *const block) {
	O42A_ENTER(return);

	// The block will be moved to the "white" list by GC thread if use count
	// drops to zero.
	O42A(o42a_gc_lock_block(O42A_ARGS block));
	--block->use_count;
	O42A(o42a_gc_unlock_block(O42A_ARGS block));

	O42A_RETURN;
}

void o42a_gc_mark(O42A_PARAMS o42a_gc_block_t *block) {
	O42A_ENTER(return);

	o42a_bool_t mark;

	O42A(o42a_gc_lock_block(O42A_ARGS block));
	mark = O42A(o42a_gc_do_mark(O42A_ARGS block));
	O42A(o42a_gc_unlock_block(O42A_ARGS block));

	// Mark the referenced data.
	if (mark) {
		O42A(block->desc->mark(O42A_ARGS o42a_gc_dataof(block)));
	}

	O42A_RETURN;
}
