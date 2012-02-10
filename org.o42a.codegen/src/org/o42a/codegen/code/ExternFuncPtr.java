/*
    Compiler Code Generator
    Copyright (C) 2011,2012 Ruslan Lopatin

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
import org.o42a.codegen.data.backend.FuncAllocation;


final class ExternFuncPtr<F extends Func<F>> extends FuncPtr<F> {

	private FuncAllocation<F> allocation;

	ExternFuncPtr(CodeId id, Signature<F> signature) {
		super(id, signature, false);
	}

	@Override
	public Function<F> getFunction() {
		return null;
	}

	@Override
	public FuncAllocation<F> getAllocation() {
		if (this.allocation != null) {
			return this.allocation;
		}

		final Functions functions =
				getSignature().getGenerator().getFunctions();

		return this.allocation =
				functions.codeBackend().externFunction(getId(), this);
	}

	@Override
	public String toString() {
		return "exten " + getSignature().toString(getId().toString());
	}

}
