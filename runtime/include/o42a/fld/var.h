/*
    Copyright (C) 2010-2014 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#ifndef O42A_FLD_VAR_H
#define O42A_FLD_VAR_H

#include "o42a/field.h"


#ifdef __cplusplus
extern "C" {
#endif

typedef struct {

	O42A_HEADER

	o42a_obj_t *object;

	o42a_obj_ref_ft *constructor;

} o42a_fld_var;


#ifndef NDEBUG
extern const o42a_dbg_type_info2f_t _O42A_DEBUG_TYPE_o42a_fld_var;
#endif /* NDEBUG */

void o42a_fld_var_propagate(o42a_obj_ctable_t*);

void o42a_fld_var_inherit(o42a_obj_ctable_t*);

void o42a_fld_var_mark(o42a_fld *);


#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* O42A_FLD_VAR_H */
