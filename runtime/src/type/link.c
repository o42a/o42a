/*
    Copyright (C) 2012-2014 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#include "o42a/type/link.h"

#include "o42a/memory/gc.h"
#include "o42a/object.h"


static void use_link_val(const o42a_val_t *const from, o42a_val_t *const to) {
	O42A_ENTER(return);

	volatile const o42a_val_t *const f = from;

	to->flags = from->flags;
	to->value = from->value;
	O42A(o42a_obj_start_val_use(to));

	O42A_RETURN;
}

static void mark_link_val(o42a_obj_t *const object) {
	O42A_ENTER(return);

	const volatile o42a_val_t *const value = &object->object_data.value;
	o42a_obj_t *const target = value->value.v_ptr;

	if (!target) {
		O42A_RETURN;
	}

	O42A(o42a_gc_mark(o42a_gc_blockof(target)));

	O42A_RETURN;
}

const o42a_val_type_t o42a_val_type_link = O42A_VAL_TYPE(
		"link",
		use_link_val,
		o42a_obj_end_val_use,
		mark_link_val,
		o42a_val_gc_none);

const o42a_val_type_t o42a_val_type_variable = O42A_VAL_TYPE(
		"variable",
		use_link_val,
		o42a_obj_end_val_use,
		mark_link_val,
		o42a_val_gc_none);
