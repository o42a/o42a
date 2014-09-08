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
	.field_num = 12,
	.name = "o42a_obj_data_t",
	.fields = {
		{
			.data_type = O42A_TYPE_INT8,
			.offset = offsetof(o42a_obj_data_t, mutex_init),
			.name = "mutex_init",
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
			.data_type = O42A_TYPE_FUNC_PTR,
			.offset = offsetof(o42a_obj_data_t, value_f),
			.name = "value_f",
		},
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_data_t, vmtc),
			.name = "vmtc",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_vmtc,
		},
		{
			.data_type = O42A_TYPE_FUNC_PTR,
			.offset = offsetof(o42a_obj_data_t, cond_f),
			.name = "cond_f",
		},
		{
			.data_type = O42A_TYPE_FUNC_PTR,
			.offset = offsetof(o42a_obj_data_t, def_f),
			.name = "def_f",
		},
		{
			.data_type = O42A_TYPE_STRUCT,
			.offset = offsetof(o42a_obj_data_t, value),
			.name = "value",
			.type_info = (o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_val,
		},
		{
			.data_type = O42A_TYPE_PTR,
			.offset = offsetof(o42a_obj_data_t, resume_from),
			.name = "resume_from",
		},
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_data_t, desc),
			.name = "desc",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_desc,
		},
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_data_t, fld_ctrs),
			.name = "fld_ctrs",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_fld_ctr,
		},
		{
			.data_type = O42A_TYPE_STRUCT,
			.offset = offsetof(o42a_obj_data_t, deps),
			.name = "deps",
			.type_info = (o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_rlist,
		},
	},
};

const o42a_dbg_type_info4f_t _O42A_DEBUG_TYPE_o42a_obj_desc = {
	.type_code = 0x042a0101,
	.field_num = 4,
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

const o42a_dbg_type_info6f_t _O42A_DEBUG_TYPE_o42a_obj_ctr = {
	.type_code = 0x042a0120,
	.field_num = 6,
	.name = "o42a_obj_ctr_t",
	.fields = {
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
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_ctr_t, sample),
			.name = "sample",
		},
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_ctr_t, vmtc),
			.name = "vmtc",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_vmtc,
		},
		{
			.data_type = O42A_TYPE_STRUCT,
			.offset = offsetof(o42a_obj_ctr_t, value),
			.name = "value",
			.type_info = (o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_val,
		},
		{
			.data_type = O42A_TYPE_INT32,
			.offset = offsetof(o42a_obj_ctr_t, num_deps),
			.name = "num_deps",
		},
	},
};

const struct _O42A_DEBUG_TYPE_o42a_obj_ctable
_O42A_DEBUG_TYPE_o42a_obj_ctable = {
	.type_code = 0x042a0122,
	.field_num = 9,
	.name = "o42a_obj_ctable_t",
	.fields = {
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_ctable_t, owner),
			.name = "owner",
		},
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_ctable_t, ancestor),
			.name = "ancestor",
		},
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_ctable_t, sample),
			.name = "sample",
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

