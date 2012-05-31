/*
    Run-Time Library
    Copyright (C) 2012 Ruslan Lopatin

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
#include "o42a/fields.h"


#ifndef NDEBUG
const o42a_dbg_type_info1f_t _O42A_DEBUG_TYPE_o42a_fld_getter = {
	.type_code = 0x042a0200 | O42A_FLD_GETTER,
	.field_num = 1,
	.name = "o42a_fld_getter",
	.fields = {
		{
			.data_type = O42A_TYPE_FUNC_PTR,
			.offset = offsetof(o42a_fld_getter, constructor),
			.name = "constructor",
		},
	},
};
#endif /* NDEBUG */

void o42a_fld_getter_propagate(o42a_obj_ctable_t *const ctable) {
	O42A_ENTER(return);

	const o42a_fld_getter *const from = &ctable->from.fld->getter;
	o42a_fld_getter *const to = &ctable->to.fld->getter;

	to->constructor = from->constructor;

	O42A_RETURN;
}

void o42a_fld_getter_inherit(o42a_obj_ctable_t *const ctable) {
	O42A_ENTER(return);

	const o42a_fld_getter *const from = &ctable->from.fld->getter;
	o42a_fld_getter *const to = &ctable->to.fld->getter;

	o42a_obj_overrider_t *const overrider = O42A(o42a_obj_field_overrider(
			ctable->sample_type,
			ctable->field));

	if (overrider) {// Field is overridden.
		if (!O42A(o42a_obj_ascendant_of_type(
				&ctable->ancestor_type->type.data,
				overrider->defined_in))) {
			// The body overrider defined in isn't present in ancestor
			// and thus not overridden there.
			// Use definition from overrider.
			to->constructor = O42A(o42a_fld_by_overrider(
					overrider)->getter.constructor);
			O42A_RETURN;
		}
	}

	// Use definition from ancestor.
	to->constructor = from->constructor;

	O42A_RETURN;
}
