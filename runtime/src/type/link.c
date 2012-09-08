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
#include "o42a/type/link.h"

#include "o42a/memory/gc.h"
#include "o42a/object.h"


static void o42a_val_mark_variable(o42a_obj_data_t *const data) {
	O42A_ENTER(return);

	volatile o42a_val_t *const value = &data->value;

	if (!(value->flags & O42A_VAL_CONDITION)) {
		O42A_RETURN;
	}

	o42a_obj_t *const object = value->value.v_ptr;

	if (object) {

		o42a_obj_data_t *const obj_data = &o42a_obj_type(object)->type.data;

		O42A(o42a_gc_mark(
				o42a_gc_blockof((char *) obj_data + obj_data->start)));
	}

	O42A_RETURN;
}

const o42a_val_type_t o42a_val_type_link = O42A_VAL_TYPE(
		"link",
		&o42a_val_mark_none,
		&o42a_val_sweep_none);

const o42a_val_type_t o42a_val_type_variable = O42A_VAL_TYPE(
		"variable",
		&o42a_val_mark_variable,
		&o42a_val_sweep_none);
