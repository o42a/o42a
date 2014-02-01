/*
    Copyright (C) 2012-2014 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#ifndef O42A_FLD_DEP_H
#define O42A_FLD_DEP_H

#include "o42a/field.h"


#ifdef __cplusplus
extern "C" {
#endif

typedef struct {

	O42A_HEADER

	o42a_obj_t *object;

} o42a_fld_dep;


#ifndef NDEBUG
extern const o42a_dbg_type_info1f_t _O42A_DEBUG_TYPE_o42a_fld_dep;
#endif /* NDEBUG */

void o42a_fld_dep_copy(o42a_obj_ctable_t*);


#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* O42A_FLD_DEP_H */
