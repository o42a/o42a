/*
    Compiler Code Generator
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
package org.o42a.codegen.code;

import org.o42a.codegen.Generator;


abstract class AbstractFunctionSettings<S extends AbstractFunctionSettings<S>>
		implements FunctionAttributes {

	final Functions functions;
	int flags;

	AbstractFunctionSettings(Functions functions) {
		this.functions = functions;
	}

	public final Generator getGenerator() {
		return this.functions.getGenerator();
	}

	@Override
	public final int getFunctionFlags() {
		return this.flags;
	}

	@Override
	public final boolean isExported() {
		return (this.flags & EXPORTED) != 0;
	}

	@Override
	public final boolean hasSideEffects() {
		return (this.flags & NO_SIDE_EFFECTS) == 0;
	}

	public final S noSideEffects() {
		return sideEffects(false);
	}

	public final S sideEffects() {
		return sideEffects(true);
	}

	public final S sideEffects(boolean sideEffects) {
		if (sideEffects) {
			this.flags &= ~NO_SIDE_EFFECTS;
		} else {
			this.flags |= NO_SIDE_EFFECTS;
		}
		return self();
	}

	final Functions functions() {
		return this.functions;
	}

	@SuppressWarnings("unchecked")
	final S self() {
		return (S) this;
	}

}
