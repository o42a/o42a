/*
    Run-Time Library
    Copyright (C) 2012 Ruslan Lopatin

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
const o42a_dbg_type_info2f_t _O42A_DEBUG_TYPE_o42a_kpr_float = {
	.type_code = 0x042a0200 | O42A_KPR_FLOAT,
	.field_num = 2,
	.name = "o42a_kpr_float",
	.fields = {
		{
			.data_type = O42A_TYPE_INT8,
			.offset = offsetof(o42a_kpr_float, flags),
			.name = "flags",
		},
		{
			.data_type = O42A_TYPE_FP64,
			.offset = offsetof(o42a_kpr_float, value),
			.name = "value",
		},
	},
};
#endif /* NDEBUG */

void o42a_kpr_float_derive(o42a_obj_ctable_t *const ctable) {
	O42A_ENTER(return);

	o42a_kpr_float *const to = &ctable->to.fld->kpr_float;

	to->flags = O42A_VAL_INDEFINITE;
	to->value = 0.0;

	O42A_RETURN;
}
