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
package org.o42a.codegen.data;

import org.o42a.codegen.Generator;


abstract class AbstractGlobalSettings<
		S extends AbstractGlobalSettings<S>>
				implements GlobalAttributes {

	private final Globals globals;
	int flags;

	AbstractGlobalSettings(Globals globals) {
		this.globals = globals;
	}

	public final Generator getGenerator() {
		return this.globals.getGenerator();
	}

	@Override
	public final boolean isExported() {
		return (this.flags & EXPORTED) != 0;
	}

	@Override
	public final boolean isConstant() {
		return (this.flags & CONSTANT) != 0;
	}

	public final S setConstant() {
		this.flags |= CONSTANT;
		return self();
	}

	public final S setVariable() {
		this.flags &= ~CONSTANT;
		return self();
	}

	public final S setConstant(boolean constant) {
		if (constant) {
			return setConstant();
		}
		return setVariable();
	}

	@Override
	public final int getDataFlags() {
		return this.flags;
	}

	final Globals globals() {
		return this.globals;
	}

	@SuppressWarnings("unchecked")
	private final S self() {
		return (S) this;
	}

}
