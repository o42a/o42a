/*
    Copyright (C) 2011-2014 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#ifndef O42A_TYPE_INTEGER_H
#define O42A_TYPE_INTEGER_H

#include "o42a/types.h"
#include "o42a/value.h"


#ifdef __cplusplus
extern "C" {
#endif

extern const o42a_val_type_t o42a_val_type_integer;

void o42a_int_by_str(o42a_val_t *, const o42a_val_t *, uint32_t);

o42a_bool_t o42a_int_to_str(o42a_val_t *, int64_t);


#ifdef __cplusplus
}
#endif

#endif /* O42A_TYPE_INTEGER_H */
