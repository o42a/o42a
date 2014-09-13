/*
    Compiler Code Generator
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
package org.o42a.codegen.code;

import static org.o42a.util.fn.Init.init;

import org.o42a.codegen.data.backend.FuncAllocation;
import org.o42a.util.fn.Init;


final class NullFuncPtr<F extends Fn<F>> extends FuncPtr<F> {

	private final Init<FuncAllocation<F>> allocation =
			init(this::createAllocation);

	NullFuncPtr(Signature<F> signature) {
		super(signature.getId().detail("null"), signature, true);
	}

	@Override
	public final Function<F> getFunction() {
		return null;
	}

	@Override
	public final boolean isExported() {
		return false;
	}

	@Override
	public final boolean hasSideEffects() {
		return true;
	}

	@Override
	public final int getFunctionFlags() {
		return 0;
	}

	@Override
	public final FuncAllocation<F> getAllocation() {
		return this.allocation.get();
	}

	@Override
	public String toString() {
		return getSignature().toString("NULL");
	}

	private FuncAllocation<F> createAllocation() {

		final Functions functions =
				getSignature().getGenerator().getFunctions();

		return functions.dataWriter().nullPtr(this);
	}

}
