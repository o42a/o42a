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
#include "o42a/fields.h"

#include "o42a/debug.h"
#include "o42a/object.h"


static const o42a_fld_desc_t o42a_obj_field_kinds[] = {
	{// object field
		propagate: &o42a_fld_obj_propagate,
		inherit: &o42a_fld_obj_inherit,
	},
	{// link field
		propagate: &o42a_fld_link_propagate,
		inherit: &o42a_fld_link_inherit,
	},
	{// variable field
		propagate: &o42a_fld_var_propagate,
		inherit: &o42a_fld_var_inherit,
	},
	{// array field
		propagate: NULL,
		inherit: NULL,
	},
	{// scope object pointer
		propagate: &o42a_fld_scope_propagate,
		inherit: &o42a_fld_scope_inherit,
	},
	{// dependency field
		propagate: NULL,
		inherit: NULL,
	},
};


inline o42a_fld_desc_t *o42a_obj_field_desc(const o42a_obj_field_t *const field) {
	return &o42a_obj_field_kinds[field->kind];
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
