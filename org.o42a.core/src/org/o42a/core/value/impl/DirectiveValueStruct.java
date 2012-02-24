/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.core.value.impl;

import org.o42a.codegen.Generator;
import org.o42a.codegen.data.Ptr;
import org.o42a.core.ir.value.Val;
import org.o42a.core.ir.value.ValType.Op;
import org.o42a.core.ir.value.struct.ValueStructIR;
import org.o42a.core.value.*;
import org.o42a.core.value.Void;


public class DirectiveValueStruct extends SingleValueStruct<Directive> {

	public static final DirectiveValueStruct INSTANCE =
			new DirectiveValueStruct();

	private DirectiveValueStruct() {
		super(ValueType.DIRECTIVE, Directive.class);
	}

	@Override
	protected ValueStructIR<SingleValueStruct<Directive>, Directive> createIR(
			Generator generator) {
		return new IR(generator, this);
	}

	private static final class IR
			extends ValueStructIR<SingleValueStruct<Directive>, Directive> {

		IR(Generator generator, DirectiveValueStruct valueStruct) {
			super(generator, valueStruct);
		}

		@Override
		public boolean hasValue() {
			return false;
		}

		@Override
		public Val val(Directive value) {

			final Val voidValue =
					ValueStruct.VOID.ir(getGenerator()).val(Void.VOID);

			return new Val(
					getValueStruct(),
					voidValue.getFlags(),
					voidValue.getLength(),
					voidValue.getValue());
		}

		@Override
		public Ptr<Op> valPtr(Directive value) {
			return ValueStruct.VOID.ir(getGenerator()).valPtr(Void.VOID);
		}

	}

}
