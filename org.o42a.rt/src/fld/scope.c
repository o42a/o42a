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
#include "o42a/fields.h"


void o42a_fld_scope_propagate(o42a_obj_ctable_t *const ctable) {
	O42A_ENTER(return);

	const o42a_fld_scope *const from = &ctable->from.fld->scope;
	o42a_fld_scope *const to = &ctable->to.fld->scope;

	// Update the scope only when propagating field.
	if (ctable->to.body->flags & O42A_OBJ_BODY_PROPAGATED) {
		// Update only the scope of body propagated from field
		// or the one of main body.

		o42a_obj_type_t *const owner_type = ctable->owner_type;

		if (owner_type) {
			to->object =
					O42A(o42a_obj_by_data(&owner_type->type.data));

			o42a_debug_mem_name("Updated scope: ", to->object);

			O42A_RETURN;
		}

		O42A_DEBUG("Object is local\n");
	}

	to->object = from->object;

	o42a_debug_mem_name("Leave the scope unchanged: ", to->object);

	O42A_RETURN;
}

void o42a_fld_scope_inherit(o42a_obj_ctable_t *const ctable) {
	O42A_ENTER(return);

	const o42a_fld_scope *const from = &ctable->from.fld->scope;
	o42a_fld_scope *const to = &ctable->to.fld->scope;

	to->object = from->object;

	o42a_debug_mem_name("Leave the scope unchanged: ", from->object);

	O42A_RETURN;
}
