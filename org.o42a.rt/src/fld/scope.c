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


void o42a_fld_scope_propagate(o42a_ctable_t *const ctable) {
	O42A_ENTER;

	o42a_fld *const to = ctable->to.fld;

	if (ctable->flags & O42A_CTR_FIELD_PROPAGATION) {
		// Update the scope only when propagating field.
		O42A_DEBUG("Field propagation\n");
		if (ctable->to.body->flags & O42A_OBODY_PROPAGATED) {
			// Update only the scope of body propagated from field
			// or the one of main body.

			o42a_otype_t *const owner_type =
					ctable->object_type->data.owner_type;

			if (owner_type) {
				to->scope = o42a_data_object(&owner_type->data);

				o42a_debug_mem_name("Updated scope: ", to->scope);

				O42A_RETURN;
			}

			O42A_DEBUG("Object is local\n");
		}
	} else {
		O42A_DEBUG("New object instantiation\n");
	}

	to->scope = ctable->from.fld->scope;

	o42a_debug_mem_name("Leave the scope unchanged: ", to->scope);

	O42A_RETURN;
}

void o42a_fld_scope_inherit(o42a_ctable_t *const ctable) {
	O42A_ENTER;

	const o42a_fld *const from = ctable->from.fld;
	o42a_fld *const to = ctable->to.fld;

	to->scope = from->scope;

	O42A_RETURN;
}
