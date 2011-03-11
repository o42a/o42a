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


void o42a_fld_obj_propagate(o42a_obj_ctable_t *const ctable) {
	O42A_ENTER;

	const o42a_fld_obj *const from = &ctable->from.fld->obj;
	o42a_fld_obj *const to = &ctable->to.fld->obj;

	to->object = NULL;
	to->constructor = from->constructor;
	to->previous = from->previous;

	O42A_RETURN;
}

void o42a_fld_obj_inherit(o42a_obj_ctable_t *const ctable) {
	O42A_ENTER;

	o42a_fld_obj *const from = &ctable->from.fld->obj;
	o42a_fld_obj *const to = &ctable->to.fld->obj;

	to->object = NULL;

	o42a_obj_overrider_t *const overrider =
			o42a_obj_field_overrider(ctable->sample_type, ctable->field);

	if (overrider) {// Field is overridden.
		if (!o42a_obj_ascendant_of_type(
				&ctable->ancestor_type->data,
				overrider->defined_in)) {
			// The body overrider defined in isn't present in ancestor
			// and thus not overridden there.
			// Use definition from overrider.
			to->constructor = o42a_fld_by_overrider(overrider)->obj.constructor;
			// Store pointer to previous definition.
			to->previous = from;
			O42A_RETURN;
		}
	}

	// Use definition from ancestor.
	to->constructor = from->constructor;
	to->previous = from->previous;

	O42A_RETURN;
}
