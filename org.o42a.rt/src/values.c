/*
    Run-Time Library
    Copyright (C) 2010 Ruslan Lopatin

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
#include "o42a/values.h"

#include "o42a/debug.h"
#include "o42a/object.h"


o42a_bool_t o42a_false(o42a_obj_t *const object) {
	O42A_ENTER;
	O42A_RETURN O42A_FALSE;
}

o42a_bool_t o42a_true(o42a_obj_t *const object) {
	O42A_ENTER;
	O42A_RETURN O42A_TRUE;
}

void o42a_false_val(o42a_val_t *const result, o42a_obj_t *const object) {
	O42A_ENTER;
	result->flags = O42A_FALSE;
	O42A_RETURN;
}

void o42a_unknown_val(o42a_val_t *const result, o42a_obj_t *const object) {
	O42A_ENTER;
	result->flags = O42A_UNKNOWN;
	O42A_RETURN;
}
