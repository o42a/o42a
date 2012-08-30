/*
    Run-Time Library
    Copyright (C) 2010-2012 Ruslan Lopatin

    This file is part of o42a.

    o42a is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License
    as published by the Free Software Foundation, either version 3
    of the License, or (at your option) any later version.

    o42a is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
#include "o42a/fields.h"


#ifndef NDEBUG
const o42a_dbg_type_info2f_t _O42A_DEBUG_TYPE_o42a_fld_link = {
	.type_code = 0x042a0200 | O42A_FLD_LINK,
	.field_num = 2,
	.name = "o42a_fld_link",
	.fields = {
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_fld_link, object),
			.name = "object",
		},
		{
			.data_type = O42A_TYPE_FUNC_PTR,
			.offset = offsetof(o42a_fld_link, constructor),
			.name = "constructor",
		},
	},
};
#endif /* NDEBUG */

void o42a_fld_link_propagate(o42a_obj_ctable_t *const ctable) {
	O42A_ENTER(return);

	const o42a_fld_link *const from = &ctable->from.fld->link;
	o42a_fld_link *const to = &ctable->to.fld->link;

	to->object = NULL;
	to->constructor = from->constructor;

	O42A_RETURN;
}

void o42a_fld_link_inherit(o42a_obj_ctable_t *const ctable) {
	O42A_ENTER(return);

	const o42a_fld_link *const from = &ctable->from.fld->link;
	o42a_fld_link *const to = &ctable->to.fld->link;

	to->object = NULL;

	o42a_obj_overrider_t *const overrider =
			O42A(o42a_obj_field_overrider(ctable->sample_type, ctable->field));

	if (overrider) {// Field is overridden.
		if (!O42A(o42a_obj_ascendant_of_type(
				&ctable->ancestor_type->type.data,
				overrider->defined_in))) {
			// The body overrider defined in isn't present in ancestor
			// and thus not overridden there.
			// Use definition from overrider.
			to->constructor =
					O42A(o42a_fld_by_overrider(overrider)->link.constructor);
			O42A_RETURN;
		}
	}

	// Use definition from ancestor.
	to->constructor = from->constructor;

	O42A_RETURN;
}
