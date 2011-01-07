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

import org.o42a.codegen.code.CodePtr.FuncPtr;
import org.o42a.codegen.code.backend.FuncWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Type;


public final class Function<F extends Func> extends Code {

	private final FunctionSettings settings;
	private final Signature<F> signature;
	private FuncWriter<F> writer;
	private CodePtr<F> pointer;

	Function(
			FunctionSettings settings,
			String name,
			Signature<F> signature) {
		super(settings.getGenerator(), name);
		this.settings = settings;
		this.signature = signature.allocate(backend());
	}

	public final Signature<F> getSignature() {
		return this.signature;
	}

	public final boolean isExported() {
		return this.settings.isExported();
	}

	public final CodePtr<F> getPointer() {
		writer();// init writer
		return this.pointer;
	}

	@Override
	public final boolean exists() {
		return this.writer != null;
	}

	public final Int32op int32arg(Code code, int index) {
		return writer().int32arg(index);
	}

	public final Int64op int64arg(Code code, int index) {
		return writer().int64arg(index);
	}

	public final Fp64op fp64arg(Code code, int index) {
		return writer().fp64arg(index);
	}

	public final BoolOp boolArg(Code code, int index) {
		return writer().boolArg(code, index);
	}

	public final RelOp relPtrArg(Code code, int index) {
		return writer().relPtrArg(code, index);
	}

	public final AnyOp ptrArg(Code code, int index) {
		return writer().ptrArg(code, index);
	}

	public final <O extends PtrOp> O ptrArg(
			Code code,
			int index,
			Type<O> type) {
		return writer().ptrArg(code, index, type);
	}

	@Override
	public final FuncWriter<F> writer() {
		if (this.writer != null) {
			return this.writer;
		}

		final Functions functions = generator();

		this.writer = backend().addFunction(
				this,
				functions.createCodeCallback(this));
		this.pointer = new FuncPtr<F>(this, this.writer.getAllocation());

		return this.writer;
	}

	@Override
	public String toString() {
		return getName() + '(' + this.signature + ')';
	}

}
