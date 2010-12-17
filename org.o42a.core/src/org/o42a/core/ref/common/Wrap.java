/*
    Compiler Core
    Copyright (C) 2010 Ruslan Lopatin

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
package org.o42a.core.ref.common;

import org.o42a.core.Distributor;
import org.o42a.core.LocationSpec;
import org.o42a.core.Scope;
import org.o42a.core.def.Rescoper;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.path.Path;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.Value;


public abstract class Wrap extends Ref {

	private Ref wrapped;

	public Wrap(LocationSpec location, Distributor distributor) {
		super(location, distributor);
	}

	@Override
	public Path getPath() {
		return wrapped().getPath();
	}

	public final Ref getWrapped() {
		return this.wrapped;
	}

	@Override
	public final Resolution resolve(Scope scope) {
		return wrapped().resolve(scope);
	}

	@Override
	public final Value<?> value(Scope scope) {
		return wrapped().value(scope);
	}

	@Override
	public final Ref reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());
		return wrapped().reproduce(reproducer);
	}

	@Override
	public Rescoper toRescoper() {
		return wrapped().toRescoper();
	}

	@Override
	public String toString() {
		if (this.wrapped != null) {
			return this.wrapped.toString();
		}
		return super.toString();
	}

	protected abstract Ref resolveWrapped();

	@Override
	protected RefOp createOp(HostOp host) {
		return wrapped().op(host);
	}

	private Ref wrapped() {
		if (this.wrapped == null) {
			this.wrapped = resolveWrapped();
			if (this.wrapped == null) {
				this.wrapped = falseRef(this, distribute());
			} else {
				this.wrapped.assertCompatible(getScope());
			}
		}
		return this.wrapped;
	}

}
