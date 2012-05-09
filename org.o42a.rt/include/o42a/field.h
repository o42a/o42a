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
#ifndef O42A_FIELD_H
#define O42A_FIELD_H

#include "o42a/object.h"


/**
 * A union of all supported field kinds.
 */
typedef union o42a_fld o42a_fld;

/**
 * A field kind identifiers.
 */
enum o42a_fld_kind {

	O42A_FLD_OBJ = 0,

	O42A_FLD_LINK = 1,

	O42A_FLD_VAR = 2,

	O42A_FLD_GETTER = 3,

	O42A_FLD_SCOPE = 4,

	O42A_FLD_DEP = 5,

	O42A_FLD_ASSIGNER = 6,

};


#ifdef __cplusplus
extern "C" {
#endif

/**
 * Field copy function type.
 *
 * Such functions are called when constructing a new object.
 */
typedef void o42a_fld_copy_ft(O42A_DECLS o42a_obj_ctable_t *);

/**
 * Variable assigner function.
 *
 * \param object[in] object pointer.
 * \param value[out] value to assign.
 *
 * \return O42A_TRUE on success or O42A_FALSE on failure.
 */
typedef o42a_bool_t o42a_obj_assigner_ft(O42A_DECLS o42a_obj_t *, o42a_obj_t *);

#ifdef __cplusplus
} /* extern "C" */
#endif


/**
 * The descriptor of the field kind.
 */
typedef const struct o42a_fld_desc {

	O42A_HEADER;

	/**
	 * This function is called for a constructing object to fill the field
	 * inherited from ancestor.
	 */
	o42a_fld_copy_ft *const inherit;

	/**
	 * This function is called for a constructing object to fill the field
	 * propagated from sample.
	 */
	o42a_fld_copy_ft *const propagate;

} o42a_fld_desc_t;


#ifdef __cplusplus
extern "C" {
#endif


/**
 * Returns
 */
o42a_fld_desc_t *o42a_fld_desc(O42A_DECLS const o42a_obj_field_t *);

/**
 * Retrieves field from body.
 *
 * \param body object body to retrieve field from.
 * \param field target field descriptor.
 *
 * \return field pointer.
 */
o42a_fld *o42a_fld_by_field(
		O42A_DECLS
		const o42a_obj_body_t *,
		const o42a_obj_field_t *);

/**
 * Retrieves overriding field from body.
 *
 * \param field target field overrider descriptor.
 *
 * \return overriding field pointer..
 */
o42a_fld *o42a_fld_by_overrider(O42A_DECLS const o42a_obj_overrider_t *);


#ifdef __cplusplus
} /* extern "C" */
#endif


#endif /* O42A_FIELD_H */
