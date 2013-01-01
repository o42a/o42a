/*
    Copyright (C) 2012,2013 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#ifndef O42A_KPR_ARRAY_H
#define O42A_KPR_ARRAY_H

#include "o42a/field.h"


#ifdef __cplusplus
extern "C" {
#endif

typedef struct {

	O42A_HEADER

	o42a_val_t value;

} o42a_kpr_array;


#ifndef NDEBUG
extern const o42a_dbg_type_info1f_t _O42A_DEBUG_TYPE_o42a_kpr_array;
#endif /* NDEBUG */


void o42a_kpr_array_mark(o42a_fld *);

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* O42A_KPR_ARRAY_H */
