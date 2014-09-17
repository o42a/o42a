/*
    Copyright (C) 2014 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#include "o42a/fields.h"

#ifndef NDEBUG
const o42a_dbg_type_info1f_t _O42A_DEBUG_TYPE_o42a_fld_resume_from = {
	.type_code = 0x042a0200 | O42A_FLD_RESUME_FROM,
	.field_num = 1,
	.name = "o42a_fld_resume_from",
	.fields = {
		{
			.data_type = O42A_TYPE_PTR,
			.offset = offsetof(o42a_fld_resume_from, resume_ptr),
			.name = "resume_ptr",
		},
	},
};
#endif /* NDEBUG */
