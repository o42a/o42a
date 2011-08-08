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
#ifndef O42A_OBJECT_H
#define O42A_OBJECT_H

#include "o42a/types.h"


struct o42a_fld_obj;
union o42a_fld;
struct o42a_obj_methods;
union o42a_obj_type;


/** Object represented by it's body. */
typedef struct o42a_obj_body o42a_obj_t;


/**
 * Object value calculator function.
 *
 * \param result[out] object value to fill by function.
 * \param object[in] object pointer.
 */
typedef void o42a_obj_val_ft(O42A_DECLS o42a_val_t *, o42a_obj_t *);

/**
 * Object condition calculator function.
 *
 * \param object[in] object pointer.
 *
 * \return condition.
 */
typedef o42a_cond_t o42a_obj_cond_ft(O42A_DECLS o42a_obj_t *);

/**
 * Object reference function.
 *
 * \param scope[in] scope object pointer.
 *
 * \return resulting object reference.
 */
typedef o42a_obj_t *o42a_obj_ref_ft(O42A_DECLS o42a_obj_t *);

/**
 * Object constructor function.
 *
 * \param scope[in] scope object pointer.
 * \param fld[in] pointer to field, which object construction invoked for. This
 * may be a field from object different from scope (see o42a_fld_obj.previous),
 * but is always belongs to compatible body of that object.
 *
 * \return resulting object reference.
 */
typedef o42a_obj_t *o42a_obj_constructor_ft(
		O42A_DECLS
		o42a_obj_t *,
		struct o42a_fld_obj *);

/**
 * Object type flags.
 *
 * Used in o42a_obj_data.flags.
 */
enum o42a_obj_type_flags {

	/**
	 * Object is created at run-time.
	 *
	 * If this is set, than object type implementation is o42a_obj_rtype.
	 * Otherwise it's o42a_obj_stype.
	 */
	O42A_OBJ_RT = 0x1,

	/** Object is abstract. */
	O42A_OBJ_ABSTRACT = 0x2,

	/** Object is protobj_type. */
	O42A_OBJ_PROTOTYPE = 0x4,

	/** Object is VOID special object. */
	O42A_OBJ_VOID = 0x80000000,

	/** Object is FALSE special object. */
	O42A_OBJ_FALSE = 0x40000000,

	/** Type flags mask inherited when constructing new instance. */
	O42A_OBJ_INHERIT_MASK = 0x0FFFFFFF,

};

/**
 * Object body flags.
 *
 * Used in o42a_obj_body.flags.
 */
enum o42a_obj_body_flags {

	/**
	 * The mask to apply to flags to gain a kind of body.
	 *
	 * \see o42a_obj_body_kind for possible values.
	 */
	O42A_OBJ_BODY_TYPE = 0x3,

};

/**
 * The kinds of object bodies.
 *
 * Apply O42A_OBJ_BODY_TYPE to o42a_obj_body.flags to gain one of these values.
 *
 * Body kind is exact only for static objects. When constructing object at run
 * time, it is only kept when O42A_OBJ_CTR_FIELD_PROPAGATION flags is set.
 * Otherwise the value is dropped to O42A_OBJ_BODY_INHERITED.
 *
 * This is used to update scope fields properly.
 */
enum o42a_obj_body_kind {

	/** The body is inherited from ancestor. */
	O42A_OBJ_BODY_INHERITED = 0,

	/** The body is from explicit sample. */
	O42A_OBJ_BODY_EXPLICIT = 1,

	/** The body is propagated from ascendant field. */
	O42A_OBJ_BODY_PROPAGATED = 2,

	/** The body is main one. */
	O42A_OBJ_BODY_MAIN = 3,

};

/**
 * Object body.
 *
 * Contains field definitions, sample object bodies and other info.
 *
 * Each object contains one or more bodies. One of them is called main
 * and corresponds to object type. Others corresponds to object ancestors.
 */
typedef struct o42a_obj_body {

	O42A_HEADER;

	/*
	 * Relative pointer to object type.
	 *
	 * Each object body of the same object refers to the same type instance
	 * of that object.
	 */
	o42a_rptr_t object_type;

	/**
	 * Relative pointer to ancestor's body.
	 */
	o42a_rptr_t ancestor_body;

	/**
	 * Pointer to object methods corresponding to this body's type.
	 */
	struct o42a_obj_methods *methods;

	/**
	 * Object body flags.
	 *
	 * \see o42a_obj_body_flags for possible values.
	 */
	uint32_t flags;

	/* Arbitrary object fields. */
	char fields[0];

} o42a_obj_body_t;

