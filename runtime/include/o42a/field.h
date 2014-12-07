/*
    Copyright (C) 2010-2014 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#ifndef O42A_FIELD_H
#define O42A_FIELD_H

#include "o42a/object.h"


#ifdef __cplusplus
extern "C" {
#endif

/**
 * A union of all supported field kinds.
 */
typedef union o42a_fld o42a_fld;

/**
 * A field and keeper kind identifiers.
 */
enum o42a_fld_kind {

	O42A_FLD_OBJ = 0,

	O42A_FLD_ALIAS = 1,

	O42A_FLD_VAR = 2,

	O42A_FLD_OWNER = 3,

	O42A_FLD_DEP = 4,

	O42A_FLD_LOCAL = 5,

	O42A_FLD_LOCK = 6,

	O42A_FLD_RESUME_FROM = 7,

};


/**
 * Field copy function type.
 *
 * Such functions are called when constructing a new object.
 */
typedef void o42a_fld_copy_ft(o42a_obj_ctable_t *);

/**
 * Object reference function.
 *
 * \param object[in] scope object pointer.
 * \param vmtc[in] a pointer to VMT chain the object reference is invoked from.
 * This can be either an original VMT chain from the object, or one of
 * the links of that chain.
 *
 * \return resulting object pointer.
 */
typedef o42a_obj_t *o42a_obj_ref_ft(o42a_obj_t *, const o42a_obj_vmtc_t *);

/**
 * The descriptor of the field kind.
 */
typedef const struct o42a_fld_desc {

	/**
	 * This function is called for a constructing object to fill the field
	 * inherited from ancestor.
	 */
	o42a_fld_copy_ft *const inherit;

	/**
	 * This function is called for a constructing object to fill the field
	 * propagated from sample's main body.
	 */
	o42a_fld_copy_ft *const propagate;

	/**
	 * GC marker function pointer.
	 *
	 * This function is called when GC marks an object containing the field
	 * to mark the GC data referenced by this field.
	 *
	 * \param field marked field pointer.
	 */
	void (* mark) (o42a_fld *);

	/**
	 * GC sweep function pointer.
	 *
	 * This function is called when GC sweeps an object containing the field
	 * to sweep the GC data referenced by this field.
	 *
	 * \param field swept field pointer.
	 */
	void (* sweep) (o42a_fld *);

	/**
	 * A function, which checks whether the given field is already initialized.
	 *
	 * This function is used by o42a_fld_start() function to lock the field
	 * during initialization in order to prevent a race conditions.
	 *
	 * \param field the field to check.
	 */
	o42a_bool_t (* is_init) (const o42a_fld *);

} o42a_fld_desc_t;


#ifndef NDEBUG
extern const o42a_dbg_type_info5f_t _O42A_DEBUG_TYPE_o42a_fld_ctr;
#endif /* NDEBUG */

/**
 * Returns
 */
o42a_fld_desc_t *o42a_fld_desc(const o42a_obj_field_t *);

/**
 * Retrieves field from body.
 *
 * \param body object body to retrieve field from.
 * \param field target field descriptor.
 *
 * \return field pointer.
 */
inline o42a_fld *o42a_fld_by_field(
		const o42a_obj_t *const object,
		const o42a_obj_field_t *const field) {
	return (o42a_fld *) (((char *) object) + field->fld);
}

/**
 * Object reference function, which always returns NULL.
 *
 * This can be used e.g. to refer void object ancestor.
 */
o42a_obj_t *o42a_obj_ref_null(o42a_obj_t *, const o42a_obj_vmtc_t *);

/**
 * Object reference evaluation stub.
 */
o42a_obj_t *o42a_obj_ref_stub(o42a_obj_t *, const o42a_obj_vmtc_t *);


#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* O42A_FIELD_H */
