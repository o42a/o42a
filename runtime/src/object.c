/*
    Copyright (C) 2010-2013 Ruslan Lopatin

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


#ifndef NDEBUG

const struct _O42A_DEBUG_TYPE_o42a_obj_data _O42A_DEBUG_TYPE_o42a_obj_data = {
	.type_code = 0x042a0100,
	.field_num = 14,
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
			.offset = offsetof(o42a_obj_data_t, claim_f),
			.name = "claim_f",
		},
		{
			.data_type = O42A_TYPE_FUNC_PTR,
			.offset = offsetof(o42a_obj_data_t, proposition_f),
			.name = "proposition_f",
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
			.offset = offsetof(o42a_obj_data_t, samples),
			.name = "samples",
			.type_info = (o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_rlist,
		},
	},
};

const o42a_dbg_type_info4f_t _O42A_DEBUG_TYPE_o42a_obj_stype = {
	.type_code = 0x042a0101,
	.field_num = 4,
	.name = "o42a_obj_stype_t",
	.fields = {
		{
			.data_type = O42A_TYPE_STRUCT,
			.offset = offsetof(o42a_obj_stype_t, data),
			.name = "data",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_data,
		},
		{
			.data_type = O42A_TYPE_STRUCT,
			.offset = offsetof(o42a_obj_stype_t, fields),
			.name = "fields",
			.type_info = (o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_rlist,
		},
		{
			.data_type = O42A_TYPE_STRUCT,
			.offset = offsetof(o42a_obj_stype_t, overriders),
			.name = "overriders",
			.type_info = (o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_rlist,
		},
		{
			.data_type = O42A_TYPE_INT32,
			.offset = offsetof(o42a_obj_stype_t, main_body_layout),
			.name = "main_body_layout",
		},
	},
};

const o42a_dbg_type_info2f_t _O42A_DEBUG_TYPE_o42a_obj_rtype = {
	.type_code = 0x042a0102,
	.field_num = 2,
	.name = "o42a_obj_rtype_t",
	.fields = {
		{
			.data_type = O42A_TYPE_STRUCT,
			.offset = offsetof(o42a_obj_rtype_t, data),
			.name = "data",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_data,
		},
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_rtype_t, sample),
			.name = "sample",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_stype,
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
			.offset = offsetof(o42a_obj_ascendant_t, type),
			.name = "type",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_stype,
		},
		{
			.data_type = O42A_TYPE_REL_PTR,
			.offset = offsetof(o42a_obj_ascendant_t, body),
			.name = "body",
		},
	},
};

const o42a_dbg_type_info1f_t _O42A_DEBUG_TYPE_o42a_obj_sample = {
	.type_code = 0x042a0111,
	.field_num = 1,
	.name = "o42a_obj_sample_t",
	.fields = {
		{
			.data_type = O42A_TYPE_REL_PTR,
			.offset = offsetof(o42a_obj_sample_t, body),
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
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_stype,
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

const o42a_dbg_type_info3f_t _O42A_DEBUG_TYPE_o42a_obj_overrider = {
	.type_code = 0x042a0113,
	.field_num = 3,
	.name = "o42a_obj_overrider_t",
	.fields = {
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_overrider_t, field),
			.name = "field",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_field,
		},
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_overrider_t, defined_in),
			.name = "defined_in",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_stype,
		},
		{
			.data_type = O42A_TYPE_REL_PTR,
			.offset = offsetof(o42a_obj_overrider_t, body),
			.name = "body",
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
			.offset = offsetof(o42a_obj_ctr_t, owner_type),
			.name = "owner_type",
		},
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_ctr_t, ancestor_type),
			.name = "ancestor_type",
		},
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_ctr_t, type),
			.name = "type",
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
			.offset = offsetof(o42a_obj_ctable_t, owner_type),
			.name = "owner_type",
		},
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_ctable_t, ancestor_type),
			.name = "ancestor_type",
		},
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_ctable_t, sample_type),
			.name = "sample_type",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_stype,
		},
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_ctable_t, object_type),
			.name = "object_type",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_rtype,
		},
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_obj_ctable_t, body_type),
			.name = "body_type",
			.type_info =
					(o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_stype,
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


extern o42a_obj_type_t *o42a_obj_type(const o42a_obj_body_t *);

extern o42a_obj_body_t *o42a_obj_ancestor(const o42a_obj_body_t *);

extern o42a_obj_stype_t *o42a_obj_stype(o42a_obj_type_t *);

extern o42a_obj_t *o42a_obj_by_data(const o42a_obj_data_t *);

extern o42a_obj_ascendant_t *o42a_obj_ascendants(const o42a_obj_data_t *);

extern o42a_obj_sample_t *o42a_obj_samples(const o42a_obj_data_t *);

extern o42a_obj_field_t *o42a_obj_fields(const o42a_obj_stype_t *);

extern o42a_obj_overrider_t *o42a_obj_overriders(const o42a_obj_stype_t *);

extern o42a_obj_body_t *o42a_obj_ascendant_body(const o42a_obj_ascendant_t *);

extern o42a_obj_body_t *o42a_obj_sample_body(const o42a_obj_sample_t *);

const o42a_obj_overrider_t *o42a_obj_field_overrider(
		const o42a_obj_stype_t *const sample_type,
		const o42a_obj_field_t *const field) {
	O42A_ENTER(return NULL);

	const size_t num_overriders = sample_type->overriders.size;
	const o42a_obj_overrider_t *const overriders =
			O42A(o42a_obj_overriders(sample_type));

	for (size_t i = 0; i < num_overriders; ++i) {

		const o42a_obj_overrider_t *const overrider = overriders + i;

		if (overrider->field == field) {
			O42A_RETURN overrider;
		}
	}

	O42A_RETURN NULL;
}

const o42a_obj_ascendant_t *o42a_obj_ascendant_of_type(
		const o42a_obj_data_t *const data,
		const o42a_obj_stype_t *const type) {
	O42A_ENTER(return NULL);

	o42a_debug_mem_name("--- Data: ", data);
	o42a_debug_mem_name("--- Type: ", type);
	const o42a_obj_ascendant_t *ascendant =
			O42A(o42a_obj_ascendants(data));

	for (size_t i = data->ascendants.size; i > 0; --i) {
		if (ascendant->type == type) {
			O42A_RETURN ascendant;
		}
		++ascendant;
		continue;
	}

	O42A_RETURN NULL;
}

static inline o42a_obj_body_t *body_of_type(
		const o42a_obj_data_t *const data,
		const o42a_obj_stype_t *const type) {
	O42A_ENTER(return NULL);

	const o42a_obj_ascendant_t *const ascendant =
			O42A(o42a_obj_ascendant_of_type(data, type));

	if (!ascendant) {
		O42A_RETURN NULL;
	}

	O42A_RETURN o42a_obj_ascendant_body(ascendant);
}

o42a_obj_body_t *o42a_obj_cast(
		o42a_obj_t *const object,
		const o42a_obj_stype_t *const type) {
	O42A_ENTER(return NULL);
	O42A_DO("Cast");

	if (type->data.flags & O42A_OBJ_VOID) {
		// any body can be void
		o42a_debug_mem_name("Cast to void: ", object);
		O42A_DONE;
		O42A_RETURN object;
	}
	if (object->declared_in == type) {
		// body of the necessary type
		o42a_debug_mem_name("Cast not required: ", object);
		o42a_debug_mem_name("     to: ", type);
		O42A_DONE;
		O42A_RETURN object;
	}

	o42a_debug_mem_name("Cast of: ", object);
	o42a_debug_mem_name("     to: ", type);

	o42a_obj_body_t *const result = O42A(body_of_type(
			&o42a_obj_type(object)->type.data,
			type));

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
		ascendants->type = aascendants->type;
		ascendants->body = aascendants->body + diff;
		++aascendants;
		++ascendants;
	}

	O42A_RETURN;
}

enum derivation_kind {
	DK_COPY,
	DK_INHERIT,
	DK_PROPAGATE,
	DK_MAIN,
};

static void derive_object_body(
		o42a_obj_ctable_t *const ctable,
		o42a_obj_body_t *ancestor_body,
		enum derivation_kind kind) {
	O42A_ENTER(return);
	O42A_DO("Derive body");

	const o42a_obj_body_t *const from_body = ctable->from.body;
	o42a_obj_body_t *const to_body = ctable->to.body;

	O42A_DEBUG("Derive body %lx -> %lx\n", (long) from_body, (long) to_body);
	o42a_debug_mem_name("Body type: ", ctable->body_type);

#ifndef NDEBUG

	// Fill debug header.
	const o42a_obj_data_t *const object_data = &ctable->object_type->data;
	const o42a_dbg_header_t *const object_header =
			(o42a_dbg_header_t *) (((char *) object_data) + object_data->start);

	O42A(o42a_dbg_copy_header(
			o42a_dbg_header(from_body),
			&to_body->__o42a_dbg_header__,
			object_header));

#endif

	// Fill body header.
	to_body->object_type =
			((char *) ctable->object_type) - ((char *) to_body);
	to_body->ancestor_body =
			kind < DK_PROPAGATE
			? from_body->ancestor_body
			: ((char *) ancestor_body) - ((char *) to_body);
	to_body->declared_in = from_body->declared_in;

	uint32_t body_kind = O42A_OBJ_BODY_INHERITED;

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
	const size_t num_fields = ctable->body_type->fields.size;
	o42a_obj_field_t *const fields =
			O42A(o42a_obj_fields(ctable->body_type));

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

static void derive_ancestor_bodies(o42a_obj_ctable_t *const ctable, int kind) {
	O42A_ENTER(return);

	const o42a_obj_ascendant_t *ascendant =
			O42A(o42a_obj_ascendants(&ctable->object_type->data));
	const o42a_obj_data_t *const adata = &ctable->ancestor_type->type.data;
	const o42a_obj_ascendant_t *aascendant =
			O42A(o42a_obj_ascendants(adata));

	for (size_t i = adata->ascendants.size; i > 0; --i) {
		ctable->body_type = ascendant->type;
		ctable->from.body = O42A(o42a_obj_ascendant_body(aascendant));
		ctable->to.body = O42A(o42a_obj_ascendant_body(ascendant));
		O42A(derive_object_body(ctable, NULL, kind));
		++aascendant;
		++ascendant;
	}

	O42A_RETURN;
}

static inline void copy_samples(
		const o42a_obj_data_t *const ancestor_data,
		o42a_obj_sample_t *samples,
		void *start) {
	O42A_ENTER(return);

	void *const astart = ((char *) ancestor_data) + ancestor_data->start;
	const o42a_obj_sample_t *asamples =
			O42A(o42a_obj_samples(ancestor_data));
	const o42a_rptr_t asamples_rptr =
			((char *) asamples) - ((char *) astart);
	const o42a_rptr_t samples_rptr =
			((char *) samples) - ((char *) start);
	const o42a_rptr_t diff = asamples_rptr - samples_rptr;

	for (size_t i = ancestor_data->samples.size; i > 0; --i) {
#ifndef NDEBUG
		O42A(o42a_dbg_copy_header(
				&asamples->__o42a_dbg_header__,
				&samples->__o42a_dbg_header__,
				(o42a_dbg_header_t*) start));
#endif
		samples->body = asamples->body + diff;
		++asamples;
		++samples;
	}

	O42A_RETURN;
}


#ifndef NDEBUG

static inline size_t get_type_info_start(
		size_t *const size,
		const size_t type_field_num) {
	O42A_ENTER(return 0);

	size_t s = *size;
	const o42a_layout_t type_info_layout = O42A_LAYOUT(o42a_dbg_type_info_t);
	const o42a_layout_t field_info_layout = O42A_LAYOUT(o42a_dbg_field_info_t);
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
		o42a_obj_rtype_t *const type,
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

	// Fill (run-time) type debug header.
	o42a_dbg_header_t *const type_header = &type->__o42a_dbg_header__;

	O42A(o42a_dbg_fill_header(
			(const o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_rtype,
			type_header,
			header));
	type_header->name = "object_type";

	O42A_RETURN;
}

static inline void fill_field_infos(
		const o42a_obj_rtype_t *const type,
		o42a_dbg_type_info_t *type_info) {
	O42A_ENTER(return);

	const o42a_obj_data_t *const data = &type->data;

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

	// Fill object type field info.
	O42A(o42a_dbg_fill_field_info(&type->__o42a_dbg_header__, field_info++));

	// Fill ascendants field info.
	for (size_t i = 0; i < num_ascendants; ++i) {
		O42A(o42a_dbg_fill_field_info(
				&ascendants[i].__o42a_dbg_header__,
				field_info++));
	}

	// Fill samples field info.
	const o42a_obj_sample_t *samples = O42A(o42a_obj_samples(data));
	const size_t num_samples = data->samples.size;

	for (size_t i = 0; i < num_ascendants; ++i) {
		O42A(o42a_dbg_fill_field_info(
				&samples[i].__o42a_dbg_header__,
				field_info++));
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

	return &O42A(o42a_obj_type(&bodies->first_body))->type.data;
}

static void o42a_obj_gc_marker(void *const obj_data) {
	O42A_ENTER(return);

	o42a_obj_data_t *const data = O42A(o42a_obj_gc_data(obj_data));
	uint32_t num_asc = data->ascendants.size;

	if (!num_asc) {
		O42A_RETURN;
	}

	// Mark all fields.
	o42a_obj_ascendant_t *asc = O42A(o42a_obj_ascendants(data));

	while (1) {

		o42a_obj_body_t *const body = O42A(o42a_obj_ascendant_body(asc));
		o42a_obj_stype_t *const type = asc->type;

		uint32_t num_fields = type->fields.size;

		if (num_fields) {

			o42a_obj_field_t *field = O42A(o42a_obj_fields(type));

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

	o42a_debug_mem_name("Sweep object: ", (char *) data + data->start);
	uint32_t num_asc = data->ascendants.size;

	if (!num_asc) {
		O42A_RETURN;
	}

	// Mark all fields.
	o42a_obj_ascendant_t *asc = O42A(o42a_obj_ascendants(data));

	while (1) {

		o42a_obj_body_t *const body = O42A(o42a_obj_ascendant_body(asc));
		o42a_obj_stype_t *const type = asc->type;

		uint32_t num_fields = type->fields.size;

		if (num_fields) {

			o42a_obj_field_t *field = O42A(o42a_obj_fields(type));

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

static o42a_obj_rtype_t *propagate_object(
		const o42a_obj_ctr_t *const ctr,
		o42a_obj_type_t *const atype,
		o42a_obj_stype_t *const sstype) {
	O42A_ENTER(return NULL);

	const o42a_obj_data_t *const adata = &atype->type.data;
	const size_t main_body_start = (size_t) (adata->object - adata->start);
	const size_t data_start = -adata->start;
	const size_t type_start = data_start - offsetof(o42a_obj_rtype_t, data);
	const o42a_layout_t obj_rtype_layout = O42A_LAYOUT(o42a_obj_rtype_t);

	const o42a_layout_t ascendant_layout = O42A_LAYOUT(o42a_obj_ascendant_t);
	const size_t ascendants_start = o42a_layout_pad(
			type_start + o42a_layout_size(obj_rtype_layout),
			ascendant_layout);
	const size_t num_ascendants = adata->ascendants.size;
	const size_t num_samples = adata->samples.size;

	const o42a_layout_t sample_layout = O42A_LAYOUT(o42a_obj_sample_t);
	const size_t samples_start = o42a_layout_pad(
			ascendants_start
			+ o42a_layout_array_size(ascendant_layout, num_ascendants),
			sample_layout);

	size_t size =
			samples_start + o42a_layout_array_size(sample_layout, num_samples);

#ifndef NDEBUG

	const size_t type_field_num =
			num_ascendants + 1 + num_ascendants + num_samples;
	const size_t type_info_start =
			O42A(get_type_info_start(&size, type_field_num));

#endif

	char *const mem = O42A(o42a_gc_alloc(&o42a_obj_gc_desc, size));
	o42a_obj_t *const object = (o42a_obj_t *) (mem + main_body_start);
	o42a_obj_rtype_t *const type = (o42a_obj_rtype_t *) (mem + type_start);
	o42a_obj_data_t *const data = &type->data;
	o42a_obj_ascendant_t *const ascendants =
			(o42a_obj_ascendant_t *) (mem + ascendants_start);
	o42a_obj_sample_t *const samples =
			(o42a_obj_sample_t *) (mem + samples_start);

#ifndef NDEBUG

	o42a_dbg_type_info_t *const type_info =
			(o42a_dbg_type_info_t *) (mem + type_info_start);

	O42A(fill_type_info(mem, type, type_info));
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
	data->claim_f = adata->claim_f;
	data->proposition_f = adata->proposition_f;
	if (adata->value_type != &o42a_val_type_void) {
		data->value_type = adata->value_type;
	} else {
		data->value_type = sstype->data.value_type;
	}

	data->fld_ctrs = NULL;
	data->ascendants.list =
			((char *) ascendants) - ((char *) &data->ascendants);
	data->ascendants.size = num_ascendants;

	data->samples.list =
			num_samples
			? ((char *) samples) - ((char *) &data->samples)
			: 0;
	data->samples.size = num_samples;

	type->sample = O42A(o42a_obj_stype(atype));

	// propagate bodies
	o42a_obj_ctable_t ctable = {
		.owner_type = ctr->owner_type,
		.ancestor_type = atype,
		.sample_type = sstype,
		.object_type = type,
	};

#ifndef NDEBUG
	O42A(o42a_dbg_fill_header(
			(const o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_ctable,
			&ctable.__o42a_dbg_header__,
			NULL));
#endif /* NDEBUG */

	O42A(derive_ancestor_bodies(&ctable, DK_COPY));
	O42A(copy_samples(&atype->type.data, samples, mem));

