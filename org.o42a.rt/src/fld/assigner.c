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

#include "o42a/memory/gc.h"


#ifndef NDEBUG
const o42a_dbg_type_info2f_t _O42A_DEBUG_TYPE_o42a_fld_assigner = {
	.type_code = 0x042a0200 | O42A_FLD_ASSIGNER,
	.field_num = 2,
	.name = "o42a_fld_assigner",
	.fields = {
		{
			.data_type = O42A_TYPE_DATA_PTR,
			.offset = offsetof(o42a_fld_assigner, bound),
			.name = "bound",
		},
		{
			.data_type = O42A_TYPE_FUNC_PTR,
			.offset = offsetof(o42a_fld_assigner, assigner_f),
			.name = "assigner_f",
		},
	},
};
#endif /* NDEBUG */

void o42a_fld_assigner_propagate(o42a_obj_ctable_t *const ctable) {
	O42A_ENTER(return);

	const o42a_fld_assigner *const from = &ctable->from.fld->assigner;
	o42a_fld_assigner *const to = &ctable->to.fld->assigner;

	to->bound = NULL;
	to->assigner_f = from->assigner_f;

	O42A_RETURN;
}

void o42a_fld_assigner_inherit(o42a_obj_ctable_t *const ctable) {
	O42A_ENTER(return);

	const o42a_fld_assigner *const from = &ctable->from.fld->assigner;
	o42a_fld_assigner *const to = &ctable->to.fld->assigner;

	to->bound = NULL;

	o42a_obj_overrider_t *const overrider =
			O42A(o42a_obj_field_overrider(ctable->sample_type, ctable->field));

	if (overrider) {// Field is overridden.
		if (!O42A(o42a_obj_ascendant_of_type(
				&ctable->ancestor_type->type.data,
				overrider->defined_in))) {
			// The body overrider defined isn't present in ancestor
			// and thus not overridden there.
			// Use definition from overrider.

			const o42a_fld_var *const ovr =
					O42A(&o42a_fld_by_overrider(overrider)->var);

			to->assigner_f = ovr->assigner_f;

			O42A_RETURN;
		}
	}

	// Use definition from ancestor.
	to->assigner_f = from->assigner_f;

	O42A_RETURN;
}

void o42a_fld_assigner_mark(o42a_fld *const field) {
	O42A_ENTER(return);

	volatile o42a_fld_assigner *const fld = &field->assigner;
	o42a_obj_stype_t *const bound = fld->bound;

	if (bound) {

		o42a_obj_data_t *const data = &bound->data;

		O42A(o42a_gc_mark(o42a_gc_blockof(((char *) data) + data->start)));
	}

	O42A_RETURN;
}
