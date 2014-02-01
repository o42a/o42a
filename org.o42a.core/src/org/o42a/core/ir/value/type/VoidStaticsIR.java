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
package org.o42a.core.ir.value.type;

import org.o42a.codegen.data.Ptr;
import org.o42a.core.ir.value.Val;
import org.o42a.core.ir.value.ValType.Op;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.Void;


public class VoidStaticsIR<T> extends StaticsIR<T> {

	public VoidStaticsIR(ValueTypeIR<T> valueTypeIR) {
		super(valueTypeIR);
	}

	@Override
	public Val val(T value) {

		final Val voidValue = voidStaticsIR().val(Void.VOID);

		return new Val(
				getValueType(),
				voidValue.getFlags(),
				voidValue.getLength(),
				voidValue.getValue());
	}

	@Override
	public Ptr<Op> valPtr(T value) {
		return voidStaticsIR().valPtr(Void.VOID);
	}

	private StaticsIR<Void> voidStaticsIR() {
		return ValueType.VOID.ir(getGenerator()).staticsIR();
	}

}
