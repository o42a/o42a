/*
    Run-Time Library
    Copyright (C) 2011,2012 Ruslan Lopatin

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
#ifndef O42A_INTEGER_H
#define O42A_INTEGER_H

#include "o42a/types.h"


#ifdef __cplusplus
extern "C" {
#endif


void o42a_int_by_str(O42A_DECLS o42a_val_t *, const o42a_val_t *, uint32_t);

o42a_bool_t o42a_int_to_str(O42A_DECLS o42a_val_t *, int64_t);


#ifdef __cplusplus
}
#endif

#endif /* O42A_INTEGER_H */
