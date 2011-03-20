/*
    Compiler Code Generator
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
package org.o42a.codegen.data;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.op.FuncOp;


public abstract class FuncRec<F extends Func>
		extends Rec<FuncOp<F>, FuncPtr<F>> {

	private final Signature<F> signature;

	public FuncRec(
			SubData<?> enclosing,
			CodeId id,
			Signature<F> signature,
			Content<FuncRec<F>> content) {
		super(enclosing, id, content);
		this.signature = signature;
	}

	@Override
	public final DataType getDataType() {
		if (this.signature.isDebuggable()) {
			return DataType.FUNC_PTR;
		}
		return DataType.CODE_PTR;
	}

	public final Signature<F> getSignature() {
		return this.signature;
	}

	public abstract void setNull();

}