/**
 * Object data.
 *
 * Contains object value and combines other data necessary to maintain object
 * and it's inheritance.
 *
 * There is exactly one data section per object.
 *
 * Object type always starts with data field, so it is safe to cast type pointer
 * to data pointer.
 */
typedef struct o42a_obj_data {

	O42A_HEADER;

	/** Relative pointer to main object body. */
	o42a_rptr_t object;

	/**
	 * Type flags.
	 *
	 * This can be used to distinguish object type implementation and contains
	 * other information. See o42a_obj_type_flags enum.
	 */
	uint32_t flags;

	/**
	 * Relative pointer to memory block containing this object.
	 *
	 * This may be a virtual block start when ancestor body is not physically
	 * present.
	 */
	o42a_rptr_t start;

	/** Constructed object value. */
	o42a_val_t value;

	/**
	 * Object value calculator function.
	 *
	 * Accepts main object body as a second argument.
	 */
	o42a_obj_val_ft *value_f;

	/**
	 * Object's requirement calculator function.
	 *
	 * Accepts main object body as a second argument.
	 */
	o42a_obj_cond_ft *requirement_f;

	/**
	 * Object's claim calculator function.
	 *
	 * Accepts main object body as a second argument.
	 */
	o42a_obj_val_ft *claim_f;

	/**
	 * Object's condition calculator function.
	 *
	 * Implies common claim.
	 *
	 * Accepts main object body as a second argument.
	 */
	o42a_obj_cond_ft *condition_f;

	/**
	 * Object's proposition calculator function.
	 *
	 * Accepts main object body as a second argument.
	 */
	o42a_obj_val_ft *proposition_f;

	/**
	 * Pointer to the type of this object's owner (i.e. enclosing object).
	 *
	 * May be NULL when object is local.
	 */
	union o42a_obj_type *owner_type;

	/**
	 * Object ancestor finder function.
	 *
	 * Accepts pointer to object's owner as argument.
	 *
	 * May be NULL when ancestor_type is static.
	 *
	 * \return ancestor pointer or NULL when ancestor is void.
	 */
	o42a_obj_ref_ft *ancestor_f;

	/**
	 * Ancestor type.
	 *
	 * Pointer to ancestor object data.
	 *
	 * NULL when ancestor is void.
	 */
	const union o42a_obj_type *ancestor_type;

	/** Relative pointer to the list of ascendant descriptors. */
	o42a_rlist_t ascendants;

	/** Relative pointer to the list of object sample descriptors. */
	o42a_rlist_t samples;

} o42a_obj_data_t;

/**
 * Static object type generated by compiler.
 */
typedef struct o42a_obj_stype {

	O42A_HEADER;

	/** Object data. */
	o42a_obj_data_t data;

	/** Relative pointer to the list of field descriptors. */
	o42a_rlist_t fields;

	/**
	 * Relative pointer to the list of field overrider descriptors.
	 *
	 * The list is ordered by field descriptor pointers.
	 */
	o42a_rlist_t overriders;

	/** Main body layout. */
	o42a_layout_t main_body_layout;

} o42a_obj_stype_t;


/**
 * Object ascendant descriptor.
 *
 * Describes object body corresponding to ascending type.
 */
typedef struct o42a_obj_ascendant {

	O42A_HEADER;

	/** Pointer to the ascending object's type. */
	o42a_obj_stype_t *type;

	/** Relative pointer to ascendant body. */
	o42a_rptr_t body;

} o42a_obj_ascendant_t;

/**
 * Object sample descriptor.
 *
 * Describes explicit sample or propagated field's body .
 */
typedef struct o42a_obj_sample {

	O42A_HEADER;

	/** Relative pointer to sample body. */
	o42a_rptr_t body;

} o42a_obj_sample_t;

/**
 * Field descriptor.
 *
 * This is used to describe the new fields only. Fields generated due to
 * override described with o42a_override.
 */
typedef const struct o42a_obj_field {

	O42A_HEADER;

	/** Pointer to object type the field first declared in. */
	o42a_obj_stype_t *declared_in;

	/**
	 * Field kind.
	 *
	 * One of the o42a_obj_field_kind enum values.
	 */
	uint32_t kind;

	/** Pointer to the field content within main body. */
	o42a_rptr_t fld;

} o42a_obj_field_t;

