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
#ifndef O42A_FLD_OBJ_H
#define O42A_FLD_OBJ_H

#include "o42a/field.h"


typedef struct o42a_fld_obj o42a_fld_obj;

struct o42a_fld_obj {

	O42A_HEADER;

	o42a_obj_t *object;

	o42a_obj_constructor_ft *constructor;

	o42a_fld_obj *previous;

};


#ifdef __cplusplus
extern "C" {
#endif


void o42a_fld_obj_propagate(o42a_obj_ctable_t*);

void o42a_fld_obj_inherit(o42a_obj_ctable_t*);


#ifdef __cplusplus
}
#endif

#endif /* O42A_FLD_OBJ_H */
