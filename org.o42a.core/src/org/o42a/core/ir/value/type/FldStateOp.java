/*
    Compiler Core
    Copyright (C) 2013 Ruslan Lopatin

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

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.field.object.FldCtrOp;
import org.o42a.core.value.ValueType;


public abstract class FldStateOp extends StateOp {

	private final FldOp<?> fld;

	public FldStateOp(FldOp<?> fld) {
		super(fld.host());
		this.fld = fld;
	}

	public final FldOp<?> fld() {
		return this.fld;
	}

	@Override
	public final ValueType<?> getValueType() {
		return fld()
				.fld()
				.getBodyIR()
				.getObjectIR()
				.getObject()
				.type()
				.getValueType();
	}

	@Override
	public void startEval(Block code, CodePos failure, FldCtrOp ctr) {
		initEval(code);
		ctr.start(code, fld()).goUnless(code, failure);
	}

	protected abstract void initEval(Code code);

}
