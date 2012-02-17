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
#include "o42a/object.h"

#include "o42a/error.h"
#include "o42a/field.h"
#include "o42a/memory.h"

#include <assert.h>
#include <stdlib.h>


inline o42a_obj_type_t *o42a_obj_type(
		O42A_PARAMS
		const o42a_obj_body_t *const body) {
	return (o42a_obj_type_t*) (((void*) body) + body->object_type);
}

inline o42a_obj_body_t *o42a_obj_ancestor(
		O42A_PARAMS
		const o42a_obj_body_t *const body) {
	return (o42a_obj_body_t*) (((void*) body) + body->ancestor_body);
}

inline o42a_obj_stype_t *o42a_obj_stype(
		O42A_PARAMS
		o42a_obj_type_t *const type) {
	return (type->type.data.flags & O42A_OBJ_RT)
			? type->rtype.sample : &type->stype;
}

inline o42a_obj_t *o42a_obj_by_data(
		O42A_PARAMS
		const o42a_obj_data_t *const data) {
	return (o42a_obj_t*) (((void*) data) + data->object);
}

inline o42a_obj_ascendant_t *o42a_obj_ascendants(
		O42A_PARAMS
		const o42a_obj_data_t *const data) {

	const o42a_rlist_t *const list = &data->ascendants;

	return (o42a_obj_ascendant_t*) (((void*) list) + list->list);
}

inline o42a_obj_sample_t *o42a_obj_samples(
		O42A_PARAMS
		const o42a_obj_data_t *const data) {

	const o42a_rlist_t *const list = &data->samples;

	return (o42a_obj_sample_t*) (((void*) list) + list->list);
}

inline o42a_obj_field_t* o42a_obj_fields(
		O42A_PARAMS
		const o42a_obj_stype_t *const type) {

	const o42a_rlist_t *const list = &type->fields;

	return (o42a_obj_field_t*) (((void*) list) + list->list);
}

inline o42a_obj_overrider_t* o42a_obj_overriders(
		O42A_PARAMS
		const o42a_obj_stype_t *const type) {

	const o42a_rlist_t *const list = &type->overriders;

	return (o42a_obj_overrider_t*) (((void*) list) + list->list);
}

inline o42a_obj_body_t *o42a_obj_ascendant_body(
		O42A_PARAMS
		const o42a_obj_ascendant_t *const ascendant) {
	return (o42a_obj_body_t *) (((void*) ascendant) + ascendant->body);
}

inline o42a_obj_body_t *o42a_obj_sample_body(
		O42A_PARAMS
		const o42a_obj_sample_t *const sample) {
	return (o42a_obj_body_t *) (((void*) sample) + sample->body);
}

const o42a_obj_overrider_t *o42a_obj_field_overrider(
		O42A_PARAMS
		const o42a_obj_stype_t *const sample_type,
		const o42a_obj_field_t *const field) {
	O42A_ENTER(return NULL);

	const size_t num_overriders = sample_type->overriders.size;
	const o42a_obj_overrider_t *const overriders =
			O42A(o42a_obj_overriders(O42A_ARGS sample_type));

	// TODO perform a binary search for overrider
	for (size_t i = 0; i < num_overriders; ++i) {

		const o42a_obj_overrider_t *const overrider = overriders + i;

		if (overrider->field == field) {
			O42A_RETURN overrider;
		}
	}

	O42A_RETURN NULL;
}

const o42a_obj_ascendant_t *o42a_obj_ascendant_of_type(
		O42A_PARAMS
		const o42a_obj_data_t *const data,
		const o42a_obj_stype_t *const type) {
	O42A_ENTER(return NULL);

	o42a_debug_mem_name("--- Data: ", data);
	o42a_debug_mem_name("--- Type: ", type);
	const o42a_obj_ascendant_t *ascendant =
			O42A(o42a_obj_ascendants(O42A_ARGS data));

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
		O42A_PARAMS
		const o42a_obj_data_t *const data,
		const o42a_obj_stype_t *const type) {
	O42A_ENTER(return NULL);

	const o42a_obj_ascendant_t *const ascendant =
			O42A(o42a_obj_ascendant_of_type(O42A_ARGS data, type));

	if (!ascendant) {
		O42A_RETURN NULL;
	}

	O42A_RETURN o42a_obj_ascendant_body(O42A_ARGS ascendant);
}

