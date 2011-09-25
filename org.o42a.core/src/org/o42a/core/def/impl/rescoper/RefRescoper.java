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
package org.o42a.core.def.impl.rescoper;

import org.o42a.core.Scope;
import org.o42a.core.ScopeInfo;
import org.o42a.core.def.Rescoper;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.Resolver;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;


public final class RefRescoper extends Rescoper {

	private final Ref ref;

	public RefRescoper(Ref ref) {
		super(ref.getScope());
		this.ref = ref;
	}

	@Override
	public boolean isStatic() {
		return this.ref.isStatic();
	}

	@Override
	public Scope rescope(Scope scope) {
		return this.ref.resolve(scope.dummyResolver()).getScope();
	}

	@Override
	public Resolver rescope(LocationInfo location, Resolver resolver) {

		final Resolution resolution = this.ref.resolve(resolver);

		if (resolution == null) {
			return null;
		}

		return resolution.getScope().walkingResolver(resolver);
	}

	@Override
	public Scope updateScope(Scope scope) {
		return getFinalScope();
	}

	@Override
	protected Rescoper createUpscoped(Scope toScope) {

		final Ref upscopedRef = this.ref.upscope(toScope);

		if (upscopedRef == null) {
			return null;
		}

		return upscopedRef.toRescoper();
	}

	@Override
	public void resolveAll(ScopeInfo location, Resolver resolver) {
		this.ref.resolveAll(resolver);
	}

	@Override
	public Rescoper reproduce(LocationInfo location, Reproducer reproducer) {

		final Ref ref = this.ref.reproduce(reproducer);

		if (ref == null) {
			return null;
		}

		return new RefRescoper(ref);
	}

	@Override
	public HostOp rescope(CodeDirs dirs, HostOp host) {
		return this.ref.op(host).target(dirs);
	}

	@Override
	public String toString() {
		return "RescopeBy[" + this.ref + ']';
	}

}
