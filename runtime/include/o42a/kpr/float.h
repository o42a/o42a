/*
    Copyright (C) 2012,2013 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#ifndef O42A_KPR_FLOAT_H
#define O42A_KPR_FLOAT_H

#include "o42a/field.h"


#ifdef __cplusplus
extern "C" {
#endif

typedef struct {

	O42A_HEADER

	double value;

	uint8_t flags;

} o42a_kpr_float;


#ifndef NDEBUG
extern const o42a_dbg_type_info2f_t _O42A_DEBUG_TYPE_o42a_kpr_float;
#endif /* NDEBUG */


void o42a_kpr_float_derive(o42a_obj_ctable_t *);

o42a_bool_t o42a_kpr_float_is_init(const o42a_fld *);

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* O42A_KPR_FLOAT_H */
