/*
    Compiler Core
    Copyright (C) 2013,2014 Ruslan Lopatin

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

import static org.o42a.codegen.code.op.Atomicity.ATOMIC;
import static org.o42a.core.ir.value.Val.VAL_CONDITION;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.op.Int64op;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.value.ValOp;


public abstract class SimpleValueIR extends ValueIR {

	public SimpleValueIR(ValueTypeIR<?> valueTypeIR, ObjectIR objectIR) {
		super(valueTypeIR, objectIR);
	}

	@Override
	public ValueOp op(ObjectOp object) {
		return new SimpleValueOp(this, object);
	}

	private static final class SimpleValueOp extends DefaultValueOp {

		SimpleValueOp(ValueIR valueIR, ObjectOp object) {
			super(valueIR, object);
		}

		@Override
		public StateOp state() {
			return new SimpleStateOp(object());
		}

	}

	private static final class SimpleStateOp extends StateOp {

		SimpleStateOp(ObjectOp host) {
			super(host);
		}

		@Override
		public void init(Block code, ValOp value) {

			final Int64op target =
					value.value(null, code)
					.toInt64(null, code)
					.load(null, code);

			value().rawValue(null, code)
					.store(code, target, ATOMIC);

			code.releaseBarrier();

			flags().store(code, VAL_CONDITION);
		}

		@Override
		public void assign(CodeDirs dirs, ObjectOp value) {
			throw new UnsupportedOperationException();
		}

	}

}
