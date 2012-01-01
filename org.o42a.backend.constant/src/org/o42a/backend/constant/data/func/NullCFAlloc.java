/*
    Constant Handler Compiler Back-end
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
package org.o42a.backend.constant.data.func;

import org.o42a.backend.constant.code.signature.CSignature;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.FuncPtr;


public class NullCFAlloc<F extends Func<F>> extends CFAlloc<F> {

	public NullCFAlloc(
			FuncPtr<F> underlyingPtr,
			CSignature<F> underlyingSignature) {
		super(underlyingPtr, underlyingSignature);
	}

	@Override
	protected FuncPtr<F> pointer() {
		return getBackend().getGenerator().getFunctions().nullPtr(
				getSignature());
	}

}
