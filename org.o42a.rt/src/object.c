/*
    Run-Time Library
    Copyright (C) 2010,2011 Ruslan Lopatin

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

#include "o42a/field.h"

#include <malloc.h>


inline o42a_obj_type_t *o42a_obj_type(const o42a_obj_body_t *const body) {
	return (o42a_obj_type_t*) (((void*) body) + body->object_type);
}

inline o42a_obj_body_t *o42a_obj_ancestor(const o42a_obj_body_t *const body) {
	return (o42a_obj_body_t*) (((void*) body) + body->ancestor_body);
}

inline o42a_obj_stype_t *o42a_obj_stype(o42a_obj_type_t *const type) {
	return (type->data.flags & O42A_OBJ_RT)
			? type->obj_rtype.sample : &type->obj_stype;
}

inline o42a_obj_t *o42a_obj_by_data(const o42a_obj_data_t *const data) {
	return (o42a_obj_t*) (((void*) data) + data->object);
}

inline o42a_obj_ascendant_t *o42a_obj_ascendants(
		const o42a_obj_data_t *const data) {

	const o42a_rlist_t *const list = &data->ascendants;

	return (o42a_obj_ascendant_t*) (((void*) list) + list->list);
}

inline o42a_obj_sample_t *o42a_obj_samples(const o42a_obj_data_t *const data) {

	const o42a_rlist_t *const list = &data->samples;

	return (o42a_obj_sample_t*) (((void*) list) + list->list);
}

inline o42a_obj_field_t* o42a_obj_fields(
		const o42a_obj_stype_t *const type) {

	const o42a_rlist_t *const list = &type->fields;

	return (o42a_obj_field_t*) (((void*) list) + list->list);
}

inline o42a_obj_overrider_t* o42a_obj_overriders(
		const o42a_obj_stype_t *const type) {

	const o42a_rlist_t *const list = &type->overriders;

	return (o42a_obj_overrider_t*) (((void*) list) + list->list);
}

inline o42a_obj_body_t *o42a_obj_ascendant_body(
		const o42a_obj_ascendant_t *const ascendant) {
	return (o42a_obj_body_t *) (((void*) ascendant) + ascendant->body);
}

inline o42a_obj_body_t *o42a_obj_sample_body(
		const o42a_obj_sample_t *const sample) {
	return (o42a_obj_body_t *) (((void*) sample) + sample->body);
}

const o42a_obj_overrider_t *o42a_obj_field_overrider(
		const o42a_obj_stype_t *const sample_type,
		const o42a_obj_field_t *const field) {
	O42A_ENTER;

	const size_t num_overriders = sample_type->overriders.size;
	const o42a_obj_overrider_t *const overriders =
			o42a_obj_overriders(sample_type);

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
		const o42a_obj_data_t *const data,
		const o42a_obj_stype_t *const type) {
	O42A_ENTER;

	o42a_debug_mem_name("--- Data:", data);
	o42a_debug_mem_name("--- Type:", type);
	const o42a_obj_ascendant_t *ascendant = o42a_obj_ascendants(data);

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

	const o42a_obj_ascendant_t *const ascendant =
			o42a_obj_ascendant_of_type(data, type);

	return ascendant ? o42a_obj_ascendant_body(ascendant) : NULL;
}

o42a_obj_body_t *o42a_obj_cast(
		o42a_obj_t *const object,
		const o42a_obj_stype_t *const type) {
	O42A_ENTER;

	if (type->data.flags & O42A_OBJ_VOID) {
		// any body can be void
		o42a_debug_mem_name("Cast to void: ", object);
		O42A_RETURN object;
	}
	if (object->methods->object_type == type) {
		// body of the necessary type
		o42a_debug_mem_name("Cast not required: ", object);
		o42a_debug_mem_name("     to: ", type);
		O42A_RETURN object;
	}

	o42a_debug_mem_name("Cast of: ", object);
	o42a_debug_mem_name("     to: ", type);

	o42a_obj_body_t *const result =
			body_of_type(&o42a_obj_type(object)->data, type);

	o42a_debug_mem_name("Cast result: ", result);

	O42A_RETURN result;
}

static inline void copy_ancestor_ascendants(
		const o42a_obj_data_t *const ancestor_data,
		o42a_obj_ascendant_t *ascendants,
		void *start) {
	O42A_ENTER;

	void* astart = ((void*) ancestor_data) + ancestor_data->start;
	const o42a_obj_ascendant_t *a = o42a_obj_ascendants(ancestor_data);
	const o42a_rptr_t aascendants_rptr = ((void*) a) - astart;
	const o42a_rptr_t ascendants_rptr = ((void*) ascendants) - start;
	const o42a_rptr_t diff = aascendants_rptr - ascendants_rptr;

	for (size_t i = ancestor_data->ascendants.size; i > 0; --i) {
		ascendants->type = a->type;
		ascendants->body = a->body + diff;
		++a;
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
		int kind) {
	O42A_ENTER;

	const o42a_obj_body_t *const from_body = ctable->from.body;
	o42a_obj_body_t *const to_body = ctable->to.body;

	O42A_DEBUG("Derive body %lx -> %lx\n", (long) from_body, (long) to_body);
	o42a_debug_mem_name("Body type: ", ctable->body_type);

	// fill header
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


	// derive fields
	const size_t num_fields = ctable->body_type->fields.size;
	o42a_obj_field_t *const fields = o42a_obj_fields(ctable->body_type);

	for (size_t i = 0; i < num_fields; ++i) {

		o42a_obj_field_t *const field = fields + i;

		ctable->field = field;
		ctable->from.fld = o42a_fld_by_field(from_body, field);
		ctable->to.fld = o42a_fld_by_field(to_body, field);

		const o42a_fld_desc_t *const desc = o42a_fld_desc(field);

		O42A_DEBUG(
				kind == DK_INHERIT
				? "Inherit field %lx -> %lx\n"
				: "Propagate field %lx -> %lx\n",
				(long) ctable->from.fld,
				(long) ctable->to.fld);
		o42a_debug_dump_mem(field, 1000);
		(kind == DK_INHERIT ? desc->inherit : desc->propagate) (ctable);
	}

	O42A_DEBUG(
			kind == DK_MAIN ? "Main body (%lx)" : (
					kind == DK_INHERIT
					? "Inherited body (%lx)" : (
							kind == DK_COPY
							? "Copied body (%lx)"
							: "Propagated body (%lx)")),
			(long) to_body);
	o42a_debug_dump_field(
			to_body,
			o42a_dbg_subfield(
					o42a_dbg_field(from_body),
					"$all_bodies",
					"$main_body",
					NULL),
			1000);

	O42A_RETURN;
}

static void derive_ancestor_bodies(o42a_obj_ctable_t *const ctable, int kind) {
	O42A_ENTER;

	const o42a_obj_ascendant_t *ascendant =
			o42a_obj_ascendants(&ctable->object_type->data);
	const o42a_obj_data_t *const adata = &ctable->ancestor_type->data;
	const o42a_obj_ascendant_t *aascendant = o42a_obj_ascendants(adata);

	for (size_t i = adata->ascendants.size; i > 0; --i) {
		ctable->body_type = ascendant->type;
		ctable->from.body = o42a_obj_ascendant_body(aascendant);
		ctable->to.body = o42a_obj_ascendant_body(ascendant);
		derive_object_body(ctable, NULL, kind);
		++aascendant;
		++ascendant;
	}

	O42A_RETURN;
}

static inline void copy_samples(
		const o42a_obj_data_t *const ancestor_data,
		o42a_obj_sample_t *samples,
		void *start) {
	O42A_ENTER;

	void* astart = ((void*) ancestor_data) + ancestor_data->start;
	const o42a_obj_sample_t *s = o42a_obj_samples(ancestor_data);
	const o42a_rptr_t asamples_rptr = ((void*) s) - astart;
	const o42a_rptr_t samples_rptr = ((void*) samples) - start;
	const o42a_rptr_t diff = asamples_rptr - samples_rptr;

	for (size_t i = ancestor_data->samples.size; i > 0; --i) {
		samples->body = s->body + diff;
		++s;
		++samples;
	}

	O42A_RETURN;
}

static o42a_obj_rtype_t *propagate_object(
		const o42a_obj_ctr_t *const ctr,
		o42a_obj_type_t *const atype,
		const o42a_obj_data_t *const adata,
		o42a_obj_stype_t *const sstype,
		char inherit) {
	O42A_ENTER;

	const size_t main_body_start = (size_t) (adata->object - adata->start);
	const size_t data_start = -adata->start;
	const o42a_layout_t obj_rtype_layout = o42a_layout(o42a_obj_rtype_t);

	const size_t ascendants_start =
			data_start + o42a_layout_size(obj_rtype_layout);
	const size_t num_ascendants = adata->ascendants.size;
	const size_t num_samples = adata->samples.size;

	const o42a_layout_t ascendant_layout = o42a_layout(o42a_obj_ascendant_t);
	const o42a_layout_t sample_layout = o42a_layout(o42a_obj_sample_t);
	const size_t samples_start =
			o42a_layout_pad(
					ascendants_start
					+ o42a_layout_array_size(ascendant_layout, num_ascendants),
			sample_layout);

	const size_t size =
			samples_start + o42a_layout_array_size(sample_layout, num_samples);

	void *mem = malloc(size);

	o42a_obj_t *const object = (o42a_obj_t*) (mem + main_body_start);
	o42a_obj_rtype_t *const obj_type = (o42a_obj_rtype_t*) (mem + data_start);
	o42a_obj_data_t *const data = &obj_type->data;
	o42a_obj_ascendant_t *const ascendants =
			(o42a_obj_ascendant_t*) (mem + ascendants_start);
	o42a_obj_sample_t *const samples =
			(o42a_obj_sample_t*) (mem + samples_start);

	// build samples
	copy_ancestor_ascendants(adata, ascendants, mem);

	// fill object type and data
	data->object = adata->object;
	data->flags = O42A_OBJ_RT | (adata->flags & O42A_OBJ_INHERIT_MASK);
	data->start = adata->start;

	data->all_bodies_layout = adata->all_bodies_layout;

	data->value.flags = O42A_UNKNOWN | O42A_INDEFINITE;
	data->value_f = adata->value_f;
	data->requirement_f = adata->requirement_f;
	data->claim_f = adata->claim_f;
	data->condition_f = adata->condition_f;
	data->proposition_f = adata->proposition_f;

	data->owner_type = ctr->scope_type;
	data->ancestor_f = ctr->ancestor_f;
	data->ancestor_type = adata->ancestor_type;

	data->ascendants.list = ((void*) ascendants) - ((void*) &data->ascendants);
	data->ascendants.size = num_ascendants;

	data->samples.list = ((void*) samples) - ((void*) &data->samples);
	data->samples.size = num_samples;

	obj_type->sample = o42a_obj_stype(atype);

	// propagate bodies
	o42a_obj_ctable_t ctable = {
		ancestor_type: atype,
		sample_type: sstype,
		object_type: obj_type,
		flags: ctr->flags,
	};

	derive_ancestor_bodies(&ctable, DK_COPY);
	copy_samples(&atype->data, samples, mem);

	O42A_DEBUG("Object data: (%lx)", (long) obj_type);
	o42a_debug_dump_field(
			data,
			o42a_dbg_subfield(
					o42a_dbg_field(sstype),
					"object_data",
					NULL),
			1000);

	O42A_RETURN obj_type;
}

typedef struct sample_data {
	o42a_obj_sample_t *sample;
	o42a_obj_body_t *old_body;
	size_t new_body;
} sample_data_t;

static inline size_t fill_sample_data(
		o42a_layout_t *layout,
		const o42a_obj_data_t *const adata,
		const o42a_obj_data_t *const sdata,
		sample_data_t *sample_data) {
	O42A_ENTER;

	size_t num_samples = 0;
	o42a_obj_sample_t *old_sample = o42a_obj_samples(sdata);

	for (size_t i = sdata->samples.size; i > 0; --i) {

		o42a_obj_body_t *const old_body = o42a_obj_sample_body(old_sample);
		const o42a_obj_stype_t *const body_type =
				old_body->methods->object_type;

		sample_data->sample = old_sample;
		if (o42a_obj_ascendant_of_type(adata, body_type)) {
			// sample body already present in ancestor
			sample_data->old_body = NULL;
		} else {
			sample_data->old_body = old_body;
			sample_data->new_body = o42a_layout_pad(
					o42a_layout_size(*layout),
					body_type->main_body_layout);
			*layout = o42a_layout_both(*layout, body_type->main_body_layout);
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
		void *mem,
		o42a_obj_body_t *const ancestor_body,
		sample_data_t *sample_data,
		o42a_obj_sample_t *samples) {
	O42A_ENTER;

	for (size_t i = ctable->sample_type->data.samples.size; i > 0; --i) {

		o42a_obj_body_t *const old_body = sample_data->old_body;

		if (!old_body) {
			++sample_data;
			continue;
		}

		ctable->from.body = old_body;
		ctable->to.body = (o42a_obj_body_t*) (mem + sample_data->new_body);
		derive_object_body(ctable, ancestor_body, DK_PROPAGATE);
		samples->body = ((void*) samples) - ((void*) ctable->to.body);
		++sample_data;
		++samples;
	}

	O42A_RETURN;
}

o42a_obj_t *o42a_obj_new(const o42a_obj_ctr_t *const ctr) {
	O42A_ENTER;

	o42a_obj_t *ancestor = NULL;
	o42a_obj_type_t *atype = NULL;

	if (ctr->ancestor_f) {
		o42a_debug_func_name("Ancestor function: ", ctr->ancestor_f);
		o42a_debug_mem_name("Ancestor scope: ", ctr->scope_type);
		ancestor = (*ctr->ancestor_f)
				(o42a_obj_by_data(&ctr->scope_type->data));
		o42a_debug_mem_name("Ancestor: ", ancestor);
		if (ancestor) {
			atype = o42a_obj_type(ancestor);
			if (atype->data.flags & O42A_OBJ_VOID) {
				atype = NULL;
				ancestor = NULL;
			}
		}
	} else {
		atype = ctr->ancestor_type;
		if (atype) {
			if (atype->data.flags & O42A_OBJ_VOID) {
				atype = NULL;
			} else {
				ancestor = o42a_obj_by_data(&ctr->ancestor_type->data);
			}
		}
	}

	const o42a_obj_data_t *const adata = atype ? &atype->data : NULL;

	if (adata && (adata->flags & O42A_OBJ_FALSE)) {
		O42A_RETURN NULL;
	}

	o42a_obj_type_t *const stype = ctr->type;
	o42a_obj_stype_t *const sstype = o42a_obj_stype(stype);

	if (!adata) {
		// Sample has no ancestor.
		// Propagate sample.
		o42a_debug_mem_name("No ancestor of ", stype);

		o42a_obj_rtype_t *const result =
				propagate_object(ctr, stype, &stype->data, sstype, 0);

		O42A_RETURN o42a_obj_by_data(&result->data);
	}

	const o42a_obj_ascendant_t *const consuming_ascendant =
			o42a_obj_ascendant_of_type(adata, sstype);

	if (consuming_ascendant) {
		// Ancestor has a body of the same type as object.
		// Propagate ancestor.
		o42a_debug_mem_name("Sample consumed by ", consuming_ascendant);

		o42a_obj_rtype_t *const result =
				propagate_object(ctr, atype, adata, sstype, 1);

		// obtain consuming ascendant from result type
		const o42a_obj_ascendant_t *const a_ascendants =
				o42a_obj_ascendants(adata);
		const o42a_obj_ascendant_t *const res_ascendants =
				o42a_obj_ascendants(&result->data);
		const o42a_obj_ascendant_t *const res_consuming_ascendant =
				res_ascendants + (consuming_ascendant - a_ascendants);

		return o42a_obj_ascendant_body(res_consuming_ascendant);
	}

	o42a_layout_t bodies_layout = adata->all_bodies_layout;

	const o42a_obj_data_t *const sdata = &stype->data;
	sample_data_t sample_data[sdata->samples.size];
	const size_t num_samples = fill_sample_data(
			&bodies_layout,
			adata,
			sdata,
			sample_data);

	const size_t main_body_start = o42a_layout_pad(
			o42a_layout_size(bodies_layout),
			sstype->main_body_layout);

	bodies_layout = o42a_layout_both(bodies_layout, sstype->main_body_layout);

	const o42a_layout_t obj_rtype_layout = o42a_layout(o42a_obj_rtype_t);
	const size_t data_start =
			o42a_layout_pad(o42a_layout_size(bodies_layout), obj_rtype_layout);

	const size_t ascendants_start =
			data_start + o42a_layout_size(obj_rtype_layout);
	const size_t num_ascendants = adata->ascendants.size + 1;

	const o42a_layout_t ascendant_layout = o42a_layout(o42a_obj_ascendant_t);
	const o42a_layout_t sample_layout = o42a_layout(o42a_obj_sample_t);
	const size_t samples_start =
			o42a_layout_pad(
					ascendants_start
					+ o42a_layout_array_size(ascendant_layout, num_ascendants),
			sample_layout);

	const size_t size =
			samples_start + o42a_layout_array_size(sample_layout, num_samples);

	void *mem = malloc(size);

	o42a_obj_t *const object = (o42a_obj_t*) (mem + main_body_start);
	o42a_obj_rtype_t *const obj_type = (o42a_obj_rtype_t*) (mem + data_start);
	o42a_obj_data_t *const data = &obj_type->data;
	o42a_obj_ascendant_t *const ascendants =
			(o42a_obj_ascendant_t*) (mem + ascendants_start);

	// build samples
	copy_ancestor_ascendants(adata, ascendants, mem);

	o42a_obj_ascendant_t *const main_ascendant =
			ascendants + (num_ascendants - 1);

	main_ascendant->type = sstype;
	main_ascendant->body = ((void*) object) - ((void*) main_ascendant);

	// fill object type and data
	data->object = main_body_start - data_start;
	data->flags = O42A_OBJ_RT | (sdata->flags & O42A_OBJ_INHERIT_MASK);
	data->start = -data_start;

	data->all_bodies_layout = bodies_layout;

	data->value.flags = O42A_UNKNOWN | O42A_INDEFINITE;
	data->value_f = sdata->value_f;
	data->requirement_f = sdata->requirement_f;
	data->claim_f = sdata->claim_f;
	data->condition_f = sdata->condition_f;
	data->proposition_f = sdata->proposition_f;

	data->owner_type = ctr->scope_type;
	data->ancestor_f = ctr->ancestor_f;
	data->ancestor_type = atype;
	obj_type->sample = sstype;

	data->ascendants.list = ((void*) ascendants) - ((void*) &data->ascendants);
	data->ascendants.size = num_ascendants;

	// propagate sample and inherit ancestor
	o42a_obj_ctable_t ctable = {
		ancestor_type: atype,
		sample_type: sstype,
		object_type: obj_type,
		flags: ctr->flags,
	};

	o42a_obj_body_t *const ancestor_body =
			(o42a_obj_body_t*) (mem + (adata->object - adata->start));

	derive_ancestor_bodies(&ctable, DK_INHERIT);
	propagate_samples(
			&ctable,
			mem,
			ancestor_body,
			sample_data,
			(o42a_obj_sample_t*) (mem + samples_start));

	ctable.from.body = o42a_obj_by_data(sdata);
	ctable.to.body = object;
	ctable.body_type = sstype;
	derive_object_body(&ctable, ancestor_body, DK_MAIN);

	O42A_DEBUG("Object data: (%lx)", (long) obj_type);
	o42a_debug_dump_field(
			data,
			o42a_dbg_subfield(
					o42a_dbg_field(sdata),
					"object_data",
					NULL),
			1000);

	O42A_RETURN object;
}

o42a_bool_t o42a_obj_cond_false(o42a_obj_t *const object) {
	O42A_ENTER;
	O42A_RETURN O42A_FALSE;
}

o42a_bool_t o42a_obj_cond_true(o42a_obj_t *const object) {
	O42A_ENTER;
	O42A_RETURN O42A_TRUE;
}

void o42a_obj_val_false(o42a_val_t *const result, o42a_obj_t *const object) {
	O42A_ENTER;
	result->flags = O42A_FALSE;
	O42A_RETURN;
}

void o42a_obj_val_unknown(o42a_val_t *const result, o42a_obj_t *const object) {
	O42A_ENTER;
	result->flags = O42A_UNKNOWN;
	O42A_RETURN;
}

o42a_obj_body_t *o42a_obj_ref_null(o42a_obj_t *scope) {
	O42A_ENTER;
	O42A_RETURN NULL;
}
