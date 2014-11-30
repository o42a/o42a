/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.core.value.array.impl;

import static org.o42a.codegen.code.op.Atomicity.ATOMIC;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Block;
import org.o42a.core.ir.object.ObjectDataIR;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.OpPresets;
import org.o42a.core.ir.value.Val;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.type.*;
import org.o42a.core.object.Obj;
import org.o42a.core.value.array.Array;
import org.o42a.core.value.array.ArrayValueType;


public final class ArrayValueTypeIR extends ValueTypeIR<Array> {

	public ArrayValueTypeIR(Generator generator, ArrayValueType valueType) {
		super(generator, valueType);
	}

	@Override
	public ValueIR valueIR(ObjectIR objectIR) {
		return new ArrayValueIR(this, objectIR);
	}

	@Override
	protected ArrayStaticsIR createStaticsIR() {
		return new ArrayStaticsIR(this);
	}

	private static final class ArrayValueIR extends ValueIR {

		ArrayValueIR(ArrayValueTypeIR valueStructIR, ObjectIR objectIR) {
			super(valueStructIR, objectIR);
		}

		@Override
		public OpPresets valuePresets(OpPresets presets) {
			return presets.setStackAllocationAllowed(false);
		}

		@Override
		public <H extends ObjectOp> ValueOp<H> op(H object) {
			if (!getValueType().isVariable()) {
				return new RowValueOp<>(this, object);
			}
			return new ArrayValueOp<>(this, object);
		}

		@Override
		public Val initialValue(ObjectDataIR dataIR) {

			final Obj object = dataIR.getObjectIR().getObject();
			final ArrayValueType arrayType = getValueType().toArrayType();
			final Array array =
					arrayType.cast(object.value().getValue())
					.getCompilerValue();

			return arrayType.ir(getGenerator()).staticsIR().val(array);
		}

	}

	private static final class RowValueOp<H extends ObjectOp>
			extends DefaultValueOp<H> {

		RowValueOp(ArrayValueIR valueIR, H object) {
			super(valueIR, object);
		}

		@Override
		public StateOp<H> state() {
			return new ArrayStateOp<>(object());
		}

	}

	private static final class ArrayValueOp<H extends ObjectOp>
			extends StatefulValueOp<H> {

		ArrayValueOp(ArrayValueIR valueIR, H object) {
			super(valueIR, object);
		}

		@Override
		public StateOp<H> state() {
			return new ArrayStateOp<>(object());
		}

	}

	private static final class ArrayStateOp<H extends ObjectOp>
			extends StateOp<H> {

		ArrayStateOp(H host) {
			super(host);
		}

		@Override
		public void init(Block code, ValOp value) {
			value().length(null, code).store(
					code,
					value.length(null, code).load(null, code),
					ATOMIC);
			value().rawValue(null, code).store(
					code,
					value.rawValue(null, code).load(null, code),
					ATOMIC);

			code.releaseBarrier();

			flags().store(code, value.flags(code).get());
		}

		@Override
		public void assign(CodeDirs dirs, ObjectOp value) {
			throw new UnsupportedOperationException();
		}

	}

}
