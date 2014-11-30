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

import static org.o42a.core.ir.value.Val.VAL_CONDITION;
import static org.o42a.core.ir.value.Val.VOID_VAL;

import org.o42a.codegen.code.Block;
import org.o42a.core.ir.object.ObjectDataIR;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.OpPresets;
import org.o42a.core.ir.value.Val;
import org.o42a.core.ir.value.ValOp;


public final class VoidValueIR extends ValueIR {

	public VoidValueIR(ValueTypeIR<?> valueTypeIR, ObjectIR objectIR) {
		super(valueTypeIR, objectIR);
	}

	@Override
	public OpPresets valuePresets(OpPresets presets) {
		return presets;
	}

	@Override
	public <H extends ObjectOp> ValueOp<H> op(H object) {
		return new VoidValueOp<>(this, object);
	}

	@Override
	public Val initialValue(ObjectDataIR dataIR) {
		return VOID_VAL;
	}

	private static final class VoidValueOp<H extends ObjectOp>
			extends DefaultValueOp<H> {

		VoidValueOp(ValueIR valueIR, H object) {
			super(valueIR, object);
		}

		@Override
		public StateOp<H> state() {
			return new VoidStateOp<>(object());
		}

	}

	private static final class VoidStateOp<H extends ObjectOp>
			extends StateOp<H> {

		VoidStateOp(H host) {
			super(host);
		}

		@Override
		public void init(Block code, ValOp value) {
			flags().store(code, VAL_CONDITION);
		}

		@Override
		public void assign(CodeDirs dirs, ObjectOp value) {
			throw new UnsupportedOperationException();
		}

	}

}