#ifndef NDEBUG
	O42A(fill_field_infos(type, type_info));
#endif /* NDEBUG */

	o42a_debug_dump_mem("Object: ", mem, 3);

	O42A_RETURN type;
}

typedef struct sample_data {
	o42a_obj_sample_t *sample;
	o42a_obj_body_t *old_body;
	size_t new_body;
} sample_data_t;

static inline size_t fill_sample_data(
		size_t *start,
		const o42a_obj_data_t *const adata,
		const o42a_obj_data_t *const sdata,
		sample_data_t *sample_data) {
	O42A_ENTER(return 0);

	size_t num_samples = 0;
	o42a_obj_sample_t *old_sample = O42A(o42a_obj_samples(sdata));

	for (size_t i = sdata->samples.size; i > 0; --i) {

		o42a_obj_body_t *const old_body =
				O42A(o42a_obj_sample_body(old_sample));
		const o42a_obj_stype_t *const body_type = old_body->declared_in;

		sample_data->sample = old_sample;
		if (O42A(o42a_obj_ascendant_of_type(adata, body_type))) {
			// Sample body already present in ancestor.
			sample_data->old_body = NULL;
		} else {
			sample_data->old_body = old_body;

			const o42a_layout_t layout = body_type->main_body_layout;
			const size_t new_body = o42a_layout_pad(*start, layout);

			sample_data->new_body = new_body;
			*start = new_body + o42a_layout_size(layout);
			++num_samples;
		}
		++sample_data;
		++old_sample;
	}

	O42A_DEBUG("Number of samples: %zi\n", num_samples);

	O42A_RETURN num_samples;
}

