/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.object.macro.impl;

import org.o42a.codegen.Generator;
import org.o42a.codegen.data.Ptr;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.value.Val;
import org.o42a.core.ir.value.ValType.Op;
import org.o42a.core.ir.value.struct.SingleValueStructIR;
import org.o42a.core.ir.value.struct.ValueIR;
import org.o42a.core.ir.value.struct.ValueStructIR;
import org.o42a.core.object.macro.Macro;
import org.o42a.core.value.SingleValueStruct;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.Void;


public class MacroValueStruct extends SingleValueStruct<Macro> {

	public static final MacroValueStruct INSTANCE = new MacroValueStruct();

	private MacroValueStruct() {
		super(MacroValueType.INSTANCE, Macro.class);
	}

	@Override
	protected ValueStructIR<SingleValueStruct<Macro>, Macro> createIR(
			Generator generator) {
		return new MacroIR(generator, this);
	}

	private static final class MacroIR extends SingleValueStructIR<Macro> {

		MacroIR(Generator generator, MacroValueStruct valueStruct) {
			super(generator, valueStruct);
		}

		@Override
		public boolean hasValue() {
			return false;
		}

		@Override
		public Val val(Macro value) {

			final Val voidValue =
					ValueStruct.VOID.ir(getGenerator()).val(Void.VOID);

			return new Val(
					getValueStruct(),
					voidValue.getFlags(),
					voidValue.getLength(),
					voidValue.getValue());
		}

		@Override
		public Ptr<Op> valPtr(Macro value) {
			return ValueStruct.VOID.ir(getGenerator()).valPtr(Void.VOID);
		}

		@Override
		public ValueIR valueIR(ObjectIR objectIR) {
			return defaultValueIR(objectIR);
		}

	}

}
