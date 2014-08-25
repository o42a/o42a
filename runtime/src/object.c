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
	.field_num = 16,
	.name = "o42a_obj_data_t",
	.fields = {
		{
			.data_type = O42A_TYPE_REL_PTR,
			.offset = offsetof(o42a_obj_data_t, object),
			.name = "object",
		},
		{
			.data_type = O42A_TYPE_REL_PTR,
			.offset = offsetof(o42a_obj_data_t, start),
			.name = "start",
		},
		{
			.data_type = O42A_TYPE_INT16,
			.offset = offsetof(o42a_obj_data_t, flags),
			.name = "flags",
		},
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
			.offset = offsetof(o42a_obj_data_t, value_type),
			.name = "value_type",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_val_type,
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
			.offset = offsetof(o42a_obj_data_t, ascendants),
			.name = "ascendants",
			.type_info = (o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_rlist,
		},
		{
			.data_type = O42A_TYPE_STRUCT,
			.offset = offsetof(o42a_obj_data_t, deps),
			.name = "deps",
			.type_info = (o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_rlist,
		},
	},
};

const o42a_dbg_type_info2f_t _O42A_DEBUG_TYPE_o42a_obj_desc = {
	.type_code = 0x042a0101,
	.field_num = 2,
	.name = "o42a_obj_desc_t",
	.fields = {
		{
			.data_type = O42A_TYPE_STRUCT,
			.offset = offsetof(o42a_obj_desc_t, fields),
			.name = "fields",
			.type_info = (o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_rlist,
		},
		{
			.data_type = O42A_TYPE_INT32,
			.offset = offsetof(o42a_obj_desc_t, main_body_layout),
			.name = "main_body_layout",
		},
	},
};

const o42a_dbg_type_info2f_t _O42A_DEBUG_TYPE_o42a_obj_ascendant = {
	.type_code = 0x042a0110,
	.field_num = 2,
	.name = "o42a_obj_ascendant_t",
	.fields = {
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_ascendant_t, desc),
			.name = "desc",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_desc,
		},
		{
			.data_type = O42A_TYPE_REL_PTR,
			.offset = offsetof(o42a_obj_ascendant_t, body),
			.name = "body",
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

const o42a_dbg_type_info4f_t _O42A_DEBUG_TYPE_o42a_obj_ctr = {
	.type_code = 0x042a0120,
	.field_num = 3,
	.name = "o42a_obj_ctr_t",
	.fields = {
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_ctr_t, owner_data),
			.name = "owner_data",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_data,
		},
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_ctr_t, ancestor_data),
			.name = "ancestor_data",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_data,
		},
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_ctr_t, sample_data),
			.name = "sample_data",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_data,
		},
		{
			.data_type = O42A_TYPE_INT32,
			.offset = offsetof(o42a_obj_ctr_t, num_deps),
			.name = "num_deps",
		},
	},
};

const o42a_dbg_type_info2f_t _O42A_DEBUG_TYPE_o42a_obj_cside = {
	.type_code = 0x042a0121,
	.field_num = 2,
	.name = "o42a_obj_cside_t",
	.fields = {
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(struct o42a_obj_cside, body),
			.name = "body",
		},
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(struct o42a_obj_cside, fld),
			.name = "fld",
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
			.offset = offsetof(o42a_obj_ctable_t, owner_data),
			.name = "owner_data",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_data,
		},
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_ctable_t, ancestor_data),
			.name = "ancestor_data",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_data,
		},
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_ctable_t, sample_data),
			.name = "sample_data",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_data,
		},
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_ctable_t, object_data),
			.name = "object_data",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_data,
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
			.data_type = O42A_TYPE_STRUCT,
			.offset = offsetof(o42a_obj_ctable_t, from),
			.name = "from",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_cside,
		},
		{
			.data_type = O42A_TYPE_STRUCT,
			.offset = offsetof(o42a_obj_ctable_t, to),
			.name = "to",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_cside,
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
			.offset = offsetof(o42a_obj_use_t, data),
			.name = "data",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_data,
		},
	},
};

#endif /* NDEBUG */


extern o42a_obj_data_t *o42a_obj_data(const o42a_obj_body_t *);

extern o42a_obj_t *o42a_obj_by_data(const o42a_obj_data_t *);

extern o42a_obj_ascendant_t *o42a_obj_ascendants(const o42a_obj_data_t *);

extern o42a_obj_field_t *o42a_obj_fields(const o42a_obj_desc_t *);

extern o42a_obj_body_t *o42a_obj_ascendant_body(const o42a_obj_ascendant_t *);

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
	.main_body_layout = O42A_LAYOUT(o42a_obj_body_t),
};

