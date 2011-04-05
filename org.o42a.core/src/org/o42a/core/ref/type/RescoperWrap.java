/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.core.ref.type;

import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.def.Rescoper;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.st.Reproducer;


abstract class RescoperWrap extends Rescoper {

	private Rescoper wrapped;

	RescoperWrap(Scope finalScope) {
		super(finalScope);
	}

	@Override
	public Scope rescope(Scope scope) {
		return wrapped().rescope(scope);
	}

	@Override
	public Scope updateScope(Scope scope) {
		return wrapped().updateScope(scope);
	}

	@Override
	public HostOp rescope(CodeDirs dirs, HostOp host) {
		return wrapped().rescope(dirs, host);
	}

	@Override
	public Rescoper reproduce(LocationInfo location, Reproducer reproducer) {
		return wrapped().reproduce(location, reproducer);
	}

	@Override
	public String toString() {
		if (this.wrapped != null) {
			return this.wrapped.toString();
		}
		return "Rescoper[" + wrappedTypeRef() + ']';
	}

	private final Rescoper wrapped() {
		if (this.wrapped != null) {
			return this.wrapped;
		}
		this.wrapped = wrappedTypeRef().getRescoper();
		assert getFinalScope() == this.wrapped.getFinalScope() :
			"Wrong final scope of " + this.wrapped
			+ ": " + this.wrapped.getFinalScope()
			+ ", but " + getFinalScope() + " expected";
		return this.wrapped;
	}

	protected abstract TypeRef wrappedTypeRef();

}