/**
 * Field overrider descriptor.
 */
typedef const struct o42a_obj_overrider {

	O42A_HEADER;

	/** Pointer to descriptor of the overridden field. */
	o42a_obj_field_t *field;

	/** Type of the object the overrider field were defined in. */
	o42a_obj_stype_t *defined_in;

	/** Relative pointer to the body containing overriding field. */
	o42a_rptr_t body;

} o42a_obj_overrider_t;

/**
 * Object type generated at run-time.
 */
typedef struct o42a_obj_rtype {

	O42A_HEADER;

	/** Object data. */
	o42a_obj_data_t data;

	/** Pointer to sample type. */
	o42a_obj_stype_t *sample;

} o42a_obj_rtype_t;

/**
 * Object type.
 *
 * There is exactly one type section per object, which can be either of
 * o42a_obj_stype_t or o42a_obj_rtype_t type depending on header flags.
 */
typedef union o42a_obj_type {

	struct {

		O42A_HEADER;

		/**
		 * Object data.
		 *
		 * Always presents.
		 */
		o42a_obj_data_t data;

	} type;

	/**
	 * Static object type.
	 *
	 * Presents if !(header.flags & O24A_OBJ_RT).
	 */
	o42a_obj_stype_t stype;

	/**
	 * Run-time object type.
	 *
	 * Presents if (header.flags & O24A_OBJ_RT).
	 */
	o42a_obj_rtype_t rtype;

} o42a_obj_type_t;

/**
 * Object methods.
 *
 * Each body has an associated methods instance. Different bodies can share the
 * same meta instance.
 */
typedef struct o42a_obj_methods {

	O42A_HEADER;

	/**
	 * Pointer to object type, where corresponding body were first declared in.
	 */
	o42a_obj_stype_t *object_type;

} o42a_obj_methods_t;

/**
 * Object construction data.
 */
typedef struct o42a_obj_ctr {

	O42A_HEADER;

	/**
	 * Pointer to enclosing object's type.
	 *
	 * NULL when constructing object within imperative code.
	 */
	o42a_obj_type_t *scope_type;

	/**
	 * Ancestor finder function.
	 *
	 * When NULL an ancestor_type should be used.
	 *
	 * See o42a_obj_data_t.ancestor_f.
	 */
	o42a_obj_ref_ft *ancestor_f;

	/**
	 * Ancestor type.
	 *
	 * Ignored when ancestor_f specified.
	 *
	 * See o42a_obj_data_t.ancestor_type.
	 */
	o42a_obj_type_t *ancestor_type;

	/**
	 * Sample object type.
	 */
	o42a_obj_type_t *type;

} o42a_obj_ctr_t;

typedef struct o42a_obj_ctable {

	O42A_HEADER;

	o42a_obj_type_t *const ancestor_type;

	o42a_obj_stype_t *const sample_type;

	o42a_obj_rtype_t *const object_type;

	o42a_obj_stype_t *body_type;

	o42a_obj_field_t *field;

	struct o42a_obj_cside {

		o42a_obj_body_t *body;

		union o42a_fld *fld;

	} from;

	struct o42a_obj_cside to;

} o42a_obj_ctable_t;


extern const struct {

	O42A_HEADER;

	const o42a_obj_stype_t *root_type;

	const o42a_obj_stype_t *void_type;

	const o42a_obj_stype_t *false_type;

	const o42a_obj_stype_t *integer_type;

	const o42a_obj_stype_t *float_type;

	const o42a_obj_stype_t *string_type;

} o42a;


