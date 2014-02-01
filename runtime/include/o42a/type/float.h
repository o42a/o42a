/*
    Copyright (C) 2011-2014 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#ifndef O42A_TYPE_FLOAT_H
#define O42A_TYPE_FLOAT_H

#include "o42a/types.h"
#include "o42a/value.h"


#ifdef __cplusplus
extern "C" {
#endif

/**
 * Float value type descriptor.
 */
extern const o42a_val_type_t o42a_val_type_float;

void o42a_float_by_str(o42a_val_t *, const o42a_val_t *);

int o42a_float_error(o42a_val_t *);

o42a_bool_t o42a_float_to_str(o42a_val_t *, double);


#ifdef __cplusplus
}
#endif

#endif /* O42A_TYPE_FLOAT_H */