static inline o42a_obj_t **o42a_obj_deps(const o42a_obj_data_t *const data) {
	return (o42a_obj_t **) (((char *) &data->deps) + data->deps.list);
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

	const size_t num_deps = data->deps.size;

	if (!num_deps) {
		O42A_RETURN;
	}

	// Mark all deps.
	o42a_obj_t **const deps = O42A(o42a_obj_deps(data));

	for (size_t i = 0; i < num_deps; ++i) {
		O42A_DEBUG("Mark dep #%zd\n", i);

		o42a_obj_t *const dep = deps[i];

		if (!dep) {
			continue;
		}

		o42a_debug_mem_name("Dep: ", object);

		O42A(o42a_gc_mark(o42a_gc_blockof(dep)));
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

/**
 * Object body derivation kind.
 */
enum derivation_kind {

	/**
	 * Inherited ancestor body.
	 */
	DK_INHERIT = 0x01,

	/**
	 * Sample body not present in ancestor.
	 */
	DK_DERIVE = 0x12,

	/**
	 * Sample body present in ancestor.
	 */
	DK_OVERRIDE = 0x13,

};

static void derive_object_body(
		o42a_obj_ctable_t *const ctable,
		const enum derivation_kind dkind) {
	O42A_ENTER(return);
	O42A_DO("Derive body");

	o42a_debug_mem_name(
			dkind == DK_DERIVE ? "Main body: "
			: dkind == DK_OVERRIDE ? "Overridden main body: "
			: dkind == DK_INHERIT ? "Inherited body: "
			: "Derived body: ",
			ctable->body_desc);

	// Derive fields.
	const size_t num_fields = ctable->body_desc->fields.size;
	o42a_obj_field_t *const fields =
			O42A(o42a_obj_fields(ctable->body_desc));
	const o42a_bool_t propagate =
			dkind == DK_DERIVE || dkind == DK_OVERRIDE;

	for (size_t i = 0; i < num_fields; ++i) {

		o42a_obj_field_t *const field = fields + i;

		ctable->field = field;
		ctable->from_fld = O42A(o42a_fld_by_field(ctable->from, field));
		ctable->to_fld = O42A(o42a_fld_by_field(ctable->to, field));

		const o42a_fld_desc_t *const desc = O42A(o42a_fld_desc(field));

		O42A_DO(propagate ? "Propagate field" : "Inherit field");
		o42a_debug_mem_name("From: ", ctable->from_fld);
		O42A_DEBUG("To: <0x%lx>\n", (long) ctable->to_fld);
		o42a_debug_dump_mem("Field: ", field, 3);

		O42A((propagate ? desc->propagate : desc->inherit) (ctable));

		O42A_DONE;
	}

	O42A_DONE;
	O42A_RETURN;
}

static void derive_ancestor_bodies(
		o42a_obj_ctable_t *const ctable,
		const size_t excluded,
		const enum derivation_kind dkind) {
	O42A_ENTER(return);

	const o42a_obj_data_t *const data = &ctable->to->object_data;
	const o42a_obj_ascendant_t *ascendant =
			O42A(o42a_obj_ascendants(data->desc));
	const o42a_obj_desc_t *const adesc =
			ctable->ancestor->object_data.desc;
	const o42a_obj_ascendant_t *aascendant =
			O42A(o42a_obj_ascendants(adesc));
	const size_t num = adesc->ascendants.size - excluded;

	for (size_t i = adesc->ascendants.size - excluded; i > 0; --i) {
		assert(
				aascendant->desc == ascendant->desc
				&& "Ancestor and sample body descriptors differ");
		ctable->body_desc = ascendant->desc;
		O42A(derive_object_body(ctable, dkind));
		++aascendant;
		++ascendant;
	}

	O42A_RETURN;
}


static inline void fill_deps(const o42a_obj_data_t *const data) {

	const size_t num_deps = data->deps.size;
	o42a_obj_t **const deps = o42a_obj_deps(data);

	// Fill deps field info.
	for (size_t i = 0; i < num_deps; ++i) {
		deps[i] = NULL;// To be able to dump the object before deps filled.
	}
}


#ifndef NDEBUG

static inline size_t count_fields(const o42a_obj_desc_t *const desc) {
	O42A_ENTER(return 0);

	const size_t num_asc = desc->ascendants.size;
	const o42a_obj_ascendant_t *const ascs = O42A(o42a_obj_ascendants(desc));
	size_t num_fields = 0;

	for (size_t i = 0; i < num_asc; ++i) {
		num_fields += ascs[i].desc->fields.size;
	}

	O42A_DEBUG("Number of fields: %zu\n", num_fields);

	O42A_RETURN num_fields;
}

static inline size_t get_type_info_start(
		size_t *const size,
		const size_t type_field_num) {
	O42A_ENTER(return 0);

	size_t s = *size;
	static const o42a_layout_t type_info_layout =
			O42A_LAYOUT(o42a_dbg_type_info_t);
	static const o42a_layout_t field_info_layout =
			O42A_LAYOUT(o42a_dbg_field_info_t);
	const size_t type_info_start = s =
			O42A(o42a_layout_pad(s, type_info_layout));

	s += O42A(o42a_layout_size(type_info_layout));
	s += O42A(o42a_layout_pad(s, field_info_layout));
	s += O42A(o42a_layout_array_size(field_info_layout, type_field_num));

	*size = s;

	O42A_RETURN type_info_start;
}

static inline void fill_type_info(
		o42a_obj_t *const object,
		o42a_dbg_type_info_t *const type_info) {
	O42A_ENTER(return);

	// Fill new object's type info (without field info yet).
	type_info->type_code = rand();
	type_info->name = "New object";

	// Fill object debug header.
	o42a_dbg_header_t *const header = &object->__o42a_dbg_header__;

	header->type_code = type_info->type_code;
	header->enclosing = 0;
	header->name = type_info->name;
	header->type_info = type_info;

	// Fill object data debug header.
	o42a_dbg_header_t *const data_header =
			&object->object_data.__o42a_dbg_header__;

	O42A(o42a_dbg_fill_header(
			(const o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_data,
			data_header,
			header));
	data_header->name = "object_data";

	O42A_RETURN;
}

static inline void fill_field_infos(
		const o42a_obj_t *const from,
		o42a_obj_t *const object,
		o42a_dbg_type_info_t *type_info) {
	O42A_ENTER(return);

	o42a_obj_data_t *const data = &object->object_data;
	const o42a_dbg_header_t *const object_header =
			O42A(o42a_dbg_header(object));

	// Fill new object's type field info.
	o42a_dbg_field_info_t *field_info = type_info->fields;

	// Fill object data field info.
	O42A(o42a_dbg_fill_field_info(&data->__o42a_dbg_header__, field_info++));

	// Fill object bodies field info.
	const o42a_obj_ascendant_t *const ascendants =
			O42A(o42a_obj_ascendants(data->desc));
	const size_t num_ascendants = data->desc->ascendants.size;

	for (size_t i = 0; i < num_ascendants; ++i) {

		const o42a_obj_ascendant_t *const asc = ascendants + i;
		const o42a_obj_field_t *const fields =
				O42A(o42a_obj_fields(asc->desc));
		const size_t num_fields = asc->desc->fields.size;

		for (size_t i = 0; i < num_fields; ++i) {

			const o42a_fld *const from_fld =
					O42A(o42a_fld_by_field(from, fields + i));
			o42a_fld *const fld =
					O42A(o42a_fld_by_field(object, fields + i));

			O42A(o42a_dbg_copy_header(
					o42a_dbg_header(from_fld),
					(o42a_dbg_header_t *) fld,
					object_header));
			O42A(o42a_dbg_fill_field_info(
					o42a_dbg_header(fld),
					field_info++));
		}
	}

	const size_t num_deps = data->deps.size;
	o42a_obj_t **const deps = o42a_obj_deps(data);

	// Fill deps field info.
	for (size_t i = 0; i < num_deps; ++i) {
		field_info->data_type = O42A_TYPE_DATA_PTR;
		field_info->offset = ((char *) (deps + i)) - ((char *) object);
		field_info->name = "D";
		field_info->type_info = NULL;
		++field_info;
	}

	O42A_RETURN;
}

#endif /* NDEBUG */


static o42a_obj_t *new_obj(const o42a_obj_ctr_t *const ctr) {
	O42A_ENTER(return NULL);

	const o42a_obj_t *const ancestor = ctr->ancestor;
	const o42a_obj_data_t *const adata = &ancestor->object_data;

	assert(ancestor && "Ancestor is missing");

	if (adata->desc == &o42a_obj_none_desc) {
		O42A_RETURN NULL;
	}

	const o42a_obj_t *const sample = ctr->sample;

	assert(sample && "Sample is missing");

	const o42a_obj_data_t *const sdata = &sample->object_data;
	const o42a_obj_desc_t *const sdesc = sdata->desc;

	const size_t num_ascendants = sdesc->ascendants.size;
	const size_t adiff = num_ascendants - adata->desc->ascendants.size;

	assert((adiff == 0 || adiff == 1) && "Inheritance is impossible");

	const size_t consumed_ascendants = adiff ? 0 : 1;
	static const o42a_layout_t dep_layout = O42A_LAYOUT(void *);
	const size_t deps_start = o42a_layout_pad(
			sdesc->object_size,
			dep_layout);
	const size_t num_deps = ctr->num_deps;
	size_t size = deps_start + o42a_layout_array_size(dep_layout, num_deps);

#ifndef NDEBUG

	const size_t type_field_num =
			1 + O42A(count_fields(sdata->desc)) + num_deps;
	const size_t type_info_start =
			O42A(get_type_info_start(&size, type_field_num));

#endif

	char *const mem = O42A(o42a_gc_alloc(&o42a_obj_gc_desc, size));
	o42a_obj_t *const object = (o42a_obj_t *) mem;
	o42a_obj_data_t *const data = &object->object_data;
	void **const deps = (void **) (mem + deps_start);

#ifndef NDEBUG

	// Fill new object's type info (without field info yet).
	o42a_dbg_type_info_t *type_info =
			(o42a_dbg_type_info_t*) (mem + type_info_start);

	O42A(fill_type_info(object, type_info));
	type_info->field_num = type_field_num;

#endif

	// Fill object data without value and VMT.
	data->mutex_init = 0;
	data->value_f = sdata->value_f;
	data->cond_f = sdata->cond_f;
	data->resume_from = NULL;
	data->desc = sdesc;

	data->fld_ctrs = NULL;
	data->deps.list =
			num_deps ? ((char *) deps) - ((char *) &data->deps) : 0;
	data->deps.size = num_deps;

#ifndef NDEBUG
	O42A(fill_field_infos(sample, object, type_info));
#endif /* NDEBUG */

	// propagate sample and inherit ancestor
	o42a_obj_ctable_t ctable = {
		.owner = ctr->owner,
		.ancestor = ancestor,
		.sample = sample,
		.from = ancestor,
		.to = object,
	};

#ifndef NDEBUG
	O42A(o42a_dbg_fill_header(
			(const o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_ctable,
			&ctable.__o42a_dbg_header__,
			NULL));
#endif /* NDEBUG */

	derive_ancestor_bodies(&ctable, consumed_ascendants, DK_INHERIT);

	ctable.body_desc = sdesc;
	ctable.from = sample;
	O42A(derive_object_body(
			&ctable,
			consumed_ascendants ? DK_OVERRIDE : DK_DERIVE));

	O42A_RETURN object;
}

o42a_obj_t *o42a_obj_new(const o42a_obj_ctr_t *const ctr) {
	O42A_ENTER(return NULL);

	assert(
			(ctr->value.flags & O42A_VAL_INDEFINITE)
			&& "Value is eagerly evaluated");

	o42a_obj_t *const object = O42A(new_obj(ctr));

	if (!object) {
		if (ctr->vmtc) {
			O42A(o42a_obj_vmtc_free(ctr->vmtc));
		}
		O42A_RETURN NULL;
	}

	const o42a_obj_data_t *const adata = &ctr->ancestor->object_data;
	const o42a_obj_data_t *const sdata = &ctr->sample->object_data;
	o42a_obj_data_t *const data = &object->object_data;

	assert(
			!(adata->value.flags & O42A_VAL_EAGER)
			&& "Ancestor value is eagerly evaluated");
	assert(
			!(sdata->value.flags & O42A_VAL_EAGER)
			&& "Sample value is eagerly evaluated");

	const o42a_obj_vmtc_t *vmtc;

	if (ctr->vmtc) {
		vmtc = ctr->vmtc;
	} else {
		vmtc = O42A(o42a_obj_vmtc_alloc(sdata->vmtc->vmt, adata->vmtc));
		if (!vmtc) {
			O42A(o42a_gc_free(o42a_gc_blockof(object)));
			O42A_RETURN NULL;
		}
	}

	O42A(vmtc_use(vmtc));

	data->vmtc = vmtc;
	data->value.flags = O42A_VAL_INDEFINITE;
	data->def_f =
			(sdata->vmtc->vmt->flags & O42A_OBJ_ANCESTOR_DEF)
			? adata->def_f
			: sdata->def_f;

	O42A(fill_deps(data));

	o42a_debug_dump_mem("Object: ", object, 3);

	O42A_RETURN object;
}

o42a_obj_t *o42a_obj_eager(o42a_obj_ctr_t *const ctr) {
	O42A_ENTER(return NULL);

	assert(
			!(ctr->value.flags & O42A_VAL_INDEFINITE)
			&& "Indefinite eagerly evaluated value");

	const o42a_obj_t *const ancestor = ctr->ancestor;
	const o42a_obj_data_t *const adata = &ancestor->object_data;
	const size_t num_deps = adata->deps.size;

	ctr->sample = ancestor;
	ctr->num_deps = num_deps;

	o42a_obj_t *const object = O42A(new_obj(ctr));

	if (!object) {
		if (ctr->vmtc) {
			O42A(o42a_obj_vmtc_free(ctr->vmtc));
		}
		O42A_RETURN NULL;
	}

	o42a_obj_data_t *const data = &object->object_data;

	const o42a_obj_vmtc_t *const vmtc = ctr->vmtc ? ctr->vmtc : adata->vmtc;

	O42A(vmtc_use(vmtc));

	data->vmtc = vmtc;
	data->def_f = adata->def_f;
	O42A(adata->desc->value_type->copy(&ctr->value, &data->value));
	data->value.flags |= O42A_VAL_EAGER;

	if (num_deps) {

		o42a_obj_data_t *const data = &object->object_data;
		void **const adeps =
				(void **) (((char *) &adata->deps) + adata->deps.list);
		void **const deps =
				(void **) (((char *) &data->deps) + data->deps.list);

		for (size_t i = 0; i < num_deps; ++i) {
			deps[i] = adeps[i];
		}
	}

	o42a_debug_dump_mem("Eager object: ", object, 3);

	O42A_RETURN object;
}

void o42a_obj_value_false(
		o42a_val_t *const result,
		o42a_obj_t *const object __attribute__((unused))) {
	O42A_ENTER(return);
	result->flags = O42A_FALSE;
	O42A_RETURN;
}

o42a_bool_t o42a_obj_cond_false(
		o42a_obj_t *const object __attribute__((unused))) {
	O42A_ENTER(return O42A_FALSE);
	O42A_RETURN O42A_FALSE;
}


void o42a_obj_value_void(
		o42a_val_t *const result,
		o42a_obj_t *const object __attribute__((unused))) {
	O42A_ENTER(return);
	result->flags = O42A_TRUE;
	O42A_RETURN;
}

o42a_bool_t o42a_obj_cond_true(
		o42a_obj_t *const object __attribute__((unused))) {
	O42A_ENTER(return O42A_FALSE);
	O42A_RETURN O42A_TRUE;
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

o42a_bool_t o42a_obj_cond_stub(
		o42a_obj_t *const object __attribute__((unused))) {
	O42A_ENTER(return O42A_FALSE);
	o42a_error_print("Object condition stub invoked");
	O42A_RETURN O42A_FALSE;
}


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

void o42a_obj_lock(o42a_obj_t *const object) {
	O42A_ENTER(return);

	o42a_obj_data_t *const data = &object->object_data;

	// Ensure the mutex is initialized.
	while (1) {

		// Attempt to start the mutex initialization.
		volatile int8_t *const init = &data->mutex_init;
		int8_t old = __sync_val_compare_and_swap(init, 0, -1);

		if (old) {
			if (old > 0) {
				__sync_synchronize();
				// The mutex is initialized already.
				break;
			}
			// The mutext is currently initializing by another thread.
			O42A(sched_yield());
			continue;
		}

		// Initialize the (recursive) mutex.
		if (O42A(pthread_mutex_init(&data->mutex, &recursive_mutex_attr))
				|| O42A(pthread_cond_init(&data->thread_cond, NULL))) {
			o42a_error_print("Failed to initialize an object mutex");
		}

		o42a_gc_block_t *const gc_block = O42A(o42a_gc_blockof(object));

		if (!gc_block->list) {
			O42A(o42a_gc_static(gc_block));
		}

		__sync_synchronize();
		*init = 1;

		break;
	}

	// Lock the mutex.
	if (O42A(pthread_mutex_lock(&data->mutex))) {
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

void o42a_obj_start_val_use(o42a_val_t *const val) {
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

void o42a_obj_end_val_use(o42a_val_t *const val) {
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