#ifdef __cplusplus
extern "C" {
#endif


/**
 * Retrieves object type from it's body.
 *
 * \param body[in] object body pointer.
 *
 * \return object type pointer.
 */
o42a_obj_type_t *o42a_obj_type(O42A_DECLS const o42a_obj_body_t *);

/**
 * Retrieves object ancestor's body.
 *
 * \param body[in] object body pointer.
 *
 * \return ancestor body pointer.
 */
o42a_obj_body_t *o42a_obj_ancestor(O42A_DECLS const o42a_obj_body_t *);

/**
 * Extracts object's static type.
 *
 * \param type[in] object type.
 *
 * \return either type itself (if it's static) or sample type (if type is
 * run-time generated).
 */
o42a_obj_stype_t *o42a_obj_stype(O42A_DECLS o42a_obj_type_t *);

/**
 * Retrieves object from it's data.
 *
 * \param data[in] object data pointer.
 *
 * \return pointer to object's main body.
 */
o42a_obj_body_t *o42a_obj_by_data(O42A_DECLS const o42a_obj_data_t *);

/**
 * Retrieves object ascendant's descriptors.
 *
 * \param data[in] object data pointer.
 *
 * \return pointer to the first element of ascendant descriptors array.
 */
o42a_obj_ascendant_t *o42a_obj_ascendants(O42A_DECLS const o42a_obj_data_t *);

/**
 * Retrieves object sample's descriptors.
 *
 * \param data[in] object data pointer.
 *
 * \return pointer to the first element of sample descriptors array.
 */
o42a_obj_sample_t *o42a_obj_samples(O42A_DECLS const o42a_obj_data_t *);

/**
 * Retrieves field descriptors.
 *
 * \param type[in] static object type pointer.
 *
 * \return pointer to the first element of the field descriptors array.
 */
o42a_obj_field_t *o42a_obj_fields(O42A_DECLS const o42a_obj_stype_t *);

/**
 * Retrieves field override descriptors.
 *
 * \param type[in] static object type pointer.
 *
 * \return pointer to the first element of the field override descriptors array.
 */
o42a_obj_overrider_t *o42a_obj_overriders(O42A_DECLS const o42a_obj_stype_t *);

/**
 * Retrieves object body corresponding to the given ascendant.
 *
 * \param ascendant[in] pointer to ascendant descriptor.
 *
 * \return body pointer.
 */
o42a_obj_t *o42a_obj_ascendant_body(O42A_DECLS const o42a_obj_ascendant_t *);

/**
 * Retrieves object body corresponding to the given sample.
 *
 * \param sample[in] pointer to ascendant descriptor.
 *
 * \return body pointer.
 */
o42a_obj_t *o42a_obj_sample_body(O42A_DECLS const o42a_obj_sample_t *);

o42a_obj_overrider_t *o42a_obj_field_overrider(
		O42A_DECLS
		const o42a_obj_stype_t *,
		const o42a_obj_field_t *);

/**
 * Searches for ascendant descriptor of the given type.
 *
 * \param data object data to search sample descriptor in.
 * \param type object type to search for.
 *
 * \return ascendant descriptor or NULL if not found.
 */
const o42a_obj_ascendant_t *o42a_obj_ascendant_of_type(
		O42A_DECLS
		const o42a_obj_data_t *,
		const o42a_obj_stype_t *);

/**
 * Searches for the object's body of the given type.
 *
 * \param object[in] object to cast.
 * \param type[in] static type to cast to.
 *
 * \return object body pointer corresponding to the given type or NULL if object
 * is not derived from type.
 */
o42a_obj_body_t *o42a_obj_cast(
		O42A_DECLS
		o42a_obj_t *,
		const o42a_obj_stype_t *);

/**
 * Instantiates a new object.
 *
 * \param ctr[in] filled-in construction data.
 *
 * \return pointer to object's body of the sample type.
 */
o42a_obj_t *o42a_obj_new(O42A_DECLS const o42a_obj_ctr_t *);


o42a_cond_t o42a_obj_cond_false(O42A_DECLS o42a_obj_t *);

o42a_cond_t o42a_obj_cond_true(O42A_DECLS o42a_obj_t *);

o42a_cond_t o42a_obj_cond_unknown(O42A_DECLS o42a_obj_t *);


void o42a_obj_val_false(O42A_DECLS o42a_val_t *, o42a_obj_t *);

void o42a_obj_val_void(O42A_DECLS o42a_val_t *, o42a_obj_t *);

void o42a_obj_val_unknown(O42A_DECLS o42a_val_t *, o42a_obj_t *);

void o42a_obj_val_stub(O42A_DECLS o42a_val_t *, o42a_obj_t *);


/**
 * Object reference function, which always returns NULL.
 *
 * This can be used e.g. to refer void object ancestor.
 */
o42a_obj_body_t *o42a_obj_ref_null(O42A_DECLS o42a_obj_t *);


#ifdef __cplusplus
}
#endif


#endif /* O42A_OBJECT_H */
