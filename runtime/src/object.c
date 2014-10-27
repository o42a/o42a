/*
    Copyright (C) 2010-2014 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#include "o42a/object.h"

#include <assert.h>
#include <sched.h>
#include <stdlib.h>

#include "o42a/error.h"
#include "o42a/field.h"
#include "o42a/memory/refcount.h"


#ifndef NDEBUG

const struct _O42A_DEBUG_TYPE_o42a_obj_data _O42A_DEBUG_TYPE_o42a_obj_data = {
	.type_code = 0x042a0100,
	.field_num = 7,
	.name = "o42a_obj_data_t",
	.fields = {
		{
			.data_type = O42A_TYPE_FUNC_PTR,
			.offset = offsetof(o42a_obj_data_t, value_f),
			.name = "value_f",
		},
		{
			.data_type = O42A_TYPE_STRUCT,
			.offset = offsetof(o42a_obj_data_t, value),
			.name = "value",
			.type_info = (o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_val,
		},
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_data_t, vmtc),
			.name = "vmtc",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_vmtc,
		},
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_data_t, desc),
			.name = "desc",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_desc,
		},
		{
			.data_type = O42A_TYPE_SYSTEM,
			.offset = offsetof(o42a_obj_data_t, mutex),
			.name = "mutex",
		},
		{
			.data_type = O42A_TYPE_SYSTEM,
			.offset = offsetof(o42a_obj_data_t, thread_cond),
			.name = "thread_cond",
		},
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_data_t, fld_ctrs),
			.name = "fld_ctrs",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_fld_ctr,
		},
	},
};

const o42a_dbg_type_info5f_t _O42A_DEBUG_TYPE_o42a_obj_desc = {
	.type_code = 0x042a0101,
	.field_num = 5,
	.name = "o42a_obj_desc_t",
	.fields = {
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_desc_t, value_type),
			.name = "value_type",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_val_type,
		},
		{
			.data_type = O42A_TYPE_PTR,
			.offset = offsetof(o42a_obj_desc_t, type_info),
			.name = "type_info",
		},
		{
			.data_type = O42A_TYPE_STRUCT,
			.offset = offsetof(o42a_obj_desc_t, fields),
			.name = "fields",
			.type_info = (o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_rlist,
		},
		{
			.data_type = O42A_TYPE_STRUCT,
			.offset = offsetof(o42a_obj_desc_t, ascendants),
			.name = "ascendants",
			.type_info = (o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_rlist,
		},
		{
			.data_type = O42A_TYPE_INT32,
			.offset = offsetof(o42a_obj_desc_t, object_size),
			.name = "object_size",
		},
	},
};

const o42a_dbg_type_info1f_t _O42A_DEBUG_TYPE_o42a_obj_ascendant = {
	.type_code = 0x042a0110,
	.field_num = 1,
	.name = "o42a_obj_ascendant_t",
	.fields = {
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_ascendant_t, desc),
			.name = "desc",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_desc,
		},
	},
};

const o42a_dbg_type_info3f_t _O42A_DEBUG_TYPE_o42a_obj_field = {
	.type_code = 0x042a0112,
	.field_num = 3,
	.name = "o42a_obj_field_t",
	.fields = {
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_field_t, declared_in),
			.name = "declared_in",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_desc,
		},
		{
			.data_type = O42A_TYPE_INT32,
			.offset = offsetof(o42a_obj_field_t, kind),
			.name = "kind",
		},
		{
			.data_type = O42A_TYPE_REL_PTR,
			.offset = offsetof(o42a_obj_field_t, fld),
			.name = "fld",
		},
	},
};


const o42a_dbg_type_info2f_t _O42A_DEBUG_TYPE_o42a_obj_vmtc = {
	.type_code = 0x042a0102,
	.field_num = 2,
	.name = "o42a_obj_vmtc_t",
	.fields = {
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_vmtc_t, vmt),
			.name = "vmt",
		},
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_vmtc_t, prev),
			.name = "prev",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_vmtc,
		},
	},
};

const o42a_dbg_type_info3f_t _O42A_DEBUG_TYPE_o42a_obj_ctr = {
	.type_code = 0x042a0120,
	.field_num = 3,
	.name = "o42a_obj_ctr_t",
	.fields = {
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_ctr_t, object),
			.name = "object",
		},
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_ctr_t, owner),
			.name = "owner",
		},
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_ctr_t, ancestor),
			.name = "ancestor",
		},
	},
};

const struct _O42A_DEBUG_TYPE_o42a_obj_ctable
_O42A_DEBUG_TYPE_o42a_obj_ctable = {
	.type_code = 0x042a0122,
	.field_num = 8,
	.name = "o42a_obj_ctable_t",
	.fields = {
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_ctable_t, owner),
			.name = "owner",
		},
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_ctable_t, ancestor_desc),
			.name = "ancestor_desc",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_desc,
		},
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_ctable_t, from),
			.name = "from",
		},
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_ctable_t, from),
			.name = "to",
		},
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_ctable_t, body_desc),
			.name = "body_desc",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_desc,
		},
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_ctable_t, field),
			.name = "field",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_field,
		},
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_ctable_t, from_fld),
			.name = "from_fld",
		},
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_ctable_t, to_fld),
			.name = "to_fld",
		},
	},
};

const o42a_dbg_type_info1f_t _O42A_DEBUG_TYPE_o42a_obj_use = {
	.type_code = 0x042a0123,
	.field_num = 1,
	.name = "o42a_obj_use_t",
	.fields = {
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_use_t, object),
			.name = "object",
		},
	},
};

#endif /* NDEBUG */

