/*
    Copyright (C) 2010-2014 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#include "o42a/fields.h"

#include "o42a/error.h"
#include "o42a/memory/gc.h"
#include "o42a/object.h"

#ifndef NDEBUG
const o42a_dbg_type_info5f_t _O42A_DEBUG_TYPE_o42a_fld_ctr = {
	.type_code = 0x042a02ff,
	.field_num = 4,
	.name = "o42a_fld_ctr_t",
	.fields = {
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_fld_ctr_t, prev),
			.name = "prev",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_fld_ctr,
		},
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_fld_ctr_t, next),
			.name = "next",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_fld_ctr,
		},
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_fld_ctr_t, fld),
			.name = "fld",
		},
		{
			.data_type = O42A_TYPE_SYSTEM,
			.offset = offsetof(o42a_fld_ctr_t, thread),
			.name = "thread",
		},
		{
			.data_type = O42A_TYPE_INT16,
			.offset = offsetof(o42a_fld_ctr_t, thread),
			.name = "fld_kind",
		},
	},
};
#endif /* NDEBUG */

static void fld_ptr_reset(o42a_obj_ctable_t *const ctable) {
	O42A_ENTER(return);
	ctable->to_fld->obj.object = NULL;
	O42A_RETURN;
}

static void fld_skip(o42a_obj_ctable_t *const ctable __attribute__((unused))) {
	O42A_ENTER(return);
	O42A_RETURN;
}

static void fld_ptr_copy(o42a_obj_ctable_t *const ctable) {
	O42A_ENTER(return);
	ctable->to_fld->obj.object = ctable->from_fld->obj.object;
	O42A_RETURN;
}

static void fld_mark_none(o42a_fld *const field __attribute__((unused))) {
	O42A_ENTER(return);
	O42A_RETURN;
}

static void fld_sweep_none(o42a_fld *const field __attribute__((unused))) {
	O42A_ENTER(return);
	O42A_RETURN;
}

static void fld_ptr_mark(o42a_fld *const field) {
	O42A_ENTER(return);

	volatile o42a_fld_obj *const fld = &field->obj;
	void *ptr = fld->object;

	if (!ptr) {
		O42A_RETURN;
	}

	O42A(o42a_gc_mark(o42a_gc_blockof(ptr)));

	O42A_RETURN;
}

static o42a_bool_t fld_ptr_is_init(const o42a_fld *const fld) {
	O42A_ENTER(return O42A_FALSE);
	O42A_RETURN fld->obj.object ? O42A_TRUE : O42A_FALSE;
}

static const o42a_fld_desc_t o42a_obj_field_kinds[] = {
	[O42A_FLD_OBJ] = {// Object field.
		.inherit = &fld_ptr_reset,
		.propagate = &fld_ptr_reset,
		.mark = &fld_ptr_mark,
		.sweep = &fld_sweep_none,
		.is_init = fld_ptr_is_init,
	},
	[O42A_FLD_ALIAS] = {// Alias field.
		.inherit = &fld_ptr_reset,
		.propagate = &fld_ptr_reset,
		.mark = &fld_ptr_mark,
		.sweep = &fld_sweep_none,
		.is_init = fld_ptr_is_init,
	},
	[O42A_FLD_VAR] = {// Variable field.
		.inherit = fld_ptr_reset,
		.propagate = fld_ptr_reset,
		.mark = fld_ptr_mark,
		.sweep = fld_sweep_none,
		.is_init = fld_ptr_is_init,
	},
	[O42A_FLD_OWNER] = {// Owner object pointer.
		.inherit = &fld_ptr_copy,
		.propagate = &o42a_fld_owner_propagate,
		.mark = &fld_ptr_mark,
		.sweep = &fld_sweep_none,
		.is_init = NULL,// Eagerly constructed.
	},
	[O42A_FLD_DEP] = {// Run time dependency field.
		.inherit = fld_ptr_copy,
		.propagate = fld_skip,
		.mark = fld_ptr_mark,
		.sweep = fld_sweep_none,
		.is_init = NULL,// Eagerly constructed.
	},
	[O42A_FLD_LOCAL] = {// Storage for locals used in yielding objects.
		.inherit = fld_ptr_reset,
		.propagate = fld_ptr_reset,
		.mark = fld_ptr_mark,
		.sweep = fld_sweep_none,
		.is_init = NULL,// Eagerly constructed.
	},
	[O42A_FLD_RESUME_FROM] = {// Resume from pointer field.
		.inherit = fld_ptr_reset,
		.propagate = fld_ptr_reset,
		.mark = fld_mark_none,
		.sweep = fld_sweep_none,
		.is_init = NULL,// Eagerly constructed.
	},
};


inline o42a_fld_desc_t *o42a_fld_desc(const o42a_obj_field_t *const field) {
	return &o42a_obj_field_kinds[field->kind];
}

extern o42a_fld *o42a_fld_by_field(
		const o42a_obj_t *,
		const o42a_obj_field_t *);

o42a_obj_t *o42a_obj_ref_null(
		o42a_obj_t *scope __attribute__((unused)),
		const o42a_obj_vmtc_t *vmtc __attribute__((unused))) {
	O42A_ENTER(return NULL);
	O42A_RETURN NULL;
}

o42a_obj_t *o42a_obj_ref_stub(
		o42a_obj_t *scope __attribute__((unused)),
		const o42a_obj_vmtc_t *vmtc __attribute__((unused))) {
	O42A_ENTER(return NULL);
	o42a_error_print("Object reference stub invoked");
	O42A_RETURN NULL;
}