static inline void propagate_samples(
		o42a_obj_ctable_t *const ctable,
		char *const mem,
		o42a_obj_body_t *const ancestor_body,
		sample_data_t *const sample_data) {
	O42A_ENTER(return);

	const size_t num_ancestors =
			ctable->ancestor_type->type.data.ascendants.size;
	const o42a_obj_data_t *const data = &ctable->object_type->data;
	o42a_obj_ascendant_t *ascendant =
			o42a_obj_ascendants(data) + num_ancestors;
	o42a_obj_sample_t *sample = O42A(o42a_obj_samples(data));
	size_t sample_idx = 0;

	for (size_t i = data->samples.size; i > 0;) {

		sample_data_t *const sd = sample_data + sample_idx;
		o42a_obj_body_t *const old_body = sd->old_body;

		if (!old_body) {
			// Body already present among ancestors.
			++sample_idx;
			continue;
		}
		--i;

		o42a_obj_body_t *const new_body =
				(o42a_obj_body_t *) (mem + sd->new_body);

		sample->body = ((char *) new_body) - ((char *) sample);
		ascendant->type = old_body->declared_in;
		ascendant->body = ((char *) new_body) - ((char *) ascendant);

#ifndef NDEBUG

		O42A_DEBUG("Sample: <0x%lx>\n", (long) sample);
		// Copy sample header.
		O42A(o42a_dbg_copy_header(
				o42a_dbg_header(sample_data->sample),
				&sample->__o42a_dbg_header__,
				(o42a_dbg_header_t*) mem));

		// Copy ascendant header.
		O42A(o42a_dbg_copy_header(
				o42a_dbg_header(o42a_obj_ascendant_of_type(
						&ctable->sample_type->data,
						old_body->declared_in)),
				&ascendant->__o42a_dbg_header__,
				(o42a_dbg_header_t*) mem));

#endif

		ctable->body_type = old_body->declared_in;
		ctable->from.body = old_body;
		ctable->to.body = new_body;

		O42A(derive_object_body(ctable, ancestor_body, DK_PROPAGATE));

		++sample_idx;
		++sample;
		++ascendant;
	}

	O42A_RETURN;
}

