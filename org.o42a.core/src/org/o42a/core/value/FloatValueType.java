/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.core.value;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.value.AbstractValueTypeIR;
import org.o42a.core.ir.value.Val;
import org.o42a.core.ir.value.ValueTypeIR;
import org.o42a.core.source.Intrinsics;


final class FloatValueType extends ValueType<Double> {

	FloatValueType() {
		super("float", Double.class);
	}

	@Override
	public Obj wrapper(Intrinsics intrinsics) {
		return intrinsics.getFloat();
	}

	@Override
	protected ValueTypeIR<Double> createIR(Generator generator) {
		return new IR(generator, this);
	}

	private static final class IR extends AbstractValueTypeIR<Double> {

		private int constSeq;

		IR(Generator generator, ValueType<Double> valueType) {
			super(generator, valueType);
		}

		@Override
		public Val val(Double value) {
			return new Val(value);
		}

		@Override
		protected CodeId constId(Double value) {
			return getGenerator().id("CONST").sub("FLOAT")
			.anonymous(++this.constSeq);
		}

	}

}
