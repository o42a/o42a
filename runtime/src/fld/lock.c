/*
    Copyright (C) 2014 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#include "o42a/fields.h"

void o42a_fld_lock_init(o42a_obj_ctable_t *const ctable) {
	O42A_ENTER(return);
	O42A(o42a_obj_lock_init(&ctable->to_fld->lock));
	O42A_RETURN;
}
