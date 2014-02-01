/*
    Copyright (C) 2010-2014 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#ifndef O42A_TYPE_STRING_H
#define O42A_TYPE_STRING_H

#include "o42a/types.h"
#include "o42a/value.h"

#include "unicode/utypes.h"


#ifdef __cplusplus
extern "C" {
#endif


/**
 * String value type descriptor.
 */
extern const o42a_val_type_t o42a_val_type_string;

size_t o42a_str_len(const o42a_val_t *);

UChar32 o42a_str_cmask(const o42a_val_t *);

void o42a_str_sub(
		o42a_val_t *,
		const o42a_val_t *,
		int64_t,
		int64_t);

int64_t o42a_str_compare(const o42a_val_t *, const o42a_val_t *);

void o42a_str_concat(o42a_val_t *, const o42a_val_t *, const o42a_val_t *);


#ifdef __cplusplus
}
#endif

#endif /* O42A_TYPE_STRING_H */
