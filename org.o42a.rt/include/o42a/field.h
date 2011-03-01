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
#ifndef o42a_obj_field_H
#define o42a_obj_field_H

#include "o42a/types.h"


enum o42a_obj_field_kind {

	O42A_FLD_OBJ = 0,

	O42A_FLD_LINK = 1,

	O42A_FLD_VAR = 2,

	O42A_FLD_ARRAY = 3,

	O42A_FLD_SCOPE = 4,

	O42A_FLD_DEP = 5,

};


struct o42a_obj_ctable {

	o42a_obj_type_t *const ancestor_type;

	o42a_obj_stype_t *const sample_type;

	o42a_obj_rtype_t *const object_type;

	o42a_obj_stype_t *body_type;

	o42a_obj_field_t *field;

	struct o42a_cside {

		o42a_obj_body_t *body;

		o42a_fld *fld;

	} from;

	struct o42a_cside to;

	uint32_t flags;

};

typedef void o42a_fcopy_ft(o42a_obj_ctable_t *);

typedef const struct o42a_fld_desc {

	o42a_fcopy_ft *const inherit;

	o42a_fcopy_ft *const propagate;

} o42a_fld_desc_t;


#ifdef __cplusplus
extern "C" {
#endif


o42a_fld_desc_t *o42a_obj_field_desc(const o42a_obj_field_t*);

o42a_obj_overrider_t *o42a_obj_field_overrider(
		const o42a_obj_stype_t*,
		const o42a_obj_field_t*);


#ifdef __cplusplus
}
#endif


#endif
