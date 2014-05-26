/*
    Copyright (C) 2014 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#ifndef O42A_FLD_ALIAS_H
#define O42A_FLD_ALIAS_H

#include "o42a/field.h"


#ifdef __cplusplus
extern "C" {
#endif

typedef struct {

	O42A_HEADER

	o42a_obj_t *object;

	o42a_obj_ref_ft *constructor;

} o42a_fld_alias;


#ifndef NDEBUG
extern const o42a_dbg_type_info2f_t _O42A_DEBUG_TYPE_o42a_fld_alias;
#endif /* NDEBUG */


#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* O42A_FLD_ALIAS_H */
