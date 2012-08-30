/*
    Run-Time Library
    Copyright (C) 2010-2012 Ruslan Lopatin

    This file is part of o42a.

    o42a is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License
    as published by the Free Software Foundation, either version 3
    of the License, or (at your option) any later version.

    o42a is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
