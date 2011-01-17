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


/**
 * Object constructor flags.
 *
 * Used in o42a_ctr.flags.
 */
enum o42a_ctr_flags {

	/**
	 * Field propagation.
	 *
	 * If set, then object is constructed as a part of field propagation
	 * process. Otherwise a new object instantiation occurs.
	 */
	O42A_CTR_FIELD_PROPAGATION = 0x01,

};

/**
 * Object type flags.
 *
 * Used in o42a_odata.flags.
 */
enum o42a_otype_flags {

	/**
	 * Object is created at run-time.
	 *
	 * If this is set, than object type implementation is o42a_rotype.
	 * Otherwise it's o42a_sotype.
	 */
	O42A_OBJ_RT = 0x1,

	/** Object is abstract. */
	O42A_OBJ_ABSTRACT = 0x2,

	/** Object is prototype. */
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
 * Used in o42a_obody.flags.
 */
enum o42a_obody_flags {

	/**
	 * The mask to apply to flags to gain a kind of body.
	 *
	 * \see o42a_obody_kind for possible values.
	 */
	O42A_OBODY_TYPE = 0x3,

};

/**
 * The kinds of object bodies.
 *
 * Apply O42A_OBODY_TYPE to o42a_obody.flags to gain one of these values.
 *
 * Body kind is exact only for static objects. When Constructing object at run
 * time, it is only kept when O42A_CTR_FIELD_PROPAGATION flags is set. Otherwise
 * the value is dropped to O42A_OBODY_INHERITED.
 *
 * This is used to update scope fields properly.
 */
enum o42a_obody_kind {

	/** The body is inherited from ancestor. */
	O42A_OBODY_INHERITED = 0,

	/** The body is from explicit sample. */
	O42A_OBODY_EXPLICIT = 1,

	/** The body is propagated from ascendant field. */
	O42A_OBODY_PROPAGATED = 2,

	/** The body is main one. */
	O42A_OBODY_MAIN = 3,

};


/**
 * Object body.
 *
 * Contains field definitions, sample object bodies and other info.
 *
 * Each object contains one or more bodies. One of them is called main
 * and corresponds to object type. Others corresponds to object ancestors.
 */
struct o42a_obody {

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
	o42a_omethods_t *methods;

	/**
	 * Object body flags.
	 *
	 * \see o42a_obody_flags for possible values.
	 */
	uint32_t flags;

	/* Arbitrary object fields. */
	char fields[0];

};

/**
 * Object methods.
 *
 * Each body has an associated methods instance. Different bodies can share the
 * same meta instance.
 */
struct o42a_omethods {

	/**
	 * Pointer to object type, where corresponding body were first declared in.
	 */
	o42a_sotype_t *object_type;

};

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
struct o42a_odata {

	/** Relative pointer to main object body. */
	o42a_rptr_t object;

	/**
	 * Type flags.
	 *
	 * This can be used to distinguish object type implementation and contains
	 * other information. See o42a_otype_flags enum.
	 */
	uint32_t flags;

	/**
	 * Relative pointer to memory block containing this object.
	 *
	 * This may be a virtual block start when ancestor body is not physically
	 * present.
	 */
	o42a_rptr_t start;

	/** Layout of all bodies. */
	o42a_layout_t all_bodies_layout;

	/** Constructed object value. */
	o42a_val_t value;

	/**
	 * Object value calculator function.
	 *
	 * Accepts main object body as a second argument.
	 */
	o42a_oval_ft *value_f;

	/**
	 * Object's requirement calculator function.
	 *
	 * Accepts main object body as a second argument.
	 */
	o42a_ocond_ft *requirement_f;

	/**
	 * Object's claim calculator function.
	 *
	 * Accepts main object body as a second argument.
	 */
	o42a_oval_ft *claim_f;

	/**
	 * Object's post-condition calculator function.
	 *
	 * Implies common claim.
	 *
	 * Accepts main object body as a second argument.
	 */
	o42a_ocond_ft *post_condition_f;