o42a_bool_t o42a_fld_start(
		o42a_obj_t *const object,
		o42a_obj_lock_t *const lock,
		o42a_fld_ctr_t *const ctr) {
	O42A_ENTER(return O42A_FALSE);

	O42A(o42a_obj_static(object, lock));
	O42A(o42a_obj_lock(lock));

	const o42a_fld *const fld = ctr->fld;
	o42a_bool_t (*const is_init) (const o42a_fld *) =
			o42a_obj_field_kinds[ctr->fld_kind].is_init;

	if (is_init(fld)) {
		O42A(o42a_obj_unlock(lock));
		// Object already set.
		O42A_RETURN O42A_FALSE;
	}

	o42a_fld_ctr_t *last_ctr = lock->fld_ctrs;
	pthread_t thread = O42A(pthread_self());

	if (!last_ctr) {
		// No fields currently constructing.
		ctr->prev = NULL;
		ctr->next = NULL;
		ctr->thread = thread;
		lock->fld_ctrs = ctr;
		O42A(o42a_obj_unlock(lock));
		O42A_RETURN O42A_TRUE;
	}

	// Find out if the field already constructing.
	while (1) {
		if (last_ctr->fld != fld) {
			// Check the next field.
			o42a_fld_ctr_t *next_ctr = last_ctr->next;
			if (!next_ctr) {
				// No more fields to check.
				break;
			}
			last_ctr = next_ctr;
		}
		// Field already constructing.
		if (last_ctr->thread == thread) {
			// A recursion during the field construction.
			ctr->prev = NULL;
			ctr->next = NULL;
			ctr->thread = last_ctr->thread;
			O42A(o42a_obj_unlock(lock));
			// Try to construct the field.
			O42A_RETURN O42A_TRUE;
		}
		// Wait for another thread to construct the field.
		while (!is_init(fld)) {
			O42A(o42a_obj_wait(lock));
		}
		// Field constructed.
		O42A(o42a_obj_unlock(lock));
		O42A_RETURN O42A_FALSE;
	}

	// Append a construction info to the list.
	last_ctr->next = ctr;
	ctr->prev = last_ctr;
	ctr->next = NULL;
	ctr->thread = thread;
	O42A(o42a_obj_unlock(lock));

	// Construct the field.
	O42A_RETURN O42A_TRUE;
}

o42a_bool_t o42a_fld_val_start(
		o42a_obj_t *const object,
		o42a_obj_lock_t *const lock,
		o42a_fld_ctr_t *const ctr) {
	O42A_ENTER(return O42A_FALSE);

	O42A(o42a_obj_static(object, lock));
	O42A(o42a_obj_lock(lock));

	o42a_val_t *const value = ctr->fld = &object->object_data.value;

	if (!(value->flags & O42A_VAL_INDEFINITE)) {
		O42A(o42a_obj_unlock(lock));
		// Object already set.
		O42A_RETURN O42A_FALSE;
	}

	o42a_fld_ctr_t *last_ctr = lock->fld_ctrs;
	pthread_t thread = O42A(pthread_self());

	if (!last_ctr) {
		// No fields currently constructing.
		ctr->prev = NULL;
		ctr->next = NULL;
		ctr->thread = thread;
		lock->fld_ctrs = ctr;
		O42A(o42a_obj_unlock(lock));
		O42A_RETURN O42A_TRUE;
	}

	// Find out if the field already constructing.
	while (1) {
		if (last_ctr->fld != value) {
			// Check the next field.
			o42a_fld_ctr_t *next_ctr = last_ctr->next;
			if (!next_ctr) {
				// No more fields to check.
				break;
			}
			last_ctr = next_ctr;
		}
		// Field already constructing.
		if (last_ctr->thread == thread) {
			// A recursion during the field construction.
			ctr->prev = NULL;
			ctr->next = NULL;
			ctr->thread = last_ctr->thread;
			O42A(o42a_obj_unlock(lock));
			// Try to construct the field.
			O42A_RETURN O42A_TRUE;
		}
		// Wait for another thread to construct the field.
		while (value->flags & O42A_VAL_INDEFINITE) {
			O42A(o42a_obj_wait(lock));
		}
		// Field constructed.
		O42A(o42a_obj_unlock(lock));
		O42A_RETURN O42A_FALSE;
	}

	// Append a construction info to the list.
	last_ctr->next = ctr;
	ctr->prev = last_ctr;
	ctr->next = NULL;
	ctr->thread = thread;
	O42A(o42a_obj_unlock(lock));

	// Construct the field.
	O42A_RETURN O42A_TRUE;
}

void o42a_fld_finish(
		o42a_obj_t *const object,
		o42a_obj_lock_t *const lock,
		o42a_fld_ctr_t *const ctr) {
	O42A_ENTER(return);

	O42A(o42a_obj_lock(lock));

	// Remove the construction info from the list.
	o42a_fld_ctr_t *prev = ctr->prev;

	if (prev) {
		// Not first in the list.
		prev->next = ctr->next;
	} else if (lock->fld_ctrs == ctr) {
		// First in the list.
		lock->fld_ctrs = ctr->next;
	}// Not in the list otherwise.

	// The object can link to garbage-collected data blocks now.
	O42A(o42a_gc_link(o42a_gc_blockof(object)));

	// Inform others the field is constructed.
	O42A(o42a_obj_broadcast(lock));

	O42A(o42a_obj_unlock(lock));

	O42A_RETURN;
}
