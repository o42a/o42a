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

import static org.o42a.core.ir.value.Val.VOID_VAL;
import static org.o42a.core.ref.Ref.voidRef;

import org.o42a.codegen.Generator;
import org.o42a.codegen.data.Global;
import org.o42a.codegen.data.Ptr;
import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.artifact.common.Intrinsics;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.value.Val;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.ir.value.ValueTypeIR;
import org.o42a.core.ref.type.StaticTypeRef;


final class VoidValueType extends ValueType<Void> {

	VoidValueType() {
		super("void", Void.class);
	}

	@Override
	public Obj wrapper(Intrinsics intrinsics) {
		return intrinsics.getVoid();
	}

	@Override
	public StaticTypeRef typeRef(LocationInfo location, Scope scope) {
		return voidRef(location, scope.distribute()).toStaticTypeRef();
	}

	@Override
	public boolean assignableFrom(ValueType<?> other) {
		return true;
	}

	@Override
	protected ValueTypeIR<Void> createIR(Generator generator) {
		return new IR(generator, this);
	}

	private static final class IR extends ValueTypeIR<Void> {

		private Ptr<ValType.Op> valPtr;

		IR(Generator generator, ValueType<Void> valueType) {
			super(generator, valueType);
		}

		@Override
		public Val val(Void value) {
			return VOID_VAL;
		}

		@Override
		public Ptr<ValType.Op> valPtr(Void value) {
			if (this.valPtr != null) {
				return this.valPtr;
			}

			final Global<ValType.Op, ValType> global =
				getGenerator().newGlobal().setConstant()
				.dontExport().newInstance(
						getGenerator().id("CONST").sub("VOID"),
						ValType.VAL_TYPE,
						val(value));

			return this.valPtr = global.getPointer();
		}

	}

}
