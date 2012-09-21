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
package org.o42a.core.ir.value.array;

import static org.o42a.codegen.code.op.Atomicity.ATOMIC;
import static org.o42a.core.ir.field.array.ArraySte.arraySteKey;
import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.core.ir.field.array.ArraySteOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValFlagsOp;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.ir.value.struct.StateOp;
import org.o42a.core.ir.value.struct.StatefulValueOp;


final class ArrayValueOp extends StatefulValueOp {

	ArrayValueOp(ArrayValueIR valueIR, ObjectOp object) {
		super(valueIR, object);
	}

	@Override
	public StateOp state(CodeDirs dirs) {

		final ArraySteOp fld = (ArraySteOp) object().field(
				dirs,
				arraySteKey(getBuilder().getContext()));

		return new ArrayStateOp(fld);
	}

	private static final class ArrayStateOp extends StateOp {

		private ValType.Op value;
		private ValFlagsOp flags;

		ArrayStateOp(ArraySteOp fld) {
			super(fld);
		}

		public final ArraySteOp array() {
			return (ArraySteOp) fld();
		}

		@Override
		public void useByValueFunction(Code code) {
			this.value = array().ptr().value(null, code);
			this.flags = this.value.flags(code, ATOMIC);
		}

		@Override
		public BoolOp loadCondition(Code code) {
			return this.flags.condition(null, code);
		}

		@Override
		public ValOp loadValue(ValDirs dirs, Code code) {
			return this.value.op(
					code.getAllocator(),
					dirs.getBuilder(),
					getValueStruct(),
					TEMP_VAL_HOLDER);
		}

		@Override
		public void init(Block code, ValOp value) {
			this.value.length(null, code).store(
					code,
					value.length(null, code).load(null, code),
					ATOMIC);
			this.value.rawValue(null, code).store(
					code,
					value.rawValue(null, code).load(null, code),
					ATOMIC);

			code.releaseBarrier();

			this.flags.store(code, value.flags(code).get());
			this.value.useRefCounted(code);

		}

		@Override
		public void initToFalse(Block code) {
			code.releaseBarrier();
			this.flags.storeFalse(code);
		}

		@Override
		public void assign(CodeDirs dirs, ObjectOp value) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected BoolOp loadIndefinite(Code code) {
			return this.flags.indefinite(null, code);
		}

		@Override
		protected void start(Block code) {
			this.value = array().ptr().value(null, code);
			super.start(code);
			this.flags = this.value.flags(code, ATOMIC);
		}

	}

}
