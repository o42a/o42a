/*
    Copyright (C) 2010-2014 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#include "o42a/lib/io.h"

#include "o42a/type/string.h"

#include "unicode/ustdio.h"


void o42a_io_print_str(const o42a_val_t *const val) {
	O42A_ENTER(return);

	const size_t len = val->length;

	if (!len) {
		O42A_RETURN;
	}

	const size_t ashift = O42A(o42a_val_ashift(val));
	const UChar32 cmask = O42A(o42a_str_cmask(val));
	const char *const str = O42A(o42a_val_data(val));

	UFILE *const uout = O42A(u_finit(stdout, NULL, NULL));

	for (size_t i = 0; i < len; ++i) {
		O42A(u_fputc(*((UChar32*) (str + (i << ashift))) & cmask, uout));
	}

	O42A(u_fclose(uout));

	O42A_RETURN;
}
