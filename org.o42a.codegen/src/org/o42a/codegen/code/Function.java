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
import org.o42a.codegen.code.backend.FuncWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Type;


public final class Function<F extends Func> extends Code {

	private final FunctionSettings settings;
	private final Signature<F> signature;
	private FuncWriter<F> writer;
	private FuncPtr<F> pointer;

	Function(
			FunctionSettings settings,
			CodeId id,
			Signature<F> signature) {
		super(settings.getGenerator(), id);
		this.settings = settings;
		this.signature = getGenerator().getFunctions().allocate(signature);
	}

	public final Signature<F> getSignature() {
		return this.signature;
	}

	public final boolean isExported() {
		return this.settings.isExported();
	}

	public final FuncPtr<F> getPointer() {
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

	public final <O extends StructOp> O ptrArg(
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

		final Functions functions = getGenerator().getFunctions();

		this.writer = getGenerator().getFunctions().codeBackend().addFunction(
				this,
				functions.createCodeCallback(this));
		this.pointer = new ConstructingFuncPtr<F>(this, this.writer.getAllocation());

		return this.writer;
	}

	@Override
	public String toString() {
		return getId().getId() + '(' + this.signature + ')';
	}

	@Override
	CodeId nestedId(CodeId name) {
		if (name != null) {
			return name;
		}
		return getGenerator().id().anonymous(++this.blockSeq);
	}

}
