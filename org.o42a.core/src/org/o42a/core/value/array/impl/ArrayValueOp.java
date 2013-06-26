/*
    Compiler Core
    Copyright (C) 2012,2013 Ruslan Lopatin

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

import org.o42a.codegen.code.Block;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.type.StateOp;
import org.o42a.core.ir.value.type.StatefulValueOp;
import org.o42a.core.ir.value.type.ValueStateOp;


final class ArrayValueOp extends StatefulValueOp {

	ArrayValueOp(ArrayValueIR valueIR, ObjectOp object) {
		super(valueIR, object);
	}

	@Override
	public StateOp state(CodeDirs dirs) {
		return new ArrayStateOp(object());
	}

	private static final class ArrayStateOp extends ValueStateOp {

		ArrayStateOp(ObjectOp host) {
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
