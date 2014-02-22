/*
    Copyright (C) 2012-2014 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#include "o42a/type/link.h"

#include "o42a/memory/gc.h"
#include "o42a/object.h"


static void mark_link_val(o42a_obj_data_t *const data) {
	O42A_ENTER(return);

	const volatile o42a_val_t *const value = &data->value;
	o42a_obj_t *const target = value->value.v_ptr;

	if (!target) {
		O42A_RETURN;
	}

	o42a_obj_data_t *const tdata = O42A(o42a_obj_data(target));

	O42A(o42a_gc_mark(o42a_gc_blockof((char *) tdata + tdata->start)));

	O42A_RETURN;
}

const o42a_val_type_t o42a_val_type_link = O42A_VAL_TYPE(
		"link",
		mark_link_val,
		o42a_val_gc_none);

const o42a_val_type_t o42a_val_type_variable = O42A_VAL_TYPE(
		"variable",
		mark_link_val,
		o42a_val_gc_none);
