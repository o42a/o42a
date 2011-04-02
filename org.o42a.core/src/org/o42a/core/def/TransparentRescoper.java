/*
    Compiler Core
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
package org.o42a.core.def;

import static org.o42a.core.ref.path.Path.SELF_PATH;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ref.path.Path;
import org.o42a.core.st.Reproducer;


final class TransparentRescoper extends Rescoper {

	TransparentRescoper(Scope finalScope) {
		super(finalScope);
	}

	@Override
	public boolean isTransparent() {
		return true;
	}

	@Override
	public Path getPath() {
		return SELF_PATH;
	}

	@Override
	public Definitions update(Definitions definitions) {
		return definitions;
	}

	@Override
	public Scope rescope(Scope scope) {
		return scope;
	}

	@Override
	public Scope updateScope(Scope scope) {
		return scope;
	}

	@Override
	public <D extends Def<D>> D updateDef(D def) {
		return def;
	}

	@Override
	public Rescoper and(Rescoper filter) {
		return filter;
	}

	@Override
	public HostOp rescope(Code code, CodePos exit, HostOp host) {
		return host;
	}

	@Override
	public Rescoper reproduce(LocationInfo location, Reproducer reproducer) {
		getFinalScope().assertCompatible(reproducer.getReproducingScope());
		return new TransparentRescoper(reproducer.getScope());
	}

	@Override
	public int hashCode() {
		return getFinalScope().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}

		final TransparentRescoper other = (TransparentRescoper) obj;

		return getFinalScope() == other.getFinalScope();
	}

	@Override
	public String toString() {
		return "noop";
	}

}
