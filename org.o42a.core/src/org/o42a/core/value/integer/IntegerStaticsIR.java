/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.core.value.integer;

import static org.o42a.core.ir.IRNames.CONST_ID;

import org.o42a.core.ir.value.Val;
import org.o42a.core.ir.value.type.CachingStaticsIR;
import org.o42a.core.ir.value.type.ValueTypeIR;
import org.o42a.util.string.ID;


final class IntegerStaticsIR extends CachingStaticsIR<Long> {

	IntegerStaticsIR(ValueTypeIR<Long> valueTypeIR) {
		super(valueTypeIR);
	}

	@Override
	public Val val(Long value) {
		return new Val(value);
	}

	@Override
	protected ID constId(Long value) {

		final String id;

		if (value == Long.MIN_VALUE) {
			id = "min";
		} else if (value < 0) {
			id = "n" + (-value);
		} else {
			id = Long.toString(value);
		}

		return CONST_ID.sub("INTEGER").sub(id);
	}

}
