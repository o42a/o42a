/*
    Run-Time Library
    Copyright (C) 2010,2011 Ruslan Lopatin

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
#include "o42a/lib/io.h"

#include "o42a/string.h"

#include "unicode/ustdio.h"


void o42a_io_print_str(const o42a_val_t *const val) {
	O42A_ENTER;

	const size_t len = val->length;

	if (!len) {
		O42A_RETURN;
	}

	const size_t step = o42a_val_alignment(val);
	const UChar32 cmask = o42a_str_cmask(val);
	const void *const str = o42a_val_data(val);

	UFILE *const uout = u_finit(stdout, NULL, NULL);

	for (size_t i = 0; i < len; i += step) {
		u_fputc(*((UChar32*) (str + i)) & cmask, uout);
	}

	u_fclose(uout);

	O42A_RETURN;
}