o42a_obj_t *o42a_obj_new(const o42a_obj_ctr_t *const ctr) {
	O42A_ENTER(return NULL);

	o42a_obj_t *ancestor = NULL;
	o42a_obj_type_t *atype = ctr->ancestor_type;

	if (atype) {
		if (atype->type.data.flags & O42A_OBJ_VOID) {
			atype = NULL;
		} else {
			ancestor = O42A(o42a_obj_by_data(&ctr->ancestor_type->type.data));
		}
	}

	const o42a_obj_data_t *const adata = atype ? &atype->type.data : NULL;

	if (adata && (adata->flags & O42A_OBJ_NONE)) {
		O42A_RETURN NULL;
	}

	o42a_obj_type_t *const stype = ctr->type;
	o42a_obj_stype_t *const sstype = O42A(o42a_obj_stype(stype));

	if (!atype) {
		// Sample has no ancestor.
		// Propagate sample.
		o42a_debug_mem_name("No ancestor of ", stype);

		o42a_obj_rtype_t *const result =
				O42A(propagate_object(ctr, stype, sstype));

		O42A_RETURN o42a_obj_by_data(&result->data);
	}

	const o42a_obj_ascendant_t *const consuming_ascendant =
			O42A(o42a_obj_ascendant_of_type(adata, sstype));

	if (consuming_ascendant) {
		// Ancestor has a body of the same type as object.
		// Propagate ancestor.
		o42a_debug_mem_name("Sample consumed by ", consuming_ascendant);

		o42a_obj_rtype_t *const result =
				O42A(propagate_object(ctr, atype, sstype));

		// obtain consuming ascendant from result type
		const o42a_obj_ascendant_t *const a_ascendants =
				O42A(o42a_obj_ascendants(adata));
		const o42a_obj_ascendant_t *const res_ascendants =
				O42A(o42a_obj_ascendants(&result->data));
		const o42a_obj_ascendant_t *const res_consuming_ascendant =
				res_ascendants + (consuming_ascendant - a_ascendants);

		O42A_RETURN o42a_obj_ascendant_body(res_consuming_ascendant);
	}

	// Ancestor bodies size.
	size_t start = offsetof(o42a_obj_stype_t, data) - adata->start;

	const o42a_obj_data_t *const sdata = &stype->type.data;
	sample_data_t sample_data[sdata->samples.size];
	const size_t num_samples =
			O42A(fill_sample_data(&start, adata, sdata, sample_data));

	const size_t main_body_start =
			O42A(o42a_layout_pad(start, sstype->main_body_layout));

	start = main_body_start + o42a_layout_size(sstype->main_body_layout);

	const o42a_layout_t obj_rtype_layout = O42A_LAYOUT(o42a_obj_rtype_t);
	const size_t type_start =
			o42a_layout_pad(start, obj_rtype_layout);

	const o42a_layout_t ascendant_layout = O42A_LAYOUT(o42a_obj_ascendant_t);
	const size_t ascendants_start = o42a_layout_pad(
			type_start + o42a_layout_size(obj_rtype_layout),
			ascendant_layout);
	const size_t num_ascendants = adata->ascendants.size + num_samples + 1;

	const o42a_layout_t sample_layout = O42A_LAYOUT(o42a_obj_sample_t);
	const size_t samples_start = o42a_layout_pad(
			ascendants_start
			+ o42a_layout_array_size(ascendant_layout, num_ascendants),
			sample_layout);

	size_t size =
			samples_start + o42a_layout_array_size(sample_layout, num_samples);

#ifndef NDEBUG

	const size_t type_field_num =
			num_ascendants + 1 + num_ascendants + num_samples;
	const size_t type_info_start =
			O42A(get_type_info_start(&size, type_field_num));

#endif

	char *const mem = O42A(o42a_gc_alloc(&o42a_obj_gc_desc, size));
	o42a_obj_t *const object = (o42a_obj_t *) (mem + main_body_start);
	o42a_obj_rtype_t *const type = (o42a_obj_rtype_t *) (mem + type_start);
	o42a_obj_data_t *const data = &type->data;
	o42a_obj_ascendant_t *const ascendants =
			(o42a_obj_ascendant_t *) (mem + ascendants_start);

#ifndef NDEBUG

	// Fill new object's type info (without field info yet).
	o42a_dbg_type_info_t *type_info =
			(o42a_dbg_type_info_t*) (mem + type_info_start);

	O42A(fill_type_info(mem, type, type_info));
	type_info->field_num = type_field_num;

#endif

	// Build samples.
	O42A(copy_ancestor_ascendants(adata, ascendants, mem));

	o42a_obj_ascendant_t *const main_ascendant =
			ascendants + (num_ascendants - 1);

	main_ascendant->type = sstype;
	main_ascendant->body = ((char *) object) - ((char *) main_ascendant);

#ifndef NDEBUG

	// Fill the main ascendant`s debug header.
	O42A(o42a_dbg_copy_header(
			o42a_dbg_header(o42a_obj_ascendant_of_type(
					&stype->type.data,
					sstype)),
			&main_ascendant->__o42a_dbg_header__,
			(o42a_dbg_header_t*) mem));

#endif

	// fill object type and data
	const size_t data_start = type_start + offsetof(o42a_obj_rtype_t, data);

	data->object = main_body_start - data_start;
	data->start = -data_start;
	data->flags = O42A_OBJ_RT | (sdata->flags & O42A_OBJ_INHERIT_MASK);
	data->mutex_init = 0;

	data->value_f = sdata->value_f;
	data->cond_f = sdata->cond_f;
	data->claim_f = sdata->claim_f;
	data->proposition_f = sdata->proposition_f;
	if (sdata->value_type != &o42a_val_type_void) {
		data->value_type = sdata->value_type;
	} else {
		data->value_type = adata->value_type;
	}

	data->fld_ctrs = NULL;
	data->ascendants.list =
			((char *) ascendants) - ((char *) &data->ascendants);
	data->ascendants.size = num_ascendants;

	data->samples.list =
			num_samples
			? (mem + samples_start) - ((char *) &data->samples)
			: 0;
	data->samples.size = num_samples;

	type->sample = sstype;

	// propagate sample and inherit ancestor
	o42a_obj_ctable_t ctable = {
		.owner_type = ctr->owner_type,
		.ancestor_type = atype,
		.sample_type = sstype,
		.object_type = type,
	};

#ifndef NDEBUG
	O42A(o42a_dbg_fill_header(
			(const o42a_dbg_type_info_t *) &_O42A_DEBUG_TYPE_o42a_obj_ctable,
			&ctable.__o42a_dbg_header__,
			NULL));
#endif /* NDEBUG */

	o42a_obj_body_t *const ancestor_body =
			(o42a_obj_body_t*) (mem + (adata->object - adata->start));

	derive_ancestor_bodies(&ctable, DK_INHERIT);
	O42A(propagate_samples(&ctable, mem, ancestor_body, sample_data));

	ctable.body_type = sstype;
	ctable.from.body = O42A(o42a_obj_by_data(sdata));
	ctable.to.body = object;

	O42A(derive_object_body(&ctable, ancestor_body, DK_MAIN));

#ifndef NDEBUG
	O42A(fill_field_infos(type, type_info));
#endif /* NDEBUG */

	o42a_debug_dump_mem("Object: ", mem, 3);

	O42A_RETURN object;
}


