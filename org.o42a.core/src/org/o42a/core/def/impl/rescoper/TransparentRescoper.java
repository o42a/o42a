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
import org.o42a.core.def.Def;
import org.o42a.core.def.Definitions;
import org.o42a.core.def.Rescoper;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;


public final class TransparentRescoper extends Rescoper {

	public TransparentRescoper(Scope finalScope) {
		super(finalScope);
	}

	@Override
	public boolean isStatic() {
		return true;
	}

	@Override
	public boolean isTransparent() {
		return true;
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
	public Resolver rescope(LocationInfo location, Resolver resolver) {
		return resolver;
	}

	@Override
	public Ref rescopeRef(Ref ref) {
		return ref;
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
	public Rescoper reproduce(LocationInfo location, Reproducer reproducer) {
		getFinalScope().assertCompatible(reproducer.getReproducingScope());
		return new TransparentRescoper(reproducer.getScope());
	}

	@Override
	public void resolveAll(ScopeInfo location, Resolver resolver) {
	}

	@Override
	public HostOp rescope(CodeDirs dirs, HostOp host) {
		return host;
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

	@Override
	protected Rescoper createUpscoped(Scope toScope) {
		return null;
	}

}
