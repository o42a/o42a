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


inline o42a_fld_desc_t *o42a_fld_desc(const o42a_obj_field_t *const field) {
	return &o42a_obj_field_kinds[field->kind];
}

inline o42a_fld *o42a_fld_by_field(
		const o42a_obj_body_t *const body,
		const o42a_obj_field_t *const field) {
	return (o42a_fld*) (((void*) body) + field->fld);
}

inline o42a_fld *o42a_fld_by_overrider(
		const o42a_obj_overrider_t *const overrider) {

	void *const body = ((void*) overrider) + overrider->body;

	return (o42a_fld*) (body + overrider->field->fld);
}
