/*
    Copyright (C) 2012 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#ifndef O42A_KPR_INTEGER_H
#define O42A_KPR_INTEGER_H

#include "o42a/field.h"


#ifdef __cplusplus
extern "C" {
#endif

typedef struct {

	O42A_HEADER

	int64_t value;

	uint8_t flags;

} o42a_kpr_integer;


#ifndef NDEBUG
extern const o42a_dbg_type_info2f_t _O42A_DEBUG_TYPE_o42a_kpr_integer;
#endif /* NDEBUG */


void o42a_kpr_integer_derive(o42a_obj_ctable_t *);

o42a_bool_t o42a_kpr_integer_is_init(const o42a_fld *);

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* O42A_KPR_INTEGER_H */
