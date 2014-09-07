/*
    Compiler LLVM Back-end
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.backend.llvm.code;

import org.o42a.codegen.code.Fn;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.backend.SignatureAllocation;


public class LLSignature<F extends Fn<F>> implements SignatureAllocation<F> {

	private final long nativePtr;
	private final Signature<F> signature;

	public LLSignature(Signature<F> signature, long nativePtr) {
		this.signature = signature;
		this.nativePtr = nativePtr;
	}

	public Signature<F> getSignature() {
		return this.signature;
	}

	public final long getNativePtr() {
		return this.nativePtr;
	}

	@Override
	public String toString() {
		return this.signature.toString();
	}

}