void o42a_obj_val_false(o42a_val_t *const result, o42a_obj_t *const object) {
	O42A_ENTER(return);
	result->flags = O42A_FALSE;
	O42A_RETURN;
}

void o42a_obj_value_false(
		o42a_val_t *const result,
		o42a_obj_data_t *const data,
		o42a_obj_t *const object) {
	O42A_ENTER(return);
	result->flags = O42A_FALSE;
	O42A_RETURN;
}

o42a_bool_t o42a_obj_cond_false(o42a_obj_t *const object) {
	O42A_ENTER(return O42A_FALSE);
	O42A_RETURN O42A_FALSE;
}


void o42a_obj_val_void(o42a_val_t *const result, o42a_obj_t *const object) {
	O42A_ENTER(return);
	result->flags = O42A_TRUE;
	O42A_RETURN;
}

void o42a_obj_value_void(
		o42a_val_t *const result,
		o42a_obj_data_t *const data,
		o42a_obj_t *const object) {
	O42A_ENTER(return);
	result->flags = O42A_TRUE;
	O42A_RETURN;
}

o42a_bool_t o42a_obj_cond_true(o42a_obj_t *const object) {
	O42A_ENTER(return O42A_FALSE);
	O42A_RETURN O42A_TRUE;
}


void o42a_obj_val_unknown(o42a_val_t *const result, o42a_obj_t *const object) {
	O42A_ENTER(return);
	O42A_RETURN;
}

void o42a_obj_val_stub(o42a_val_t *const result, o42a_obj_t *const object) {
	O42A_ENTER(return);
	o42a_error_print("Object value part stub invoked");
	result->flags = O42A_FALSE;
	O42A_RETURN;
}

void o42a_obj_value_stub(
		o42a_val_t *const result,
		o42a_obj_data_t *const data,
		o42a_obj_t *const object) {
	O42A_ENTER(return);
	o42a_error_print("Object value stub invoked");
	result->flags = O42A_FALSE;
	O42A_RETURN;
}

o42a_bool_t o42a_obj_cond_stub(o42a_obj_t *const object) {
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
	o42a_obj_type_t *const type = o42a_obj_type(obj);
	o42a_obj_data_t *const data = &type->type.data;

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

	o42a_obj_data_t *const data = &O42A(o42a_obj_type(obj))->type.data;

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

	o42a_obj_data_t *const data = &O42A(o42a_obj_type(obj))->type.data;

	o42a_debug_mem_name("End link target use: ", (char *) data + data->start);
	O42A(o42a_gc_unuse(o42a_gc_blockof((char *) data + data->start)));

	O42A_RETURN;
}
