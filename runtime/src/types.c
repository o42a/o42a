/*
    Copyright (C) 2010-2014 Ruslan Lopatin

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
*/
#include "o42a/types.h"


extern size_t o42a_layout_size(o42a_layout_t);

extern uint8_t o42a_layout_ashift(o42a_layout_t);

extern uint8_t o42a_layout_alignment(o42a_layout_t);

extern size_t o42a_layout_offset(size_t, o42a_layout_t);

extern size_t o42a_layout_pad(size_t, o42a_layout_t);

extern size_t o42a_layout_array_size(o42a_layout_t, size_t);

extern o42a_layout_t o42a_layout_array(o42a_layout_t, size_t);

extern o42a_layout_t o42a_layout(uint8_t, size_t);

extern o42a_layout_t o42a_layout_both(o42a_layout_t, o42a_layout_t);