extern const o42a_obj_ascendant_t *o42a_obj_ascendants(const o42a_obj_desc_t *);

extern o42a_obj_field_t *o42a_obj_fields(const o42a_obj_desc_t *);

const o42a_obj_desc_t o42a_obj_void_desc = {
#ifndef NDEBUG
	.__o42a_dbg_header__ = {
		.type_code = 0x042a0101,
		.enclosing = 0,
		.name = "o42a_obj_void_desc",
		.type_info = (o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_desc,
	},
#endif /* NDEBUG */
	.fields = {
#ifndef NDEBUG
		.__o42a_dbg_header__ = {
			.type_code = 0x042a0001,
			.enclosing = -((int32_t) offsetof(o42a_obj_desc_t, fields)),
			.name = "fields",
			.type_info = (o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_rlist,
		},
#endif /* NDEBUG */
		.list = 0,
		.size = 0,
	},
	.ascendants = {
#ifndef NDEBUG
		.__o42a_dbg_header__ = {
			.type_code = 0x042a0001,
			.enclosing = -((int32_t) offsetof(o42a_obj_desc_t, ascendants)),
			.name = "ascendants",
			.type_info = (o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_rlist,
		},
#endif /* NDEBUG */
		.list = 0,
		.size = 0,
	},
	.object_size = sizeof(o42a_obj_t),
};

const struct o42a_obj_false_desc o42a_obj_false_desc = {
	.desc = {
#ifndef NDEBUG
		.__o42a_dbg_header__ = {
			.type_code = 0x042a0101,
			.enclosing = 0,
			.name = "o42a_obj_false_desc",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_desc,
		},
#endif /* NDEBUG */
		.fields = {
#ifndef NDEBUG
			.__o42a_dbg_header__ = {
				.type_code = 0x042a0001,
				.enclosing = -((int32_t) offsetof(o42a_obj_desc_t, fields)),
				.name = "fields",
				.type_info =
						(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_rlist,
			},
#endif /* NDEBUG */
			.list = 0,
			.size = 0,
		},
		.ascendants = {
#ifndef NDEBUG
			.__o42a_dbg_header__ = {
				.type_code = 0x042a0001,
				.enclosing = -((int32_t) offsetof(o42a_obj_desc_t, ascendants)),
				.name = "ascendants",
				.type_info =
						(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_rlist,
			},
#endif /* NDEBUG */
			.list =
					offsetof(struct o42a_obj_false_desc, ascendants)
					- offsetof(o42a_obj_desc_t, ascendants),
			.size = 1,
		},
		.object_size = sizeof(o42a_obj_t),
	},
	.ascendants = {
		{
#ifndef NDEBUG
			.__o42a_dbg_header__ = {
				.type_code = 0x042a0110,
				.enclosing = -((int32_t) offsetof(
						struct o42a_obj_false_desc,
						ascendants)),
				.name = "$ascendant.false",
				.type_info =
						(o42a_dbg_type_info_t *)
						&_O42A_DEBUG_TYPE_o42a_obj_ascendant,
			},
#endif /* NDEBUG */
			.desc = &o42a_obj_false_desc.desc,
		}
	},
};

const o42a_obj_desc_t o42a_obj_none_desc = {
#ifndef NDEBUG
	.__o42a_dbg_header__ = {
		.type_code = 0x042a0101,
		.enclosing = 0,
		.name = "o42a_obj_none_desc",
		.type_info = (o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_desc,
	},
#endif /* NDEBUG */
	.fields = {
#ifndef NDEBUG
		.__o42a_dbg_header__ = {
			.type_code = 0x042a0001,
			.enclosing = -((int32_t) offsetof(o42a_obj_desc_t, fields)),
			.name = "fields",
			.type_info = (o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_rlist,
		},
#endif /* NDEBUG */
		.list = 0,
		.size = 0,
	},
	.ascendants = {
#ifndef NDEBUG
		.__o42a_dbg_header__ = {
			.type_code = 0x042a0001,
			.enclosing = -((int32_t) offsetof(o42a_obj_desc_t, ascendants)),
			.name = "ascendants",
			.type_info = (o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_rlist,
		},
#endif /* NDEBUG */
		.list = 0,
		.size = 0,
	},
	.object_size = sizeof(o42a_obj_t),
};

static inline void vmtc_use(const o42a_obj_vmtc_t *const vmtc) {
	O42A_ENTER(return);
	if (vmtc->prev) {
		// Not terminator. Increase the reference count.
		o42a_refcount_block_t *const block =
				o42a_refcount_blockof(vmtc);
		__sync_add_and_fetch(&block->ref_count, 1);
	}
	O42A_RETURN;
}

const o42a_obj_vmtc_t *o42a_obj_vmtc_alloc(
		const o42a_obj_vmt_t *const vmt,
		const o42a_obj_vmtc_t *const prev) {
	O42A_ENTER(return NULL);

	const o42a_obj_vmt_t *const prev_vmt = prev->vmt;

	if (vmt == prev_vmt) {
		// Reuse the previous link chain with the same VMT.
		O42A_RETURN prev;
	}
	if (prev_vmt->size <= sizeof(o42a_obj_vmt_t)) {
		// The previous VMT is empty. No point in chaining with it.
		O42A_RETURN &vmt->terminator;
	}

	o42a_refcount_block_t *const block =
			O42A(o42a_refcount_balloc(sizeof(o42a_obj_vmtc_t)));

	if (!block) {
		O42A(o42a_obj_vmtc_free(prev));
		O42A_RETURN NULL;
	}

	o42a_obj_vmtc_t *const vmtc = O42A(o42a_refcount_data(block));

#ifndef NDEBUG
	O42A(o42a_dbg_fill_header(
			(const o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_vmtc,
			&vmtc->__o42a_dbg_header__,
			NULL));
#endif /* NDEBUG */

	vmtc->vmt = vmt;
	vmtc->prev = prev;
	O42A(vmtc_use(prev));

	O42A_RETURN vmtc;
}

/**
 * Releases the VMT chain.
 *
 * If VMT chain link is allocated with vmtc_alloc method, then decreases its
 * reference count and deallocates it if it is dropped to zero.
 * Also releases a previous link.
 *
 * If the chain link is terminator, then does nothing.
 *
 * \param vmtc VMT chain to release.
 */
static inline void vmtc_release(const o42a_obj_vmtc_t *vmtc) {
	O42A_ENTER(return);
	for (;;) {

		const o42a_obj_vmtc_t *const prev = vmtc->prev;

		if (!prev) {
			// Terminator. It is always statically allocated. Do nothing.
			O42A_RETURN;
		}

		o42a_refcount_block_t *const block = o42a_refcount_blockof(vmtc);

		if (__sync_sub_and_fetch(&block->ref_count, 1)) {
			// Chain link is still used.
			O42A_RETURN;
		}

		// Chain link is no longer used.
		// Free it and release the previous one.
		O42A(o42a_refcount_free(block));
		vmtc = prev;
	}
}

void o42a_obj_vmtc_free(const o42a_obj_vmtc_t *vmtc) {
	O42A_ENTER(return);

	const o42a_obj_vmtc_t *const prev = vmtc->prev;

	if (!prev) {
		// Terminator. It is always statically allocated. Do nothing.
		O42A_RETURN;
	}

	volatile uint64_t *const ref_count =
			&o42a_refcount_blockof(vmtc)->ref_count;

	if (*ref_count) {
		// The chain is still referenced. Do nothing.
		O42A_RETURN;
	}

	O42A(vmtc_release(prev));

	O42A_RETURN;
}

static void o42a_obj_gc_marker(void *const obj_data) {
	O42A_ENTER(return);

	o42a_obj_t *const object = obj_data;
	o42a_obj_data_t *const data = &object->object_data;
	const volatile o42a_val_t *const value = &data->value;
	const uint32_t flags = value->flags;

	if (flags & O42A_VAL_CONDITION) {
		data->desc->value_type->mark(object);
	}

	const o42a_obj_desc_t *const desc = data->desc;
	uint32_t num_asc = desc->ascendants.size;

	if (!num_asc) {
		O42A_RETURN;
	}

	// Mark all fields.
	const o42a_obj_ascendant_t *asc = O42A(o42a_obj_ascendants(desc));

	while (1) {

		const o42a_obj_desc_t *const body_desc = asc->desc;
		uint32_t num_fields = body_desc->fields.size;

		if (num_fields) {

			o42a_obj_field_t *field = O42A(o42a_obj_fields(body_desc));

			while (1) {

				o42a_fld *const fld = O42A(o42a_fld_by_field(object, field));
				o42a_fld_desc_t *const fld_desc = O42A(o42a_fld_desc(field));

				o42a_debug_mem_name("Mark field: ", fld);
				O42A(fld_desc->mark(fld));

				if (!(--num_fields)) {
					break;
				}
				++field;
			}
		}

		if (!(--num_asc)) {
			break;
		}
		++asc;
	}

	O42A_RETURN;
}

static void o42a_obj_gc_sweeper(void *const obj_data) {
	O42A_ENTER(return);

	o42a_obj_t *const object = obj_data;
	o42a_obj_data_t *const data = &object->object_data;
	const volatile o42a_val_t *const value = &data->value;
	const uint32_t flags = value->flags;

	if (flags & O42A_VAL_CONDITION) {
		data->desc->value_type->sweep(object);
	}

	o42a_debug_mem_name("Sweep object: ", object);

	O42A(vmtc_release(data->vmtc));

	const o42a_obj_desc_t *const desc = data->desc;
	uint32_t num_asc = desc->ascendants.size;

	if (!num_asc) {
		O42A_RETURN;
	}

	// Mark all fields.
	const o42a_obj_ascendant_t *asc = O42A(o42a_obj_ascendants(desc));

	while (1) {

		const o42a_obj_desc_t *const body_desc = asc->desc;
		uint32_t num_fields = body_desc->fields.size;

		if (num_fields) {

			o42a_obj_field_t *field = O42A(o42a_obj_fields(body_desc));

			while (1) {

				o42a_fld *const fld = O42A(o42a_fld_by_field(object, field));
				o42a_fld_desc_t *const fld_desc = O42A(o42a_fld_desc(field));

				O42A(fld_desc->sweep(fld));

				if (!(--num_fields)) {
					break;
				}
				++field;
			}
		}

		if (!(--num_asc)) {
			break;
		}
		++asc;
	} while (num_asc);

	O42A_RETURN;
}

const o42a_gc_desc_t o42a_obj_gc_desc = {
	.mark = &o42a_obj_gc_marker,
	.sweep = &o42a_obj_gc_sweeper,
};

static pthread_mutexattr_t recursive_mutex_attr;

void o42a_init() {
	O42A_ENTER(return);
	if (O42A(pthread_mutexattr_init(&recursive_mutex_attr))
			|| O42A(pthread_mutexattr_settype(
					&recursive_mutex_attr,
					PTHREAD_MUTEX_RECURSIVE))) {
		o42a_error_print("Can not initialize o42a runtime");
		exit(EXIT_FAILURE);
	}
	O42A_RETURN;
}

static inline void obj_mutex_init(o42a_obj_data_t *const data) {
	O42A_ENTER(return);
	if (O42A(pthread_mutex_init(&data->mutex, &recursive_mutex_attr))
			|| O42A(pthread_cond_init(&data->thread_cond, NULL))) {
		o42a_error_print("Failed to initialize an object mutex");
	}
	O42A_RETURN;
}

static void derive_object_body(
		o42a_obj_ctable_t *const ctable,
		const o42a_bool_t inherit) {
	O42A_ENTER(return);
	O42A_DO("Derive body");

	o42a_debug_mem_name(
			inherit ?  "Inherited body: ": "Main body: ",
			ctable->body_desc);

	// Derive fields.
	const o42a_obj_t *const from = ctable->from;
	const size_t num_fields = ctable->body_desc->fields.size;
	o42a_obj_field_t *const fields =
			O42A(o42a_obj_fields(ctable->body_desc));

	if (!from) {
		ctable->from_fld = NULL;
	}
	for (size_t i = 0; i < num_fields; ++i) {

		o42a_obj_field_t *const field = fields + i;

		ctable->field = field;

		const o42a_fld_desc_t *const desc = O42A(o42a_fld_desc(field));

		O42A_DO(inherit ? "Inherit field" : "Propagate field");
		if (from) {
			ctable->from_fld = O42A(o42a_fld_by_field(from, field));
			o42a_debug_mem_name("From: ", ctable->from_fld);
		}
		ctable->to_fld = O42A(o42a_fld_by_field(ctable->to, field));
		O42A_DEBUG("To: <0x%lx>\n", (long) ctable->to_fld);
		o42a_debug_dump_mem("Field: ", field, 3);

		O42A((inherit ? desc->inherit : desc->propagate) (ctable));

		O42A_DONE;
	}

	O42A_DONE;
	O42A_RETURN;
}

static void derive_ancestor_bodies(
		o42a_obj_ctable_t *const ctable,
		const size_t excluded,
		o42a_bool_t inherit) {
	O42A_ENTER(return);

	const o42a_obj_data_t *const data = &ctable->to->object_data;
	const o42a_obj_ascendant_t *ascendant =
			O42A(o42a_obj_ascendants(data->desc));
	const o42a_obj_desc_t *const adesc = ctable->ancestor_desc;
	const o42a_obj_ascendant_t *aascendant =
			O42A(o42a_obj_ascendants(adesc));
	const size_t num = adesc->ascendants.size - excluded;

	for (size_t i = adesc->ascendants.size - excluded; i > 0; --i) {
		assert(
				aascendant->desc == ascendant->desc
				&& "Ancestor and sample body descriptors differ");
		ctable->body_desc = ascendant->desc;
		O42A(derive_object_body(ctable, inherit));
		++aascendant;
		++ascendant;
	}

	O42A_RETURN;
}

o42a_obj_t *o42a_obj_alloc(const o42a_obj_desc_t *const desc) {
	O42A_ENTER(return NULL);

	o42a_obj_t *const object =
			O42A(o42a_gc_alloc(&o42a_obj_gc_desc, desc->object_size));

	if (!object) {
		O42A_RETURN NULL;
	}

#ifndef NDEBUG

	o42a_dbg_header_t *const header = &object->__o42a_dbg_header__;

	O42A(o42a_dbg_fill_header(desc->type_info, header, NULL));
	header->name = "New object";

#endif /* NDEBUG */

	o42a_obj_data_t *const data = &object->object_data;

	data->desc = desc;
	data->vmtc = NULL;
	data->value.flags = O42A_VAL_INDEFINITE;
	data->fld_ctrs = NULL;

	O42A(obj_mutex_init(data));

	O42A_RETURN object;
}


o42a_obj_t *o42a_obj_new(const o42a_obj_ctr_t *const ctr) {
	O42A_ENTER(return NULL);

	o42a_obj_t *const object = ctr->object;
	o42a_obj_data_t *const data = &object->object_data;
	const o42a_obj_desc_t *const desc = data->desc;
	const size_t num_ascendants = desc->ascendants.size;

	const o42a_obj_t *const ancestor = ctr->ancestor;
	const o42a_obj_desc_t *adesc;
	const o42a_obj_vmtc_t *vmtc = data->vmtc;
	size_t consumed_ascendants;

	if (!ancestor) {
		// Ancestor not specified.
		// No inheritable fields expected.
		adesc = desc;
		consumed_ascendants = 0;
	} else {

		const o42a_obj_data_t *const adata = &ancestor->object_data;

		adesc = adata->desc;
		if (adesc == &o42a_obj_none_desc) {
			O42A(o42a_obj_dispose(ctr));
			O42A_RETURN NULL;
		}
		if (!vmtc->prev) {
			vmtc = O42A(o42a_obj_vmtc_alloc(vmtc->vmt, adata->vmtc));
			if (!vmtc) {
				O42A(o42a_obj_dispose(ctr));
				O42A_RETURN NULL;
			}
			data->vmtc = vmtc;
		}

		const size_t adiff = num_ascendants - adesc->ascendants.size;

		assert((adiff == 0 || adiff == 1) && "Inheritance is impossible");
		consumed_ascendants = adiff ? 0 : 1;
	}

	O42A(vmtc_use(vmtc));

	// Inherit ancestor bodies,
	// or propagate all sample bodies if ancestor not specified.
	o42a_obj_ctable_t ctable = {
		.owner = ctr->owner,
		.ancestor_desc = adesc,
		.from = ancestor,
		.to = object,
	};

#ifndef NDEBUG
	O42A(o42a_dbg_fill_header(
			(const o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_ctable,
			&ctable.__o42a_dbg_header__,
			NULL));
#endif /* NDEBUG */

	O42A(derive_ancestor_bodies(&ctable, consumed_ascendants, !!ancestor));

	if (ancestor) {
		// Propagate a main body of the sample,
		// unless all bodies already propagated.
		ctable.body_desc = desc;
		ctable.from = NULL;
		O42A(derive_object_body(&ctable, O42A_FALSE));
	}

	o42a_debug_dump_mem(
			(object->object_data.value.flags & O42A_VAL_CONDITION)
			? "Eager object: "
			: "Object: ",
			object,
			3);

	O42A_RETURN object;
}

void o42a_obj_dispose(const o42a_obj_ctr_t *const ctr) {
	O42A_ENTER(return);

	const o42a_obj_data_t *const data = &ctr->object->object_data;

	if (data->vmtc) {
		O42A(o42a_obj_vmtc_free(data->vmtc));
	}

	const o42a_val_t *const value = &data->value;

	if (value->flags & O42A_VAL_CONDITION) {
		O42A(data->desc->value_type->discard(value));
	}

	O42A(o42a_gc_free(o42a_gc_blockof(ctr->object)));

	O42A_RETURN;
}

o42a_bool_t o42a_obj_cond(o42a_obj_t *const object) {
	O42A_ENTER(return O42A_FALSE);

	o42a_val_t val = {
		.flags = O42A_VAL_INDEFINITE,
	};

#ifndef NDEBUG
	O42A(o42a_dbg_fill_header(
			(const o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_val,
			&val.__o42a_dbg_header__,
			NULL));
#endif /* NDEBUG */

	o42a_obj_data_t *const data = &object->object_data;

	O42A(data->value_f(&val, object));

	if (!(val.flags & O42A_VAL_CONDITION)) {
		O42A_RETURN O42A_FALSE;
	}

	O42A(data->desc->value_type->discard(&val));

	O42A_RETURN O42A_TRUE;
}

void o42a_obj_value_eager(
		o42a_val_t *const result,
		o42a_obj_t *const object) {
	O42A_ENTER(return);

	o42a_obj_data_t *const data = &object->object_data;

	assert(
			(data->value.flags & O42A_VAL_CONDITION)
			&& "Object value is not eagerly evaluated");

	O42A(data->desc->value_type->use(&data->value, result));
	o42a_debug_dump_mem("Eager value: ", result, 3);

	O42A_RETURN;
}

void o42a_obj_value_false(
		o42a_val_t *const result,
		o42a_obj_t *const object __attribute__((unused))) {
	O42A_ENTER(return);
	result->flags = O42A_FALSE;
	O42A_RETURN;
}


void o42a_obj_value_void(
		o42a_val_t *const result,
		o42a_obj_t *const object __attribute__((unused))) {
	O42A_ENTER(return);
	result->flags = O42A_TRUE;
	O42A_RETURN;
}


void o42a_obj_value_unknown(
		o42a_val_t *const result __attribute__((unused)),
		o42a_obj_t *const object __attribute__((unused))) {
	O42A_ENTER(return);
	O42A_RETURN;
}

void o42a_obj_value_stub(
		o42a_val_t *const result,
		o42a_obj_t *const object __attribute__((unused))) {
	O42A_ENTER(return);
	o42a_error_print("Object value stub invoked");
	result->flags = O42A_FALSE;
	O42A_RETURN;
}


static void static_obj_init(const o42a_gc_block_t *block) {
	O42A_ENTER(return);

	o42a_obj_t *const object = O42A(o42a_gc_dataof(block));

	O42A(obj_mutex_init(&object->object_data));

	O42A_RETURN;
}

inline void o42a_obj_static(o42a_obj_t *const object) {
	O42A_ENTER(return);
	o42a_gc_block_t *const gc_block = O42A(o42a_gc_blockof(object));
	if (!gc_block->list) {
		O42A(o42a_gc_static(gc_block, &static_obj_init));
	}
	O42A_RETURN;
}

void o42a_obj_lock(o42a_obj_t *const object) {
	O42A_ENTER(return);
	O42A(o42a_obj_static(object));
	// Lock the mutex.
	if (O42A(pthread_mutex_lock(&object->object_data.mutex))) {
		o42a_error_print("Failed to lock an object mutex");
	}
	O42A_RETURN;
}

void o42a_obj_unlock(o42a_obj_t *const object) {
	O42A_ENTER(return);
	if (O42A(pthread_mutex_unlock(&object->object_data.mutex))) {
		o42a_error_print("Current thread does not own an object mutex");
	}
	O42A_RETURN;
}

void o42a_obj_wait(o42a_obj_t *const object) {
	O42A_ENTER(return);
	if (O42A(pthread_cond_wait(
			&object->object_data.thread_cond,
			&object->object_data.mutex))) {
		o42a_error_print("Current thread does not own an object mutex");
	}
	O42A_RETURN;
}

void o42a_obj_signal(o42a_obj_t *const object) {
	O42A_ENTER(return);
	if (O42A(pthread_cond_signal(&object->object_data.thread_cond))) {
		o42a_error_print("Failed to send signal to an object condition waiter");
	}
	O42A_RETURN;
}

void o42a_obj_broadcast(o42a_obj_t *const object) {
	O42A_ENTER(return);
	if (O42A(pthread_cond_broadcast(&object->object_data.thread_cond))) {
		o42a_error_print(
				"Failed to broadcast signal to an object condition waiters");
	}
	O42A_RETURN;
}

void o42a_obj_use(o42a_obj_t *const object) {
	O42A_ENTER(return);
	o42a_debug_mem_name("Use object: ", object);
	O42A(o42a_gc_use(o42a_gc_blockof(object)));
	O42A_RETURN;
}

static inline o42a_gc_block_t *gc_block_by_object(void *const ptr) {
	O42A_ENTER(return NULL);
	O42A_RETURN o42a_gc_blockof(ptr);
}

o42a_obj_t *o42a_obj_use_mutable(o42a_obj_t **const var) {
	O42A_ENTER(return NULL);
	o42a_obj_t *const result =
			O42A(o42a_gc_use_mutable((void **) var, &gc_block_by_object));
	O42A_RETURN result;
}

void o42a_obj_start_use(o42a_obj_use_t *const use, o42a_obj_t *const object) {
	O42A_ENTER(return);

	assert(
			!use->object
			&& "Object use instance already utilized by another object");

	if (O42A(o42a_gc_use(o42a_gc_blockof(object)))) {
		o42a_debug_mem_name("Start object use: ", object);
		use->object = object;
	} else {
		o42a_debug_mem_name("Static object: ", object);
	}

	O42A_RETURN;
}

void o42a_obj_end_use(o42a_obj_use_t *const use) {
	O42A_ENTER(return);

	if (use->object) {
		o42a_debug_mem_name("End object use: ", use->object);
		O42A(o42a_gc_unuse(o42a_gc_blockof(use->object)));
		use->object = NULL;
	}

	O42A_RETURN;
}

void o42a_obj_start_val_use(const o42a_val_t *const val) {
	O42A_ENTER(return);

	if (!(val->flags & O42A_VAL_CONDITION)) {
		O42A_RETURN;
	}

	o42a_obj_t *const obj = val->value.v_ptr;

	if (!obj) {
		O42A_RETURN;
	}

	o42a_debug_mem_name("Start link target use: ", obj);

	O42A(o42a_gc_use(o42a_gc_blockof(obj)));

	O42A_RETURN;
}

void o42a_obj_end_val_use(const o42a_val_t *const val) {
	O42A_ENTER(return);

	if (!(val->flags & O42A_VAL_CONDITION)) {
		O42A_RETURN;
	}

	o42a_obj_t *const obj = val->value.v_ptr;

	if (!obj) {
		O42A_RETURN;
	}

	o42a_debug_mem_name("End link target use: ", obj);

	O42A(o42a_gc_unuse(o42a_gc_blockof(obj)));

	O42A_RETURN;
}
