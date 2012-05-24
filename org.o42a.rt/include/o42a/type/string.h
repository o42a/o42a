/*
    Run-Time Library
    Copyright (C) 2010-2012 Ruslan Lopatin

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
#ifndef O42A_TYPE_STRING_H
#define O42A_TYPE_STRING_H

#include "o42a/types.h"
#include "o42a/value.h"

#include "unicode/utypes.h"


#ifdef __cplusplus
extern "C" {
#endif


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
