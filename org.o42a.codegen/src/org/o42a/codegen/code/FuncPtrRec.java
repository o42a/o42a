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
package org.o42a.codegen.code;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.data.FuncRec;
import org.o42a.codegen.data.Content;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;


final class FuncPtrRec<F extends Func> extends FuncRec<F> {

	FuncPtrRec(
			SubData<?> enclosing,
			CodeId id,
			Signature<F> signature,
			Content<FuncRec<F>> content) {
		super(enclosing, id, signature, content);
	}

	@Override
	public void setNull() {
		setValue(new CodePtr.NullPtr<F>(
				getSignature(),
				getGenerator().dataWriter().nullPtr(getSignature())));
	}

	@Override
	protected void allocate(DataAllocator allocator) {
		setAllocation(allocator.allocateCodePtr(
				getEnclosing().pointer(getGenerator()).getAllocation(),
				getAllocation(),
				getSignature()));
	}

	@Override
	protected void write(DataWriter writer) {
		getValue().getAllocation().write(writer);
	}

}
