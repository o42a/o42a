/*
    Run-Time Library
    Copyright (C) 2010-2012 Ruslan Lopatin

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
#include "o42a/fields.h"

#include "o42a/error.h"
#include "o42a/memory/gc.h"
#include "o42a/object.h"

#ifndef NDEBUG
const o42a_dbg_type_info4f_t _O42A_DEBUG_TYPE_o42a_fld_ctr = {
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
	},
};
#endif /* NDEBUG */

static void o42a_fld_mark_none(o42a_fld *const field) {
	O42A_ENTER(return);
	O42A_RETURN;
}

static void o42a_fld_mark_obj(o42a_fld *const field) {
	O42A_ENTER(return);

	volatile o42a_fld_obj *const fld = &field->obj;
	o42a_obj_t *const object = fld->object;

	if (!object) {
		O42A_RETURN;
	}

	o42a_obj_data_t *const data = O42A(&o42a_obj_type(object)->type.data);

	O42A(o42a_gc_mark(o42a_gc_blockof((char *) data + data->start)));

	O42A_RETURN;
}

static const o42a_fld_desc_t o42a_obj_field_kinds[] = {
	[O42A_FLD_OBJ] = {// object field
		.propagate = &o42a_fld_obj_propagate,
		.inherit = &o42a_fld_obj_inherit,
		.mark = &o42a_fld_mark_obj,
	},
	[O42A_FLD_LINK] = {// link field
		.propagate = &o42a_fld_link_propagate,
		.inherit = &o42a_fld_link_inherit,
		.mark = &o42a_fld_mark_obj,
	},
	[O42A_FLD_VAR] = {// variable field
		.propagate = &o42a_fld_var_propagate,
		.inherit = &o42a_fld_var_inherit,
		.mark = &o42a_fld_var_mark,
	},
	[O42A_FLD_GETTER] = {// getter field
		.propagate = &o42a_fld_getter_propagate,
		.inherit = &o42a_fld_getter_inherit,
		.mark = &o42a_fld_mark_none,
	},
	[O42A_FLD_SCOPE] = {// scope object pointer
		.propagate = &o42a_fld_scope_propagate,
		.inherit = &o42a_fld_scope_inherit,
		.mark = &o42a_fld_mark_obj,
	},
	[O42A_FLD_DEP] = {// dependency field
		.propagate = &o42a_fld_dep_copy,
		.inherit = &o42a_fld_dep_copy,
		.mark = &o42a_fld_mark_obj,
	},
	[O42A_FLD_ASSIGNER] = {// variable assigner
		.propagate = &o42a_fld_assigner_propagate,
		.inherit = &o42a_fld_assigner_inherit,
		.mark = &o42a_fld_assigner_mark,
	},
};


inline o42a_fld_desc_t *o42a_fld_desc(const o42a_obj_field_t *const field) {
	return &o42a_obj_field_kinds[field->kind];
}

extern o42a_fld *o42a_fld_by_field(
		const o42a_obj_body_t *,
		const o42a_obj_field_t *);

extern o42a_fld *o42a_fld_by_overrider(const o42a_obj_overrider_t *);

o42a_obj_body_t *o42a_obj_ref_null(o42a_obj_t *scope) {
	O42A_ENTER(return NULL);
	O42A_RETURN NULL;
}

o42a_obj_body_t *o42a_obj_ref_stub(o42a_obj_t *scope) {
	O42A_ENTER(return NULL);
	o42a_error_print("Object reference stub invoked");
	O42A_RETURN NULL;
}

o42a_bool_t o42a_fld_start(
		o42a_obj_data_t *const data,
		o42a_fld_ctr_t *const ctr) {
	O42A_ENTER(return O42A_FALSE);

	O42A(o42a_obj_lock(data));
	if (ctr->fld->obj.object) {
		O42A(o42a_obj_unlock(data));
		// Object already set.
		O42A_RETURN O42A_FALSE;
	}

	o42a_fld_ctr_t *last_ctr = data->fld_ctrs;
	pthread_t thread = O42A(pthread_self());

	if (!last_ctr) {
		// No fields currently constructing.
		ctr->prev = NULL;
		ctr->next = NULL;
		ctr->thread = thread;
		data->fld_ctrs = ctr;
		O42A(o42a_obj_unlock(data));
		O42A_RETURN O42A_TRUE;
	}

	// Find out if the field already constructing.
	while (1) {
		if (last_ctr->fld != ctr->fld) {
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
			O42A(o42a_obj_unlock(data));
			// Try to construct the field.
			O42A_RETURN O42A_TRUE;
		}
		// Wait for another thread to construct the field.
		while (last_ctr->fld->obj.object) {
			O42A(o42a_obj_wait(data));
		}
		// Field constructed.
		O42A(o42a_obj_unlock(data));
		O42A_RETURN O42A_FALSE;
	}

	// Append a construction info to the list.
	last_ctr->next = ctr;
	ctr->prev = last_ctr;
	ctr->next = NULL;
	ctr->thread = thread;
	O42A(o42a_obj_unlock(data));

	// Construct the field.
	O42A_RETURN O42A_TRUE;
}

void o42a_fld_finish(o42a_obj_data_t *const data, o42a_fld_ctr_t *const ctr) {
	O42A_ENTER(return);

	O42A(o42a_obj_lock(data));

	// Remove the construction info from the list.
	o42a_fld_ctr_t *prev = ctr->prev;

	if (prev) {
		// Not first in the list.
		prev->next = ctr->next;
	} else if (data->fld_ctrs == ctr) {
		// First in the list.
		data->fld_ctrs = ctr->next;
	}// Not in the list otherwise.

	// Inform others the field is constructed.
	O42A(o42a_obj_broadcast(data));

	O42A(o42a_obj_unlock(data));

	O42A_RETURN;
}