	/**
	 * Object's proposition calculator function.
	 *
	 * Accepts main object body as a second argument.
	 */
	o42a_oval_ft *proposition_f;

	/**
	 * Pointer to the type of this object's owner (i.e. enclosing object).
	 *
	 * May be NULL when object is local.
	 */
	o42a_otype_t *owner_type;

	/**
	 * Object ancestor finder function.
	 *
	 * Accepts pointer to object's owner as argument.
	 *
	 * May be NULL when ancestor_type is static.
	 *
	 * \return ancestor pointer or NULL when ancestor is void.
	 */
	o42a_oref_ft *ancestor_f;

	/**
	 * Ancestor type.
	 *
	 * Pointer to ancestor object data.
	 *
	 * NULL when ancestor is void.
	 */
	const o42a_otype_t *ancestor_type;

	/** Relative pointer to the list of ascendant descriptors. */
	o42a_rlist_t ascendants;

	/** Relative pointer to the list of object sample descriptors. */
	o42a_rlist_t samples;

};

/**
 * Static object type generated by compiler.
 */
struct o42a_sotype {

	/** Object data. */
	o42a_odata_t data;

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

};


/**
 * Object ascendant descriptor.
 *
 * Describes object body corresponding to ascending type.
 */
struct o42a_ascendant {

	/** Pointer to the ascending object's type. */
	o42a_sotype_t *type;

	/** Relative pointer to ascendant body. */
	o42a_rptr_t body;

};

/**
 * Object sample descriptor.
 *
 * Describes explicit sample or propagated field's body .
 */
struct o42a_sample {

	/** Relative pointer to sample body. */
	o42a_rptr_t body;

};

/**
 * Field descriptor.
 *
 * This is used to describe the new fields only. Fields generated due to
 * override described with o42a_override.
 */
struct o42a_field {

	/** Pointer to object type the field first declared in. */
	o42a_sotype_t *declared_in;

	/**
	 * Field kind.
	 *
	 * One of the o42a_field_kind enum values.
	 */
	uint32_t kind;

	/** Pointer to the field content within main body. */
	o42a_rptr_t fld;

};

/**
 * Field overrider descriptor.
 */
struct o42a_overrider {

	/** Pointer to descriptor of the overridden field. */
	o42a_field_t *field;

	/** Type of the object the overrider field were defined in. */
	o42a_sotype_t *defined_in;

	/** Relative pointer to the body containing overriding field. */
	o42a_rptr_t body;

};

/**
 * Object type generated at run-time.
 */
struct o42a_rotype {

	/** Object data. */
	o42a_odata_t data;

	/** Pointer to sample type. */
	o42a_sotype_t *sample;

};

/**
 * Object type.
 *
 * There is exactly one type section per object, which can be either of
 * o42a_sotype_t or o42a_rotype_t type depending on header flags.
 */
union o42a_otype {

	/**
	 * Object data.
	 *
	 * Always presents.
	 */
	o42a_odata_t data;

	/**
	 * Static object type.
	 *
	 * Presents if !(header.flags & O24A_OBJ_RT).
	 */
	o42a_sotype_t sotype;

