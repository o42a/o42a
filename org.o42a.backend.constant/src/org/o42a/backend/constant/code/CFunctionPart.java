/*
    Constant Handler Compiler Back-end
    Copyright (C) 2012-2014 Ruslan Lopatin

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

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Fn;
import org.o42a.codegen.code.Function;


final class CFunctionPart<F extends Fn<F>> extends CBlockPart {

	CFunctionPart(CFunction<F> function) {
		super(function);
	}

	private CFunctionPart(CFunction<F> function, int index) {
		super(function, function.getId().anonymous(index), index);
	}

	@SuppressWarnings("unchecked")
	public final CFunction<F> function() {
		return (CFunction<F>) code();
	}

	@Override
	protected CBlockPart newNextPart(int index) {
		return new CFunctionPart<>(function(), index);
	}

	@Override
	protected Block createUnderlying() {

		final Function<F> underlyingFunction = function().getUnderlying();

		if (index() == 0) {
			return underlyingFunction;
		}

		return underlyingFunction.addBlock(getId());
	}

}
