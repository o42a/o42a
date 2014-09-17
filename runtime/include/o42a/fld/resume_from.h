/*
    Copyright (C) 2014 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#ifndef O42A_FLD_RESUME_FROM_H
#define O42A_FLD_RESUME_FROM_H

#include "o42a/field.h"

#ifdef __cplusplus
extern "C" {
#endif

typedef struct o42a_fld_resume_from o42a_fld_resume_from;

struct o42a_fld_resume_from {

	O42A_HEADER

	/**
	 * Value evaluation resume pointer.
	 *
	 * It is a position inside the object's value definition function (def_f).
     *
	 * This value is updated by the yield statement. The next time the value is
	 * requested, the evaluation starts from this position.
	 *
	 * Always NULL for newly constructed objects. In this case the value
	 * evaluation starts from the beginning.
	 *
	 * It is an instance field. I.e. there is at most one such field exists
	 * in object.
	 */
	void *resume_ptr;

};


#ifndef NDEBUG

extern const o42a_dbg_type_info1f_t _O42A_DEBUG_TYPE_o42a_fld_resume_from;

#endif /* NDEBUG */

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* O42A_FLD_RESUME_FROM_H */