	/**
	 * Run-time object type.
	 *
	 * Presents if (header.flags & O24A_OBJ_RT).
	 */
	o42a_rotype_t rotype;

};

/**
 * Object construction data.
 */
typedef struct o42a_ctr {

	/**
	 * Pointer to enclosing object's type.
	 *
	 * NULL when constructing object within imperative code.
	 */
	o42a_otype_t *scope_type;

	/**
	 * Ancestor finder function.
	 *
	 * When NULL an ancestor_type should be used.
	 *
	 * See o42a_odata_t.ancestor_f.
	 */
	o42a_oref_ft *ancestor_f;

	/**
	 * Ancestor type.
	 *
	 * Ignored when ancestor_f specified.
	 *
	 * See o42a_odata_t.ancestor_type.
	 */
	o42a_otype_t *ancestor_type;

	/**
	 * Sample object type.
	 */
	o42a_otype_t *type;

	/**
	 * Constructor flags bit mask.
	 *
	 * \see o42a_ctr_flags enumeration for possible values.
	 */
	uint32_t flags;

} o42a_ctr_t;


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
o42a_otype_t *o42a_object_type(const o42a_obody_t*);

/**
 * Retrieves object ancestor's body.
 *
 * \param body[in] object body pointer.
 *
 * \return ancestor body pointer.
 */
o42a_obody_t *o42a_object_ancestor(const o42a_obody_t*);

/**
 * Extracts object's static type.
 *
 * \param type[in] object type.
 *
 * \return either type itself (if it's static) or sample type (if type is
 * run-time generated).
 */
o42a_sotype_t *o42a_static_type(o42a_otype_t*);

/**
 * Retrieves object from it's data.
 *
 * \param data[in] object data pointer.
 *
 * \return pointer to object's main body.
 */
o42a_obody_t *o42a_data_object(const o42a_odata_t*);

/**
 * Retrieves object ascendant's descriptors.
 *
 * \param data[in] object data pointer.
 *
 * \return pointer to the first element of ascendant descriptors array.
 */
o42a_ascendant_t *o42a_object_ascendants(const o42a_odata_t*);

/**
 * Retrieves object sample's descriptors.
 *
 * \param data[in] object data pointer.
 *
 * \return pointer to the first element of sample descriptors array.
 */
o42a_sample_t *o42a_object_samples(const o42a_odata_t*);

/**
 * Retrieves field descriptors.
 *
 * \param type[in] static object type pointer.
 *
 * \return pointer to the first element of the field descriptors array.
 */
o42a_field_t *o42a_object_fields(const o42a_sotype_t*);

/**
 * Retrieves field override descriptors.
 *
 * \param type[in] static object type pointer.
 *
 * \return pointer to the first element of the field override descriptors array.
 */
o42a_overrider_t *o42a_object_overriders(const o42a_sotype_t*);

/**
 * Retrieves object body corresponding to the given ascendant.
 *
 * \param ascendant[in] pointer to ascendant descriptor.
 *
 * \return body pointer.
 */
o42a_obj_t *o42a_ascendant_body(const o42a_ascendant_t*);

/**
 * Retrieves object body corresponding to the given sample.
 *
 * \param sample[in] pointer to ascendant descriptor.
 *
 * \return body pointer.
 */
o42a_obj_t *o42a_sample_body(const o42a_sample_t*);

/**
 * Retrieves field from body.
 *
 * \param body object body to retrieve field from.
 * \param field target field descriptor.
 *
 * \return field pointer.
 */
o42a_fld *o42a_field_fld(const o42a_obody_t*, const o42a_field_t*);

/**
 * Retrieves overriding field from body.
 *
 * \param field target field overrider descriptor.
 *
 * \return overriding field pointer..
 */
o42a_fld *o42a_overrider_fld(const o42a_overrider_t*);

/**
 * Searches for ascendant descriptor of the given type.
 *
 * \param data object data to search sample descriptor in.
 * \param type object type to search for.
 *
 * \return ascendant descriptor or NULL if not found.
 */
const o42a_ascendant_t *o42a_ascendant_of_type(
		const o42a_odata_t *const,
		const o42a_sotype_t *const);

/**
 * Searches for the object's body of the given type.
 *
 * \param object[in] object to cast.
 * \param type[in] static type to cast to.
 *
 * \return object body pointer corresponding to the given type or NULL if object
 * is not derived from type.
 */
o42a_obody_t *o42a_cast(o42a_obj_t*, const o42a_sotype_t*);

/**
 * Instantiates a new object.
 *
 * \param ctr[in] filled-in construction data.
 *
 * \return pointer to object's body of the sample type.
 */
o42a_obj_t *o42a_new(const o42a_ctr_t*);

/**
 * Object reference function, which always returns NULL.
 *
 * This can be used e.g. to refer void object ancestor.
 */
o42a_obody_t *o42a_null_object_ref(o42a_obj_t*);


#ifdef __cplusplus
}
#endif

#endif
