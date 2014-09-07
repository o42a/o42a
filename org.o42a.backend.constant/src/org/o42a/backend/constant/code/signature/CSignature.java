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
package org.o42a.backend.constant.code.signature;

import org.o42a.backend.constant.data.ConstBackend;
import org.o42a.codegen.code.Fn;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.SignatureBuilder;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.backend.SignatureAllocation;


public final class CSignature<F extends Fn<F>>
		extends Signature<F>
		implements SignatureAllocation<F> {

	private final CSignatureWriter<F> constWriter;

	CSignature(CSignatureWriter<F> constWriter) {
		super(constWriter.getOrignalSignature().getId());
		this.constWriter = constWriter;
	}

	public final ConstBackend getBackend() {
		return this.constWriter.getBackend();
	}

	public final Signature<F> getOriginal() {
		return this.constWriter.getOrignalSignature();
	}

	@Override
	public final boolean isDebuggable() {
		return false;
	}

	@Override
	public F op(FuncCaller<F> caller) {
		return getOriginal().op(caller);
	}

	@Override
	protected void build(SignatureBuilder builder) {
		this.constWriter.rebuild(builder);
	}

}
