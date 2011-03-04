/*
    Run-Time Library
    Copyright (C) 2011 Ruslan Lopatin

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
#ifndef O42A_ERROR_H
#define O42A_ERROR_H

#include "o42a/types.h"


#ifdef __cplusplus
extern "C" {
#endif

void o42a_error_print_str(const o42a_val_t *);

void o42a_error_print(const char *);

__attribute__ ((format(printf, 1, 2)))
void o42a_error_printf(const char *, ...);


void o42a_error_start();

void o42a_error_append_str(const o42a_val_t *);

void o42a_error_append(const char *);

__attribute__ ((format(printf, 1, 2)))
void o42a_error_appendf(const char *, ...);

void o42a_error_end();


#ifdef __cplusplus
}
#endif

#endif /* O42A_ERROR_H */
