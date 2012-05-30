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
#ifndef O42A_FLD_LINK_H
#define O42A_FLD_LINK_H

#include "o42a/field.h"


#ifdef __cplusplus
extern "C" {
#endif

typedef struct {

	O42A_HEADER

	o42a_obj_t *object;

	o42a_obj_ref_ft *constructor;

} o42a_fld_link;


void o42a_fld_link_propagate(o42a_obj_ctable_t*);

void o42a_fld_link_inherit(o42a_obj_ctable_t*);


#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* O42A_FLD_LINK_H */