const o42a_obj_desc_t o42a_obj_false_desc = {
#ifndef NDEBUG
	.__o42a_dbg_header__ = {
		.type_code = 0x042a0101,
		.enclosing = 0,
		.name = "o42a_obj_false_desc",
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
	.main_body_layout = O42A_LAYOUT(o42a_obj_body_t),
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
	.main_body_layout = O42A_LAYOUT(o42a_obj_body_t),
};


const o42a_obj_ascendant_t *o42a_obj_ascendant_of_type(
		const o42a_obj_data_t *const data,
		const o42a_obj_desc_t *const desc) {
	O42A_ENTER(return NULL);

	o42a_debug_mem_name("--- Data: ", data);
	o42a_debug_mem_name("--- Type: ", desc);

	const o42a_obj_ascendant_t *ascendant = O42A(o42a_obj_ascendants(data));

	for (size_t i = data->ascendants.size; i > 0; --i) {
		if (ascendant->desc == desc) {
			O42A_RETURN ascendant;
		}
		++ascendant;
		continue;
	}

	O42A_RETURN NULL;
}

static inline o42a_obj_body_t *body_of_type(
		const o42a_obj_data_t *const data,
		const o42a_obj_desc_t *const desc) {
	O42A_ENTER(return NULL);

	const o42a_obj_ascendant_t *const ascendant =
			O42A(o42a_obj_ascendant_of_type(data, desc));

	if (!ascendant) {
		O42A_RETURN NULL;
	}

	O42A_RETURN o42a_obj_ascendant_body(ascendant);
}

o42a_obj_body_t *o42a_obj_cast(
		o42a_obj_t *const object,
		const o42a_obj_desc_t *const desc) {
	O42A_ENTER(return NULL);
	O42A_DO("Cast");

	if (desc == &o42a_obj_void_desc) {
		// any body can be void
		o42a_debug_mem_name("Cast to void: ", object);
		O42A_DONE;
		O42A_RETURN object;
	}
	if (object->declared_in == desc) {
		// body of the necessary type
		o42a_debug_mem_name("Cast not required: ", object);
		o42a_debug_mem_name("     to: ", desc);
		O42A_DONE;
		O42A_RETURN object;
	}

	o42a_debug_mem_name("Cast of: ", object);
	o42a_debug_mem_name("     to: ", desc);

	o42a_obj_body_t *const result =
			O42A(body_of_type(o42a_obj_data(object), desc));

	o42a_debug_mem_name("Cast result: ", result);

	O42A_DONE;
	O42A_RETURN result;
}


static inline void copy_ancestor_ascendants(
		const o42a_obj_data_t *const ancestor_data,
		o42a_obj_ascendant_t *ascendants,
		void *start) {
	O42A_ENTER(return);

	void *astart = ((char *) ancestor_data) + ancestor_data->start;
	const o42a_obj_ascendant_t *aascendants =
			O42A(o42a_obj_ascendants(ancestor_data));
	const o42a_rptr_t aascendants_rptr =
			((char *) aascendants) - ((char *) astart);
	const o42a_rptr_t ascendants_rptr =
			((char *) ascendants) - ((char *) start);
	const o42a_rptr_t diff = aascendants_rptr - ascendants_rptr;

	for (size_t i = ancestor_data->ascendants.size; i > 0; --i) {
#ifndef NDEBUG
		O42A(o42a_dbg_copy_header(
				o42a_dbg_header(aascendants),
				&ascendants->__o42a_dbg_header__,
				(o42a_dbg_header_t*) start));
#endif
		ascendants->desc = aascendants->desc;
		ascendants->body = aascendants->body + diff;
		++aascendants;
		++ascendants;
	}

	O42A_RETURN;
}

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

/**
 * Allocates a new VMT chain.
 *
 * The chain link instances are reference-counted. This function sets the
 * reference count of newly allocated link to one and increases the reference
 * count of previous link in the chain by one, unless it is a terminator link.
 *
 * The allocated link chain can be released by vmtc_release function.
 *
 * If the VMT of the previous link is the same as provided one, then just
 * increases the reference count of previous link and returns it.
 *
 * \param vmt VMT of the new chain link.
 * \param prev previous link in VMT chain.
 *
 * \return a pointer to new VMT chain, or NULL if allocation failed.
 */
static inline const o42a_obj_vmtc_t *vmtc_alloc(
		const o42a_obj_vmt_t *const vmt,
		const o42a_obj_vmtc_t *const prev) {
	O42A_ENTER(return NULL);

	if (vmt == prev->vmt) {
		// Reuse a previous link chain with the same VMT.
		O42A(vmtc_use(prev));
		O42A_RETURN prev;
	}

	o42a_refcount_block_t *const block =
			O42A(o42a_refcount_balloc(sizeof(o42a_obj_vmtc_t)));

	block->ref_count = 1;

	o42a_obj_vmtc_t *const vmtc = o42a_refcount_data(block);

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

static inline const o42a_obj_vmtc_t *vmtc_derive(
		o42a_obj_ctable_t *const ctable) {
	O42A_ENTER(return NULL);

	const o42a_obj_desc_t *const declared_in =
			ctable->from.body->declared_in;
	const o42a_obj_ascendant_t *const aasc =
			O42A(o42a_obj_ascendant_of_type(
					ctable->ancestor_data,
					declared_in));
	const o42a_obj_ascendant_t *const sasc =
			O42A(o42a_obj_ascendant_of_type(
					ctable->sample_data,
					declared_in));

	if (!sasc) {

		const o42a_obj_vmtc_t *const avmtc =
				O42A(o42a_obj_ascendant_body(aasc))->vmtc;

		O42A(vmtc_use(avmtc));

		O42A_RETURN avmtc;
	}

	const o42a_obj_vmtc_t *const svmtc =
			O42A(o42a_obj_ascendant_body(sasc))->vmtc;

	if (!aasc) {
		O42A(vmtc_use(svmtc));
		O42A_RETURN svmtc;
	}

	const o42a_obj_vmtc_t *const prev =
			O42A(o42a_obj_ascendant_body(aasc))->vmtc;

	O42A_RETURN vmtc_alloc(svmtc->vmt, prev);
}

enum derivation_kind {
	DK_COPY,
	DK_INHERIT,
	DK_MAIN,
};

static void derive_object_body(
		o42a_obj_ctable_t *const ctable,
		enum derivation_kind kind) {
	O42A_ENTER(return);
	O42A_DO("Derive body");

	const o42a_obj_body_t *const from_body = ctable->from.body;
	o42a_obj_body_t *const to_body = ctable->to.body;

	O42A_DEBUG("Derive body %lx -> %lx\n", (long) from_body, (long) to_body);
	o42a_debug_mem_name("Body type: ", ctable->body_desc);

#ifndef NDEBUG

	// Fill debug header.
	const o42a_obj_data_t *const object_data = ctable->object_data;
	const o42a_dbg_header_t *const object_header =
			(o42a_dbg_header_t *) (((char *) object_data) + object_data->start);

	O42A(o42a_dbg_copy_header(
			o42a_dbg_header(from_body),
			&to_body->__o42a_dbg_header__,
			object_header));

#endif

	// Fill body header.
	to_body->object_data =
			((char *) ctable->object_data) - ((char *) to_body);
	to_body->declared_in = from_body->declared_in;
	to_body->vmtc = O42A(vmtc_derive(ctable));

	if (kind == DK_INHERIT) {
		// Drop the kind of body to "inherited" for inherited body.
		to_body->flags =
				(from_body->flags & ~O42A_OBJ_BODY_TYPE)
				| O42A_OBJ_BODY_INHERITED;
	} else {
		// Keep the kind of body otherwise.
		to_body->flags = from_body->flags;
	}

	// Derive fields.
	const size_t num_fields = ctable->body_desc->fields.size;
	o42a_obj_field_t *const fields =
			O42A(o42a_obj_fields(ctable->body_desc));

	for (size_t i = 0; i < num_fields; ++i) {

		o42a_obj_field_t *const field = fields + i;

		ctable->field = field;
		ctable->from.fld = O42A(o42a_fld_by_field(from_body, field));
		ctable->to.fld = O42A(o42a_fld_by_field(to_body, field));

		const o42a_fld_desc_t *const desc = O42A(o42a_fld_desc(field));

		O42A_DO(kind == DK_INHERIT ? "Inherit field" : "Propagate field");
		o42a_debug_mem_name("From: ", ctable->from.fld);
		O42A_DEBUG("To: <0x%lx>\n", (long) ctable->to.fld);
		o42a_debug_dump_mem("Field: ", field, 3);

		O42A((kind == DK_INHERIT ? desc->inherit : desc->propagate) (ctable));

		O42A_DONE;
	}

	o42a_debug_dump_mem(
			kind == DK_MAIN ? "Main body: " : (
					kind == DK_INHERIT
					? "Inherited body: " : (
							kind == DK_COPY
							? "Copied body: "
							: "Propagated body: ")),
			to_body,
			3);

	O42A_DONE;
	O42A_RETURN;
}

static void derive_ancestor_bodies(
		o42a_obj_ctable_t *const ctable,
		int kind,
		size_t excluded) {
	O42A_ENTER(return);

	const o42a_obj_ascendant_t *ascendant =
			O42A(o42a_obj_ascendants(ctable->object_data));
	const o42a_obj_data_t *const adata = ctable->ancestor_data;
	const o42a_obj_ascendant_t *aascendant =
			O42A(o42a_obj_ascendants(adata));
	const size_t num = adata->ascendants.size - excluded;

	for (size_t i = adata->ascendants.size; i > 0; --i) {
		ctable->body_desc = ascendant->desc;
		ctable->from.body = O42A(o42a_obj_ascendant_body(aascendant));
		ctable->to.body = O42A(o42a_obj_ascendant_body(ascendant));
		O42A(derive_object_body(ctable, kind));
		++aascendant;
		++ascendant;
	}

	O42A_RETURN;
}


#ifndef NDEBUG

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
		void *start,
		o42a_obj_data_t *const data,
		o42a_dbg_type_info_t *const type_info) {
	O42A_ENTER(return);

	// Fill new object's type info (without field info yet).
	type_info->type_code = rand();
	type_info->name = "New object";

	// Fill top-level debug header.
	o42a_dbg_header_t *const header = start;

	header->type_code = type_info->type_code;
	header->enclosing = 0;
	header->name = type_info->name;
	header->type_info = type_info;

	// Fill object data debug header.
	o42a_dbg_header_t *const data_header = &data->__o42a_dbg_header__;

	O42A(o42a_dbg_fill_header(
			(const o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_data,
			data_header,
			header));
	data_header->name = "object_type";

	O42A_RETURN;
}

static inline void fill_field_infos(
		void *start,
		const o42a_obj_data_t *const data,
		o42a_dbg_type_info_t *type_info) {
	O42A_ENTER(return);

	// Fill new object's type field info.
	o42a_dbg_field_info_t *field_info = type_info->fields;

	// Fill object ascendant bodies field info.
	const o42a_obj_ascendant_t *const ascendants =
			O42A(o42a_obj_ascendants(data));
	const size_t num_ascendants = data->ascendants.size;

	for (size_t i = 0; i < num_ascendants; ++i) {

		const o42a_obj_body_t *const body =
				o42a_obj_ascendant_body(ascendants + i);

		O42A(o42a_dbg_fill_field_info(o42a_dbg_header(body), field_info++));
	}

	// Fill object data field info.
	O42A(o42a_dbg_fill_field_info(&data->__o42a_dbg_header__, field_info++));

	// Fill ascendants field info.
	for (size_t i = 0; i < num_ascendants; ++i) {
		O42A(o42a_dbg_fill_field_info(
				&ascendants[i].__o42a_dbg_header__,
				field_info++));
	}

	const size_t num_deps = data->deps.size;
	void **const deps =
			 (void **) (((char *) &data->deps) + data->deps.list);

	// Fill deps field info.
	for (size_t i = 0; i < num_deps; ++i) {
		field_info->data_type = O42A_TYPE_DATA_PTR;
		field_info->offset = ((char *) (deps + i)) - ((char *) start);
		field_info->name = "D";
		field_info->type_info = NULL;
		++field_info;
	}

	O42A_RETURN;
}

#endif /* NDEBUG */


struct obj_bodies {
	O42A_HEADER
	o42a_obj_body_t first_body;
};

static inline o42a_obj_data_t *o42a_obj_gc_data(void *const obj_data) {

	struct obj_bodies *const bodies = obj_data;

	return O42A(o42a_obj_data(&bodies->first_body));
}

static void o42a_obj_gc_marker(void *const obj_data) {
	O42A_ENTER(return);

	o42a_obj_data_t *const data = O42A(o42a_obj_gc_data(obj_data));
	const volatile o42a_val_t *const value = &data->value;
	const uint32_t flags = value->flags;

	if (flags & O42A_VAL_CONDITION) {
		data->value_type->mark(data);
	}

	uint32_t num_asc = data->ascendants.size;

	if (!num_asc) {
		O42A_RETURN;
	}

	// Mark all fields.
	o42a_obj_ascendant_t *asc = O42A(o42a_obj_ascendants(data));

	while (1) {

		o42a_obj_body_t *const body = O42A(o42a_obj_ascendant_body(asc));
		const o42a_obj_desc_t *const desc = asc->desc;

		uint32_t num_fields = desc->fields.size;

		if (num_fields) {

			o42a_obj_field_t *field = O42A(o42a_obj_fields(desc));

			while (1) {

				o42a_fld *const fld = O42A(o42a_fld_by_field(body, field));
				o42a_fld_desc_t *const desc = O42A(o42a_fld_desc(field));

				o42a_debug_mem_name("Mark field: ", fld);
				O42A(desc->mark(fld));

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

static void o42a_obj_gc_sweeper(void *const obj_data) {
	O42A_ENTER(return);

	o42a_obj_data_t *const data = O42A(o42a_obj_gc_data(obj_data));
	const volatile o42a_val_t *const value = &data->value;
	const uint32_t flags = value->flags;

	if (flags & O42A_VAL_CONDITION) {
		data->value_type->sweep(data);
	}

	o42a_debug_mem_name("Sweep object: ", (char *) data + data->start);
	uint32_t num_asc = data->ascendants.size;

	if (!num_asc) {
		O42A_RETURN;
	}

	// Mark all fields.
	o42a_obj_ascendant_t *asc = O42A(o42a_obj_ascendants(data));

	while (1) {

		o42a_obj_body_t *const body = O42A(o42a_obj_ascendant_body(asc));

		O42A(vmtc_release(body->vmtc));

		const o42a_obj_desc_t *const desc = asc->desc;
		uint32_t num_fields = desc->fields.size;

		if (num_fields) {

			o42a_obj_field_t *field = O42A(o42a_obj_fields(desc));

			while (1) {

				o42a_fld *const fld = O42A(o42a_fld_by_field(body, field));
				o42a_fld_desc_t *const desc = O42A(o42a_fld_desc(field));

				O42A(desc->sweep(fld));

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

static o42a_obj_data_t *propagate_object(
		const o42a_obj_ctr_t *const ctr,
		const o42a_obj_data_t *const adata,
		const o42a_obj_data_t *const sdata) {
	O42A_ENTER(return NULL);

	const size_t main_body_start = (size_t) (adata->object - adata->start);
	const size_t data_start = -adata->start;
	static const o42a_layout_t obj_data_layout = O42A_LAYOUT(o42a_obj_data_t);

	static const o42a_layout_t ascendant_layout =
			O42A_LAYOUT(o42a_obj_ascendant_t);
	const size_t ascendants_start = o42a_layout_pad(
			data_start + o42a_layout_size(obj_data_layout),
			ascendant_layout);
	const size_t num_ascendants = adata->ascendants.size;
	const size_t num_deps = ctr->num_deps;

	static const o42a_layout_t dep_layout = O42A_LAYOUT(void*);
	size_t deps_start = o42a_layout_pad(
			ascendants_start
			+ o42a_layout_array_size(ascendant_layout, num_ascendants),
			dep_layout);
	size_t size =
			deps_start
			+ o42a_layout_array_size(dep_layout, num_deps);

#ifndef NDEBUG

	const size_t type_field_num =
			num_ascendants + 1 + num_ascendants + num_deps;
	const size_t type_info_start =
			O42A(get_type_info_start(&size, type_field_num));

#endif

	char *const mem = O42A(o42a_gc_alloc(&o42a_obj_gc_desc, size));
	o42a_obj_t *const object = (o42a_obj_t *) (mem + main_body_start);
	o42a_obj_data_t *const data = (o42a_obj_data_t *) (mem + data_start);
	o42a_obj_ascendant_t *const ascendants =
			(o42a_obj_ascendant_t *) (mem + ascendants_start);
	void **const deps = (void **) (mem + deps_start);

#ifndef NDEBUG

	o42a_dbg_type_info_t *const type_info =
			(o42a_dbg_type_info_t *) (mem + type_info_start);

	O42A(fill_type_info(mem, data, type_info));
	type_info->field_num = type_field_num;

#endif

	// Build samples.
	O42A(copy_ancestor_ascendants(adata, ascendants, mem));

	// Fill object type and data.
	data->object = adata->object;
	data->start = adata->start;
	data->flags = O42A_OBJ_RT | (adata->flags & O42A_OBJ_INHERIT_MASK);
	data->mutex_init = 0;

	data->value_f = adata->value_f;
	data->cond_f = adata->cond_f;
	data->def_f = adata->def_f;
	if (!(ctr->value.flags & O42A_VAL_INDEFINITE)) {
		data->value = ctr->value;
		data->value.flags |= O42A_VAL_EAGER;
	} else if (adata->value.flags & O42A_VAL_EAGER) {
		data->value = adata->value;
	} else {
		data->value.flags = O42A_VAL_INDEFINITE;
	}
	data->resume_from = NULL;
	data->desc = adata->desc;
	if (adata->value_type != &o42a_val_type_void) {
		data->value_type = adata->value_type;
	} else {
		data->value_type = sdata->value_type;
	}

	data->fld_ctrs = NULL;
	data->ascendants.list =
			((char *) ascendants) - ((char *) &data->ascendants);
	data->ascendants.size = num_ascendants;
	data->deps.list =
			num_deps ? ((char *) deps) - ((char *) &data->deps) : 0;
	data->deps.size = num_deps;

	// propagate bodies
	o42a_obj_ctable_t ctable = {
		.owner_data = ctr->owner_data,
		.ancestor_data = adata,
		.sample_data = sdata,
		.object_data = data,
	};

#ifndef NDEBUG
	O42A(o42a_dbg_fill_header(
			(const o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_ctable,
			&ctable.__o42a_dbg_header__,
			NULL));
#endif /* NDEBUG */

	O42A(derive_ancestor_bodies(&ctable, DK_COPY, 0));

#ifndef NDEBUG
	O42A(fill_field_infos(mem, data, type_info));
#endif /* NDEBUG */

	o42a_debug_dump_mem("Object: ", mem, 3);

	O42A_RETURN data;
}

o42a_obj_t *o42a_obj_new(const o42a_obj_ctr_t *const ctr) {
	O42A_ENTER(return NULL);

	o42a_obj_t *ancestor = NULL;
	const o42a_obj_data_t *adata = ctr->ancestor_data;

	if (adata) {
		if (adata->flags & O42A_OBJ_VOID) {
			adata = NULL;
		} else if (adata->flags & O42A_OBJ_NONE) {
			O42A_RETURN NULL;
		} else {
			ancestor = O42A(o42a_obj_by_data(adata));
		}
	}

	const o42a_obj_data_t *const sdata = ctr->sample_data;
	const o42a_obj_desc_t *const sdesc = sdata->desc;

	if (!adata) {
		// Sample has no ancestor.
		// Propagate sample.
		o42a_debug_mem_name("No ancestor of ", sdata);

		o42a_obj_data_t *const result =
				O42A(propagate_object(ctr, sdata, sdata));

		O42A_RETURN o42a_obj_by_data(result);
	}

	const o42a_obj_ascendant_t *const consuming_ascendant =
			O42A(o42a_obj_ascendant_of_type(adata, sdesc));
	const size_t consumed_ascendants = consuming_ascendant ? 1 : 0;

	// Ancestor bodies size.
	size_t start = -adata->start;
	size_t main_body_start;

	if (consumed_ascendants) {
		main_body_start = adata->object - adata->start;
	} else {
		main_body_start =
				O42A(o42a_layout_pad(start, sdesc->main_body_layout));
		start = main_body_start + o42a_layout_size(sdesc->main_body_layout);
	}

	static const o42a_layout_t obj_data_layout = O42A_LAYOUT(o42a_obj_data_t);
	const size_t data_start = o42a_layout_pad(start, obj_data_layout);

	static const o42a_layout_t ascendant_layout =
			O42A_LAYOUT(o42a_obj_ascendant_t);
	const size_t ascendants_start = o42a_layout_pad(
			data_start + o42a_layout_size(obj_data_layout),
			ascendant_layout);
	const size_t num_ascendants =
			adata->ascendants.size + 1 - consumed_ascendants;
	const size_t num_deps = ctr->num_deps;

	static const o42a_layout_t dep_layout = O42A_LAYOUT(void*);
	size_t deps_start = o42a_layout_pad(
			ascendants_start
			+ o42a_layout_array_size(ascendant_layout, num_ascendants),
			dep_layout);
	size_t size =
			deps_start
			+ o42a_layout_array_size(dep_layout, num_deps);

#ifndef NDEBUG

	const size_t type_field_num =
			num_ascendants + 1 + num_ascendants + num_deps;
	const size_t type_info_start =
			O42A(get_type_info_start(&size, type_field_num));

#endif

	char *const mem = O42A(o42a_gc_alloc(&o42a_obj_gc_desc, size));
	o42a_obj_t *const object = (o42a_obj_t *) (mem + main_body_start);
	o42a_obj_data_t *const data = (o42a_obj_data_t *) (mem + data_start);
	o42a_obj_ascendant_t *const ascendants =
			(o42a_obj_ascendant_t *) (mem + ascendants_start);
	void **const deps = (void **) (mem + deps_start);

#ifndef NDEBUG

	// Fill new object's type info (without field info yet).
	o42a_dbg_type_info_t *type_info =
			(o42a_dbg_type_info_t*) (mem + type_info_start);

	O42A(fill_type_info(mem, data, type_info));
	type_info->field_num = type_field_num;

#endif

	// Build samples.
	O42A(copy_ancestor_ascendants(adata, ascendants, mem));

	if (!consumed_ascendants) {

		o42a_obj_ascendant_t *const main_ascendant =
				ascendants + (num_ascendants - 1);

		main_ascendant->desc = sdesc;
		main_ascendant->body = ((char *) object) - ((char *) main_ascendant);

#ifndef NDEBUG

	// Fill the main ascendant`s debug header.
	O42A(o42a_dbg_copy_header(
			o42a_dbg_header(o42a_obj_ascendant_of_type(sdata, sdesc)),
			&main_ascendant->__o42a_dbg_header__,
			(o42a_dbg_header_t*) mem));

#endif
	}

	// fill object type and data
	const int32_t sflags = sdata->flags;

	data->object = main_body_start - data_start;
	data->start = -data_start;
	data->flags = O42A_OBJ_RT | (sflags & O42A_OBJ_INHERIT_MASK);
	data->mutex_init = 0;

	data->value_f = sdata->value_f;
	data->cond_f = sdata->cond_f;
	data->def_f =
			(sflags & O42A_OBJ_ANCESTOR_DEF) ? adata->def_f : sdata->def_f;
	if (!(ctr->value.flags & O42A_VAL_INDEFINITE)) {
		data->value = ctr->value;
		data->value.flags |= O42A_VAL_EAGER;
	} else if (sflags & O42A_OBJ_ANCESTOR_DEF) {
		if (adata->value.flags & O42A_VAL_EAGER) {
			data->value = adata->value;
		} else {
			data->value.flags = O42A_VAL_INDEFINITE;
		}
	} else if (sdata->value.flags & O42A_VAL_EAGER) {
		data->value = sdata->value;
	} else {
		data->value.flags = O42A_VAL_INDEFINITE;
	}
	data->resume_from = NULL;
	data->desc = sdesc;
	if (sdata->value_type != &o42a_val_type_void) {
		data->value_type = sdata->value_type;
	} else {
		data->value_type = adata->value_type;
	}

	data->fld_ctrs = NULL;
	data->ascendants.list =
			((char *) ascendants) - ((char *) &data->ascendants);
	data->ascendants.size = num_ascendants;
	data->deps.list =
			num_deps ? ((char *) deps) - ((char *) &data->deps) : 0;
	data->deps.size = num_deps;

	// propagate sample and inherit ancestor
	o42a_obj_ctable_t ctable = {
		.owner_data = ctr->owner_data,
		.ancestor_data = adata,
		.sample_data = sdata,
		.object_data = data,
	};

#ifndef NDEBUG
	O42A(o42a_dbg_fill_header(
			(const o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_ctable,
			&ctable.__o42a_dbg_header__,
			NULL));
#endif /* NDEBUG */

	derive_ancestor_bodies(&ctable, DK_INHERIT, consumed_ascendants);

	ctable.body_desc = sdesc;
	ctable.from.body = O42A(o42a_obj_by_data(sdata));
	ctable.to.body = object;

	O42A(derive_object_body(&ctable, DK_MAIN));

#ifndef NDEBUG
	O42A(fill_field_infos(mem, data, type_info));
#endif /* NDEBUG */

	o42a_debug_dump_mem("Object: ", mem, 3);

	O42A_RETURN object;
}


void o42a_obj_def_false(
		o42a_val_t *const result,
		o42a_obj_t *const object __attribute__((unused))) {
	O42A_ENTER(return);
	result->flags = O42A_FALSE;
	O42A_RETURN;
}

void o42a_obj_value_false(
		o42a_val_t *const result,
		o42a_obj_data_t *const data __attribute__((unused)),
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


void o42a_obj_def_void(
		o42a_val_t *const result,
		o42a_obj_t *const object __attribute__((unused))) {
	O42A_ENTER(return);
	result->flags = O42A_TRUE;
	O42A_RETURN;
}

void o42a_obj_value_void(
		o42a_val_t *const result,
		o42a_obj_data_t *const data __attribute__((unused)),
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


void o42a_obj_def_unknown(
		o42a_val_t *const result __attribute__((unused)),
		o42a_obj_t *const object __attribute__((unused))) {
	O42A_ENTER(return);
	O42A_RETURN;
}

void o42a_obj_def_stub(
		o42a_val_t *const result,
		o42a_obj_t *const object __attribute__((unused))) {
	O42A_ENTER(return);
	o42a_error_print("Object value definition stub invoked");
	result->flags = O42A_FALSE;
	O42A_RETURN;
}

void o42a_obj_value_stub(
		o42a_val_t *const result,
		o42a_obj_data_t *const data __attribute__((unused)),
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

void o42a_obj_lock(o42a_obj_data_t *const data) {
	O42A_ENTER(return);

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
		if (!(data->flags & O42A_OBJ_RT)) {
			O42A(o42a_obj_use_static(data));
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

void o42a_obj_unlock(o42a_obj_data_t *const data) {
	O42A_ENTER(return);
	if (O42A(pthread_mutex_unlock(&data->mutex))) {
		o42a_error_print("Current thread does not own an object mutex");
	}
	O42A_RETURN;
}

void o42a_obj_wait(o42a_obj_data_t *const data) {
	O42A_ENTER(return);
	if (O42A(pthread_cond_wait(&data->thread_cond, &data->mutex))) {
		o42a_error_print("Current thread does not own an object mutex");
	}
	O42A_RETURN;
}

void o42a_obj_signal(o42a_obj_data_t *const data) {
	O42A_ENTER(return);
	if (O42A(pthread_cond_signal(&data->thread_cond))) {
		o42a_error_print("Failed to send signal to an object condition waiter");
	}
	O42A_RETURN;
}

void o42a_obj_broadcast(o42a_obj_data_t *const data) {
	O42A_ENTER(return);
	if (O42A(pthread_cond_broadcast(&data->thread_cond))) {
		o42a_error_print(
				"Failed to broadcast signal to an object condition waiters");
	}
	O42A_RETURN;
}

void o42a_obj_use(o42a_obj_data_t *const data) {
	O42A_ENTER(return);
	o42a_debug_mem_name("Use object: ", (char *) data + data->start);
	O42A(o42a_gc_use(o42a_gc_blockof((char *) data + data->start)));
	O42A_RETURN;
}

static inline o42a_gc_block_t *gc_block_by_object(void *const ptr) {
	O42A_ENTER(return NULL);

	o42a_obj_t *const obj = ptr;
	o42a_obj_data_t *const data = o42a_obj_data(obj);

	O42A_RETURN o42a_gc_blockof((char *) data + data->start);
}

o42a_obj_t *o42a_obj_use_mutable(o42a_obj_t **const var) {
	O42A_ENTER(return NULL);
	o42a_obj_t *const result =
			O42A(o42a_gc_use_mutable((void **) var, &gc_block_by_object));
	O42A_RETURN result;
}

void o42a_obj_use_static(o42a_obj_data_t *const data) {
	O42A_ENTER(return);

	assert(!(data->flags & O42A_OBJ_RT) && "Object is not static");
	if (!(data->flags & O42A_OBJ_RT)) {
		o42a_debug_mem_name("Static object: ", (char *) data + data->start);
		O42A(o42a_gc_static(o42a_gc_blockof((char *) data + data->start)));
	}

	O42A_RETURN;
}

void o42a_obj_start_use(
		o42a_obj_use_t *const use,
		o42a_obj_data_t *const data) {
	O42A_ENTER(return);

	assert(
			!use->data
			&& "Object use instance already utilized by another object");

	if (data->flags & O42A_OBJ_RT) {
		use->data = data;
		o42a_debug_mem_name("Start object use: ", (char *) data + data->start);
		O42A(o42a_gc_use(o42a_gc_blockof((char *) data + data->start)));
	} else {
		O42A(o42a_obj_use_static(data));
	}

	O42A_RETURN;
}

void o42a_obj_end_use(o42a_obj_use_t *const use) {
	O42A_ENTER(return);

	if (use->data) {
		o42a_debug_mem_name(
				"End object use: ",
				(char *) use->data + use->data->start);
		O42A(o42a_gc_unuse(
				o42a_gc_blockof((char *) use->data + use->data->start)));
		use->data = NULL;
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

	o42a_obj_data_t *const data = O42A(o42a_obj_data(obj));

	o42a_debug_mem_name("Start link target use: ", (char *) data + data->start);
	O42A(o42a_gc_use(o42a_gc_blockof((char *) data + data->start)));

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

	o42a_obj_data_t *const data = O42A(o42a_obj_data(obj));

	o42a_debug_mem_name("End link target use: ", (char *) data + data->start);
	O42A(o42a_gc_unuse(o42a_gc_blockof((char *) data + data->start)));

	O42A_RETURN;
}
