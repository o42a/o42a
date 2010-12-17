/*
    Compiler Code Generator
    Copyright (C) 2010 Ruslan Lopatin

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

import org.o42a.codegen.code.CodePtr;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.op.CodeOp;


public abstract class CodeRec<F extends Func>
		extends Rec<CodeOp<F>, CodePtr<F>> {

	private final Signature<F> signature;

	public CodeRec(
			String name,
			String id,
			Signature<F> signature,
			Content<CodeRec<F>> content) {
		super(name, id, content);
		this.signature = signature;
	}

	@Override
	public final DataType getDataType() {
		return DataType.CODE_PTR;
	}

	public final Signature<F> getSignature() {
		return this.signature;
	}

	public abstract void setNull();

}
