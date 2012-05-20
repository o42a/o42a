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
	O42A_GC_BLOCK_CHECKED

};

typedef struct o42a_gc_list {

	o42a_gc_block_t *first;

	o42a_gc_block_t *last;

} o42a_gc_list_t;


/**
 * Next GC oddity, i.e. which of the "white" lists will be processed next.
 *
 * This can be either 0 or 1.
 *
 * Current oddity can be calculated as gc_next_oddity ^ 1.
 */
static unsigned gc_next_oddity;

/**
 * GC lock.
 *
 * Should be acquired with o42a_gc_lock for GC list modifications GC oddity
 * switch.
 */
static int gc_lock;


static o42a_gc_list_t gc_used_list;

static o42a_gc_list_t gc_white_lists[2];

o42a_gc_block_t *o42a_gc_balloc(
		O42A_PARAMS
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
	block->prev = NULL;
	block->next = NULL;
	block->uses = NULL;

	O42A_RETURN block;
}

inline void *o42a_gc_alloc(O42A_PARAMS const size_t size) {
	O42A_ENTER(return NULL);
	struct _o42a_gc_block *const block =
			(struct _o42a_gc_block *) O42A(o42a_gc_balloc(O42A_ARGS size));
	O42A_RETURN (void*) &block->data;
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

static inline void o42a_gc_block_uncheck(O42A_PARAMS o42a_gc_block_t *block) {
	O42A_ENTER(return);
	block->flags &= ~(O42A_GC_BLOCK_CHECKED << gc_next_oddity);
	O42A_RETURN;
}

void o42a_gc_use(O42A_PARAMS o42a_gc_use_t *const use) {
	O42A_ENTER(return);

	o42a_gc_block_t *const block = use->block;

	O42A(o42a_gc_lock_block(O42A_ARGS block));

	switch (block->list) {
	case O42A_GC_LIST_NONE:
		// Not initialized yet.
		// Create the uses list.
		use->prev = NULL;
		use->next = NULL;
		block->uses = use;
		block->list = O42A_GC_LIST_USED;

		O42A(o42a_gc_lock(O42A_ARG));
		// Add to the used list.
		O42A(o42a_gc_list_add(O42A_ARGS &gc_used_list, block));
		O42A(o42a_gc_unlock(O42A_ARG));

		O42A(o42a_gc_unlock_block(O42A_ARGS block));

		O42A_RETURN;
	case O42A_GC_LIST_STATIC:
		// Static data is "grey".
		O42A(o42a_gc_unlock_block(O42A_ARGS block));
		O42A_RETURN;
	case O42A_GC_LIST_USED:
		// Already used by some thread.
		{
			// Extend the uses list.
			o42a_gc_use_t *const first = block->uses;

			use->prev = NULL;
			use->next = first;
			first->prev = use;
			block->uses = use;
		}

		O42A(o42a_gc_unlock_block(O42A_ARGS block));

		O42A_RETURN;
	}

	// Data block is in the white list.
	O42A(o42a_gc_lock(O42A_ARG));
	// Remove from the white list.
	O42A(o42a_gc_list_remove(
			O42A_ARGS
			&gc_white_lists[block->list & 1],
			block));
	// Add to the used list.
	O42A(o42a_gc_list_add(O42A_ARGS &gc_used_list, block));
	// Drop the checked flag for the next oddity.
	O42A(o42a_gc_block_uncheck(O42A_ARGS block));
	O42A(o42a_gc_unlock(O42A_ARG));

	// Create the use list.
	use->prev = NULL;
	use->next = NULL;
	block->uses = use;
	block->list = O42A_GC_LIST_USED;

	O42A(o42a_gc_unlock_block(O42A_ARGS block));

	O42A_RETURN;
}

void o42a_gc_unuse(O42A_PARAMS o42a_gc_use_t *const use) {
	O42A_ENTER(return);

	o42a_gc_block_t *const block = use->block;

	O42A(o42a_gc_lock_block(O42A_ARGS block));

	// Reduce the uses list.
	o42a_gc_use_t *const prev = use->prev;
	o42a_gc_use_t *const next = use->next;

	if (prev) {
		prev->next = next;
	} else {
		block->uses = next;
	}
	if (next) {
		next->prev = prev;
		O42A(o42a_gc_unlock_block(O42A_ARGS block));
		O42A_RETURN;
	}
	if (prev) {
		O42A(o42a_gc_unlock_block(O42A_ARGS block));
		O42A_RETURN;
	}

	// Not used any more.
	O42A(o42a_gc_lock(O42A_ARG));
	// Remove from used list.
	O42A(o42a_gc_list_remove(O42A_ARGS &gc_used_list, block));
	// Add to the next oddity white list.
	O42A(o42a_gc_list_add(O42A_ARGS &gc_white_lists[gc_next_oddity], block));
	// Drop the checked flag for the next oddity.
	O42A(o42a_gc_block_uncheck(O42A_ARGS block));
	O42A(o42a_gc_unlock(O42A_ARG));

	O42A(o42a_gc_unlock_block(O42A_ARGS block));

	O42A_RETURN;
}
