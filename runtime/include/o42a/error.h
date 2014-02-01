/*
    Copyright (C) 2011-2014 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#ifndef O42A_ERROR_H
#define O42A_ERROR_H

#include <stdio.h>

#include "o42a/types.h"
#include "o42a/value.h"


#ifdef __cplusplus
extern "C" {
#endif

inline void o42a_error_start() {
	fputs("[E] ", stderr);
}

inline void o42a_error_end() {
	fputc('\n', stderr);
}

void o42a_error_print_str(const o42a_val_t *);

void o42a_error_print(const char *);

__attribute__ ((format(printf, 1, 2)))
void o42a_error_printf(const char *, ...);

void o42a_error_append_str(const o42a_val_t *);

inline void o42a_error_append(const char *const message) {
	fputs(message, stderr);
}

__attribute__ ((format(printf, 1, 2)))
void o42a_error_appendf(const char *, ...);

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* O42A_ERROR_H */
