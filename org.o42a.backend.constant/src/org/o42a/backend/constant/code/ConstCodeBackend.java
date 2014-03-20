/*
    Constant Handler Compiler Back-end
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.backend.constant.code;

import static org.o42a.backend.constant.data.ConstBackend.cast;

import org.o42a.backend.constant.code.signature.CSignatureWriter;
import org.o42a.backend.constant.data.*;
import org.o42a.backend.constant.data.func.ExternCFAlloc;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.*;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.data.Ptr;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.FuncAllocation;
import org.o42a.util.string.ID;


public class ConstCodeBackend implements CodeBackend {

	private final ConstBackend backend;

	public ConstCodeBackend(ConstBackend backend) {
		this.backend = backend;
	}

	public final ConstBackend getBackend() {
		return this.backend;
	}

	@Override
	public <F extends Func<F>> SignatureWriter<F> addSignature(
			Signature<F> signature) {
		return new CSignatureWriter<>(getBackend(), signature);
	}

	@Override
	public <F extends Func<F>> FuncWriter<F> addFunction(
			Function<F> function,
			BeforeReturn beforeReturn) {
		return new CFunction<>(this.backend, function, beforeReturn);
	}

	@Override
	public <F extends Func<F>> FuncAllocation<F> externFunction(
			ID id,
			FuncPtr<F> pointer) {
		return new ExternCFAlloc<>(getBackend(), pointer, id.toString());
	}

	@Override
	public DataAllocation<AnyOp> codeToAny(
			final CodePtr ptr) {

		final CCodePos cpos = cast(ptr.pos());

		return new AnyCDAlloc(
				getBackend(),
				ptr.toAny(),
				new UnderAlloc<AnyOp>() {
					@Override
					public Ptr<AnyOp> allocateUnderlying(CDAlloc<AnyOp> alloc) {
						return cpos.part().underlying().ptr().toAny();
					}
				});
	}

}