o42a_obj_body_t *o42a_obj_cast(
		O42A_PARAMS
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
	if (object->methods->object_type == type) {
		// body of the necessary type
		o42a_debug_mem_name("Cast not required: ", object);
		o42a_debug_mem_name("     to: ", type);
		O42A_DONE;
		O42A_RETURN object;
	}

	o42a_debug_mem_name("Cast of: ", object);
	o42a_debug_mem_name("     to: ", type);

	o42a_obj_body_t *const result = O42A(body_of_type(
			O42A_ARGS
			&o42a_obj_type(O42A_ARGS object)->type.data,
			type));

	o42a_debug_mem_name("Cast result: ", result);

	O42A_DONE;
	O42A_RETURN result;
}


o42a_obj_body_t *o42a_obj_cast_or_error(
		O42A_PARAMS
		o42a_obj_t *const object,
		const o42a_obj_stype_t *const type) {
	O42A_ENTER(return NULL);

	o42a_obj_body_t *const result = O42A(o42a_obj_cast(O42A_ARGS object, type));

	if (result) {
		O42A_RETURN result;
	}

	o42a_error_print(O42A_ARGS "Cast failure");

	O42A_RETURN o42a_obj_by_data(O42A_ARGS &o42a.false_type->data);
}

static inline void copy_ancestor_ascendants(
		O42A_PARAMS
		const o42a_obj_data_t *const ancestor_data,
		o42a_obj_ascendant_t *ascendants,
		void *start) {
	O42A_ENTER(return);

	void* astart = ((void*) ancestor_data) + ancestor_data->start;
	const o42a_obj_ascendant_t *aascendants =
			O42A(o42a_obj_ascendants(O42A_ARGS ancestor_data));
	const o42a_rptr_t aascendants_rptr = ((void*) aascendants) - astart;
	const o42a_rptr_t ascendants_rptr = ((void*) ascendants) - start;
	const o42a_rptr_t diff = aascendants_rptr - ascendants_rptr;

	for (size_t i = ancestor_data->ascendants.size; i > 0; --i) {
#ifndef NDEBUG
		O42A(o42a_dbg_copy_header(
				O42A_ARGS
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
		O42A_PARAMS
		o42a_obj_ctable_t *const ctable,
		o42a_obj_body_t *ancestor_body,
		int kind) {
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
			(o42a_dbg_header_t*) (((void*) object_data) + object_data->start);

	O42A(o42a_dbg_copy_header(
			O42A_ARGS
			o42a_dbg_header(from_body),
			&to_body->__o42a_dbg_header__,
			object_header));

#endif

	// Fill body header.
	to_body->object_type =
			((void*) ctable->object_type) - ((void*) to_body);
	to_body->ancestor_body =
			kind < DK_PROPAGATE
			? from_body->ancestor_body
			: ((void*) ancestor_body) - ((void*) to_body);
	to_body->methods = from_body->methods;

	uint32_t body_kind = O42A_OBJ_BODY_INHERITED;

	if (kind != DK_INHERIT) {
		// keep the kind of body when propagating field
		to_body->flags = from_body->flags;
	} else {
		// drop kind of body to "inherited"
		to_body->flags =
				(from_body->flags & ~O42A_OBJ_BODY_TYPE)
				| O42A_OBJ_BODY_INHERITED;
	}

	// Derive fields.
	const size_t num_fields = ctable->body_type->fields.size;
	o42a_obj_field_t *const fields =
			O42A(o42a_obj_fields(O42A_ARGS ctable->body_type));

	for (size_t i = 0; i < num_fields; ++i) {

		o42a_obj_field_t *const field = fields + i;

		ctable->field = field;
		ctable->from.fld = O42A(o42a_fld_by_field(O42A_ARGS from_body, field));
		ctable->to.fld = O42A(o42a_fld_by_field(O42A_ARGS to_body, field));

		const o42a_fld_desc_t *const desc =
				O42A(o42a_fld_desc(O42A_ARGS field));

		O42A_DO(kind == DK_INHERIT ? "Inherit field" : "Propagate field");
		o42a_debug_mem_name("From: ", ctable->from.fld);
		O42A_DEBUG("To: <0x%lx>\n", (long) ctable->to.fld);
		o42a_debug_dump_mem("Field: ", field, 3);

		O42A((kind == DK_INHERIT ? desc->inherit : desc->propagate) (
				O42A_ARGS
				ctable));

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
		O42A_PARAMS
		o42a_obj_ctable_t *const ctable,
		int kind) {
	O42A_ENTER(return);

	const o42a_obj_ascendant_t *ascendant =
			O42A(o42a_obj_ascendants(O42A_ARGS &ctable->object_type->data));
	const o42a_obj_data_t *const adata = &ctable->ancestor_type->type.data;
	const o42a_obj_ascendant_t *aascendant =
			O42A(o42a_obj_ascendants(O42A_ARGS adata));

	for (size_t i = adata->ascendants.size; i > 0; --i) {
		ctable->body_type = ascendant->type;
		ctable->from.body = O42A(o42a_obj_ascendant_body(O42A_ARGS aascendant));
		ctable->to.body = O42A(o42a_obj_ascendant_body(O42A_ARGS ascendant));
		O42A(derive_object_body(O42A_ARGS ctable, NULL, kind));
		++aascendant;
		++ascendant;
	}

	O42A_RETURN;
}

static inline void copy_samples(
		O42A_PARAMS
		const o42a_obj_data_t *const ancestor_data,
		o42a_obj_sample_t *samples,
		void *start) {
	O42A_ENTER(return);

	void* astart = ((void*) ancestor_data) + ancestor_data->start;
	const o42a_obj_sample_t *asamples =
			O42A(o42a_obj_samples(O42A_ARGS ancestor_data));
	const o42a_rptr_t asamples_rptr = ((void*) asamples) - astart;
	const o42a_rptr_t samples_rptr = ((void*) samples) - start;
	const o42a_rptr_t diff = asamples_rptr - samples_rptr;

	for (size_t i = ancestor_data->samples.size; i > 0; --i) {
#ifndef NDEBUG
		O42A(o42a_dbg_copy_header(
				O42A_ARGS
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
		O42A_PARAMS
		size_t *const size,
		const size_t type_field_num) {
	O42A_ENTER(return 0);

	size_t s = *size;
	const o42a_layout_t type_info_layout = O42A_LAYOUT(o42a_dbg_type_info_t);
	const o42a_layout_t field_info_layout = O42A_LAYOUT(o42a_dbg_field_info_t);
	const size_t type_info_start = s =
			O42A(o42a_layout_pad(O42A_ARGS s, type_info_layout));

	s += O42A(o42a_layout_size(O42A_ARGS type_info_layout));
	s += O42A(o42a_layout_pad(O42A_ARGS s, field_info_layout));
	s += O42A(o42a_layout_array_size(
			O42A_ARGS
			field_info_layout,
			type_field_num));

	*size = s;

	O42A_RETURN type_info_start;
}

static inline void fill_type_info(
		O42A_PARAMS
		void *start,
		o42a_obj_rtype_t *const type,
		o42a_dbg_type_info_t *const type_info) {
	O42A_ENTER(return);

	// Fill new object's type info (without field info yet).
	type_info->type_code = rand();
	type_info->name = "New object";

	// Fill top-level debug header.
	o42a_dbg_header_t *header = (o42a_dbg_header_t*) start;

	header->type_code = type_info->type_code;
	header->enclosing = 0;
	header->name = type_info->name;
	header->type_info = type_info;

	// Fill (run-time) type debug header.
	o42a_dbg_header_t *const type_header = &type->__o42a_dbg_header__;

	O42A(o42a_dbg_fill_header(
			O42A_ARGS
			o42a_dbg.rtype_type_info,
			type_header,
			header));
	type_header->name = "object_type";

	O42A_RETURN;
}

static inline void fill_field_infos(
		O42A_PARAMS
		const o42a_obj_rtype_t *const type,
		o42a_dbg_type_info_t *type_info) {
	O42A_ENTER(return);

	const o42a_obj_data_t *const data = &type->data;

	// Fill new object's type field info.
	o42a_dbg_field_info_t *field_info = type_info->fields;

	// Fill object ascendant bodies field info.
	const o42a_obj_ascendant_t *const ascendants =
			O42A(o42a_obj_ascendants(O42A_ARGS data));
	const size_t num_ascendants = data->ascendants.size;

	for (size_t i = 0; i < num_ascendants; ++i) {

		const o42a_obj_body_t *const body = o42a_obj_ascendant_body(
				O42A_ARGS
				ascendants + i);

		O42A(o42a_dbg_fill_field_info(
				O42A_ARGS
				o42a_dbg_header(body),
				field_info++));
	}

	// Fill object type field info.
	O42A(o42a_dbg_fill_field_info(
			O42A_ARGS
			&type->__o42a_dbg_header__,
			field_info++));

	// Fill ascendants field info.
	for (size_t i = 0; i < num_ascendants; ++i) {
		O42A(o42a_dbg_fill_field_info(
				O42A_ARGS
				&ascendants[i].__o42a_dbg_header__,
				field_info++));
	}

	// Fill samples field info.
	const o42a_obj_sample_t *samples = O42A(o42a_obj_samples(O42A_ARGS data));
	const size_t num_samples = data->samples.size;

	for (size_t i = 0; i < num_ascendants; ++i) {
		O42A(o42a_dbg_fill_field_info(
				O42A_ARGS
				&samples[i].__o42a_dbg_header__,
				field_info++));
	}

	O42A_RETURN;
}

#endif /* NDEBUG */


static o42a_obj_rtype_t *propagate_object(
		O42A_PARAMS
		const o42a_obj_ctr_t *const ctr,
		o42a_obj_type_t *const atype,
		o42a_obj_stype_t *const sstype,
		char inherit) {
	O42A_ENTER(return NULL);

	const o42a_obj_data_t *const adata = &atype->type.data;
	const size_t main_body_start = (size_t) (adata->object - adata->start);
	const size_t data_start = -adata->start;
	const size_t type_start = data_start - offsetof(o42a_obj_rtype_t, data);
	const o42a_layout_t obj_rtype_layout = O42A_LAYOUT(o42a_obj_rtype_t);

	const o42a_layout_t ascendant_layout = O42A_LAYOUT(o42a_obj_ascendant_t);
	const size_t ascendants_start = o42a_layout_pad(
			O42A_ARGS
			type_start + o42a_layout_size(O42A_ARGS obj_rtype_layout),
			ascendant_layout);
	const size_t num_ascendants = adata->ascendants.size;
	const size_t num_samples = adata->samples.size;

	const o42a_layout_t sample_layout = O42A_LAYOUT(o42a_obj_sample_t);
	const size_t samples_start =
			o42a_layout_pad(
					O42A_ARGS
					ascendants_start + o42a_layout_array_size(
							O42A_ARGS ascendant_layout,
							num_ascendants),
			sample_layout);

	size_t size = samples_start + o42a_layout_array_size(
			O42A_ARGS
			sample_layout,
			num_samples);

#ifndef NDEBUG

	const size_t type_field_num =
			num_ascendants + 1 + num_ascendants + num_samples;
	const size_t type_info_start =
			O42A(get_type_info_start(O42A_ARGS &size, type_field_num));

#endif

	void *const mem = O42A(o42a_mem_alloc_gc(O42A_ARGS size));
	o42a_obj_t *const object = (o42a_obj_t*) (mem + main_body_start);
	o42a_obj_rtype_t *const type = (o42a_obj_rtype_t*) (mem + type_start);
	o42a_obj_data_t *const data = &type->data;
	o42a_obj_ascendant_t *const ascendants =
			(o42a_obj_ascendant_t*) (mem + ascendants_start);
	o42a_obj_sample_t *const samples =
			(o42a_obj_sample_t*) (mem + samples_start);

#ifndef NDEBUG

	o42a_dbg_type_info_t *const type_info =
			(o42a_dbg_type_info_t*) (mem + type_info_start);

	O42A(fill_type_info(O42A_ARGS mem, type, type_info));
	type_info->field_num = type_field_num;

#endif

	// Build samples.
	O42A(copy_ancestor_ascendants(O42A_ARGS adata, ascendants, mem));

	// Fill object type and data.
	data->object = adata->object;
	data->flags = O42A_OBJ_RT | (adata->flags & O42A_OBJ_INHERIT_MASK);
	data->start = adata->start;

	data->value.flags = O42A_UNKNOWN | O42A_INDEFINITE;
	data->value_f = adata->value_f;
	data->requirement_f = adata->requirement_f;
	data->claim_f = adata->claim_f;
	data->condition_f = adata->condition_f;
	data->proposition_f = adata->proposition_f;

	data->owner_type = ctr->scope_type;
	data->ancestor_type = adata->ancestor_type;

	data->ascendants.list = ((void*) ascendants) - ((void*) &data->ascendants);
	data->ascendants.size = num_ascendants;

	data->samples.list =
			num_samples
			? ((void*) samples) - ((void*) &data->samples)
			: 0;
	data->samples.size = num_samples;

	type->sample = O42A(o42a_obj_stype(O42A_ARGS atype));

	// propagate bodies
	o42a_obj_ctable_t ctable = {
		ancestor_type: atype,
		sample_type: sstype,
		object_type: type,
	};

	O42A(derive_ancestor_bodies(O42A_ARGS &ctable, DK_COPY));
	O42A(copy_samples(O42A_ARGS &atype->type.data, samples, mem));

#ifndef NDEBUG
	O42A(fill_field_infos(O42A_ARGS type, type_info));
#endif

	o42a_debug_dump_mem("Object: ", mem, 3);

	O42A_RETURN type;
}

typedef struct sample_data {
	o42a_obj_sample_t *sample;
	o42a_obj_body_t *old_body;
	size_t new_body;
} sample_data_t;

static inline size_t fill_sample_data(
		O42A_PARAMS
		size_t *start,
		const o42a_obj_data_t *const adata,
		const o42a_obj_data_t *const sdata,
		sample_data_t *sample_data) {
	O42A_ENTER(return 0);

	size_t num_samples = 0;
	o42a_obj_sample_t *old_sample = O42A(o42a_obj_samples(O42A_ARGS sdata));

	for (size_t i = sdata->samples.size; i > 0; --i) {

		o42a_obj_body_t *const old_body =
				O42A(o42a_obj_sample_body(O42A_ARGS old_sample));
		const o42a_obj_stype_t *const body_type =
				O42A(old_body->methods->object_type);

		sample_data->sample = old_sample;
		if (O42A(o42a_obj_ascendant_of_type(O42A_ARGS adata, body_type))) {
			// Sample body already present in ancestor.
			sample_data->old_body = NULL;
		} else {
			sample_data->old_body = old_body;

			const o42a_layout_t layout = body_type->main_body_layout;
			const size_t new_body = o42a_layout_pad(O42A_ARGS *start, layout);

			sample_data->new_body = new_body;
			*start = new_body + o42a_layout_size(O42A_ARGS layout);
			++num_samples;
		}
		++sample_data;
		++old_sample;
	}

	O42A_DEBUG("Number of samples: %zi\n", num_samples);

	O42A_RETURN num_samples;
}

static inline void propagate_samples(
		O42A_PARAMS
		o42a_obj_ctable_t *const ctable,
		void *mem,
		o42a_obj_body_t *const ancestor_body,
		sample_data_t *sample_data) {
	O42A_ENTER(return);

	const size_t num_ancestors =
			ctable->ancestor_type->type.data.ascendants.size;
	const o42a_obj_data_t *const data = &ctable->object_type->data;
	o42a_obj_ascendant_t *ascendant =
			o42a_obj_ascendants(O42A_ARGS data) + num_ancestors;
	o42a_obj_sample_t *sample = O42A(o42a_obj_samples(O42A_ARGS data));

	for (size_t i = data->samples.size; i > 0;) {

		o42a_obj_body_t *const old_body = sample_data->old_body;

		if (!old_body) {
			// Body already present among ancestors.
			++sample_data;
			continue;
		}
		--i;

		o42a_obj_body_t *const new_body =
				(o42a_obj_body_t*) (mem + sample_data->new_body);

		sample->body = ((void*) new_body) - ((void*) sample);
		ascendant->type = old_body->methods->object_type;
		ascendant->body = ((void*) new_body) - ((void*) ascendant);

#ifndef NDEBUG

		O42A_DEBUG("Sample: <0x%lx>\n", (long) sample);
		// Copy sample header.
		O42A(o42a_dbg_copy_header(
				O42A_ARGS
				o42a_dbg_header(sample_data->sample),
				&sample->__o42a_dbg_header__,
				(o42a_dbg_header_t*) mem));

		// Copy ascendant header.
		O42A(o42a_dbg_copy_header(
				O42A_ARGS
				o42a_dbg_header(o42a_obj_ascendant_of_type(
						O42A_ARGS
						&ctable->sample_type->data,
						old_body->methods->object_type)),
				&ascendant->__o42a_dbg_header__,
				(o42a_dbg_header_t*) mem));

#endif

		ctable->body_type = old_body->methods->object_type;
		ctable->from.body = old_body;
		ctable->to.body = new_body;
		O42A(derive_object_body(O42A_ARGS ctable, ancestor_body, DK_PROPAGATE));

		++sample_data;
		++sample;
		++ascendant;
	}

	O42A_RETURN;
}

o42a_obj_t *o42a_obj_new(
		O42A_PARAMS
		const o42a_obj_ctr_t *const ctr) {
	O42A_ENTER(return NULL);

	o42a_obj_t *ancestor = NULL;
	o42a_obj_type_t *atype = ctr->ancestor_type;

	if (atype) {
		if (atype->type.data.flags & O42A_OBJ_VOID) {
			atype = NULL;
		} else {
			ancestor = O42A(o42a_obj_by_data(
					O42A_ARGS
					&ctr->ancestor_type->type.data));
		}
	}

	const o42a_obj_data_t *const adata = atype ? &atype->type.data : NULL;

	if (adata && (adata->flags & O42A_OBJ_FALSE)) {
		O42A_RETURN NULL;
	}

	o42a_obj_type_t *const stype = ctr->type;
	o42a_obj_stype_t *const sstype = O42A(o42a_obj_stype(O42A_ARGS stype));

	if (!atype) {
		// Sample has no ancestor.
		// Propagate sample.
		o42a_debug_mem_name("No ancestor of ", stype);

		o42a_obj_rtype_t *const result =
				O42A(propagate_object(O42A_ARGS ctr, stype, sstype, 0));

		O42A_RETURN o42a_obj_by_data(O42A_ARGS &result->data);
	}

	const o42a_obj_ascendant_t *const consuming_ascendant =
			O42A(o42a_obj_ascendant_of_type(O42A_ARGS adata, sstype));

	if (consuming_ascendant) {
		// Ancestor has a body of the same type as object.
		// Propagate ancestor.
		o42a_debug_mem_name("Sample consumed by ", consuming_ascendant);

		o42a_obj_rtype_t *const result =
				O42A(propagate_object(O42A_ARGS ctr, atype, sstype, 1));

		// obtain consuming ascendant from result type
		const o42a_obj_ascendant_t *const a_ascendants =
				O42A(o42a_obj_ascendants(O42A_ARGS adata));
		const o42a_obj_ascendant_t *const res_ascendants =
				O42A(o42a_obj_ascendants(O42A_ARGS &result->data));
		const o42a_obj_ascendant_t *const res_consuming_ascendant =
				res_ascendants + (consuming_ascendant - a_ascendants);

		O42A_RETURN o42a_obj_ascendant_body(O42A_ARGS res_consuming_ascendant);
	}

	// Ancestor bodies size.
	size_t start = offsetof(o42a_obj_stype_t, data) - adata->start;

	const o42a_obj_data_t *const sdata = &stype->type.data;
	sample_data_t sample_data[sdata->samples.size];
	const size_t num_samples = O42A(fill_sample_data(
			O42A_ARGS
			&start,
			adata,
			sdata,
			sample_data));

	const size_t main_body_start = O42A(o42a_layout_pad(
			O42A_ARGS
			start,
			sstype->main_body_layout));

	start =
			main_body_start
			+ o42a_layout_size(O42A_ARGS sstype->main_body_layout);

	const o42a_layout_t obj_rtype_layout = O42A_LAYOUT(o42a_obj_rtype_t);
	const size_t type_start =
			o42a_layout_pad(O42A_ARGS start, obj_rtype_layout);

	const o42a_layout_t ascendant_layout = O42A_LAYOUT(o42a_obj_ascendant_t);
	const size_t ascendants_start = o42a_layout_pad(
			O42A_ARGS
			type_start + o42a_layout_size(O42A_ARGS obj_rtype_layout),
			ascendant_layout);
	const size_t num_ascendants = adata->ascendants.size + num_samples + 1;

	const o42a_layout_t sample_layout = O42A_LAYOUT(o42a_obj_sample_t);
	const size_t samples_start =
			o42a_layout_pad(
					O42A_ARGS
					ascendants_start + o42a_layout_array_size(
							O42A_ARGS ascendant_layout,
							num_ascendants),
			sample_layout);

	size_t size =
			samples_start
			+ o42a_layout_array_size(O42A_ARGS sample_layout, num_samples);

#ifndef NDEBUG

	const size_t type_field_num =
			num_ascendants + 1 + num_ascendants + num_samples;
	const size_t type_info_start =
			O42A(get_type_info_start(O42A_ARGS &size, type_field_num));

#endif

	void *const mem = O42A(o42a_mem_alloc_gc(O42A_ARGS size));
	o42a_obj_t *const object = (o42a_obj_t*) (mem + main_body_start);
	o42a_obj_rtype_t *const type = (o42a_obj_rtype_t*) (mem + type_start);
	o42a_obj_data_t *const data = &type->data;
	o42a_obj_ascendant_t *const ascendants =
			(o42a_obj_ascendant_t*) (mem + ascendants_start);

#ifndef NDEBUG

	// Fill new object's type info (without field info yet).
	o42a_dbg_type_info_t *type_info =
			(o42a_dbg_type_info_t*) (mem + type_info_start);

	O42A(fill_type_info(O42A_ARGS mem, type, type_info));
	type_info->field_num = type_field_num;

#endif

	// Build samples.
	O42A(copy_ancestor_ascendants(O42A_ARGS adata, ascendants, mem));

	o42a_obj_ascendant_t *const main_ascendant =
			ascendants + (num_ascendants - 1);

	main_ascendant->type = sstype;
	main_ascendant->body = ((void*) object) - ((void*) main_ascendant);

#ifndef NDEBUG

	// Fill the main ascendant`s debug header.
	O42A(o42a_dbg_copy_header(
			O42A_ARGS
			o42a_dbg_header(o42a_obj_ascendant_of_type(
					O42A_ARGS
					&stype->type.data,
					sstype)),
			&main_ascendant->__o42a_dbg_header__,
			(o42a_dbg_header_t*) mem));

#endif

	// fill object type and data
	const size_t data_start = type_start + offsetof(o42a_obj_rtype_t, data);

	data->object = main_body_start - data_start;
	data->flags = O42A_OBJ_RT | (sdata->flags & O42A_OBJ_INHERIT_MASK);
	data->start = -data_start;

	data->value.flags = O42A_UNKNOWN | O42A_INDEFINITE;
	data->value_f = sdata->value_f;
	data->requirement_f = sdata->requirement_f;
	data->claim_f = sdata->claim_f;
	data->condition_f = sdata->condition_f;
	data->proposition_f = sdata->proposition_f;

	data->owner_type = ctr->scope_type;
	data->ancestor_type = atype;

	data->ascendants.list = ((void*) ascendants) - ((void*) &data->ascendants);
	data->ascendants.size = num_ascendants;

	data->samples.list =
			num_samples
			? (mem + samples_start) - ((void*) &data->samples)
			: 0;
	data->samples.size = num_samples;

	type->sample = sstype;

	// propagate sample and inherit ancestor
	o42a_obj_ctable_t ctable = {
		ancestor_type: atype,
		sample_type: sstype,
		object_type: type,
	};

	o42a_obj_body_t *const ancestor_body =
			(o42a_obj_body_t*) (mem + (adata->object - adata->start));

	derive_ancestor_bodies(O42A_ARGS &ctable, DK_INHERIT);
	O42A(propagate_samples(
			O42A_ARGS
			&ctable,
			mem,
			ancestor_body,
			sample_data));

	ctable.body_type = sstype;
	ctable.from.body = O42A(o42a_obj_by_data(O42A_ARGS sdata));
	ctable.to.body = object;
	O42A(derive_object_body(O42A_ARGS &ctable, ancestor_body, DK_MAIN));

#ifndef NDEBUG
	O42A(fill_field_infos(O42A_ARGS type, type_info));
#endif

	o42a_debug_dump_mem("Object: ", mem, 3);

	O42A_RETURN object;
}

o42a_cond_t o42a_obj_cond_false(O42A_PARAMS o42a_obj_t *const object) {
	O42A_ENTER(return O42A_FALSE);
	O42A_RETURN O42A_FALSE;
}

o42a_cond_t o42a_obj_cond_true(O42A_PARAMS o42a_obj_t *const object) {
	O42A_ENTER(return O42A_FALSE);
	O42A_RETURN O42A_TRUE;
}

o42a_cond_t o42a_obj_cond_unknown(O42A_PARAMS o42a_obj_t *const object) {
	O42A_ENTER(return O42A_FALSE);
	O42A_RETURN O42A_UNKNOWN;
}

o42a_cond_t o42a_obj_cond_stub(O42A_PARAMS o42a_obj_t *const object) {
	O42A_ENTER(return O42A_FALSE);
	o42a_error_print(O42A_ARGS "Object condition stub invoked");
	O42A_RETURN O42A_UNKNOWN;
}


void o42a_obj_val_false(
		O42A_PARAMS
		o42a_val_t *const result,
		o42a_obj_t *const object) {
	O42A_ENTER(return);
	result->flags = O42A_FALSE;
	O42A_RETURN;
}

void o42a_obj_val_void(
		O42A_PARAMS
		o42a_val_t *const result,
		o42a_obj_t *const object) {
	O42A_ENTER(return);
	result->flags = O42A_TRUE;
	O42A_RETURN;
}

void o42a_obj_val_unknown(
		O42A_PARAMS
		o42a_val_t *const result,
		o42a_obj_t *const object) {
	O42A_ENTER(return);
	O42A_RETURN;
}

void o42a_obj_val_stub(
		O42A_PARAMS
		o42a_val_t *const result,
		o42a_obj_t *const object) {
	O42A_ENTER(return);
	o42a_error_print(O42A_ARGS "Object value stub invoked");
	result->flags = O42A_FALSE;
	O42A_RETURN;
}


o42a_obj_body_t *o42a_obj_ref_null(O42A_PARAMS o42a_obj_t *scope) {
	O42A_ENTER(return NULL);
	O42A_RETURN NULL;
}

o42a_obj_body_t *o42a_obj_ref_stub(O42A_PARAMS o42a_obj_t *scope) {
	O42A_ENTER(return NULL);
	o42a_error_print(O42A_ARGS "Object reference stub invoked");
	O42A_RETURN NULL;
}


o42a_obj_body_t *o42a_obj_constructor_stub(
		O42A_PARAMS
		o42a_obj_t *scope,
		struct o42a_fld_obj *field) {
	O42A_ENTER(return NULL);
	o42a_error_print(O42A_ARGS "Object constructor stub invoked");
	O42A_RETURN NULL;
}
