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
package org.o42a.core.ref.impl;

import org.o42a.core.*;
import org.o42a.core.def.Rescoper;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.Wrap;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.log.Loggable;


public final class RescopedRef extends Wrap {

	private final Ref ref;
	private final Rescoper rescoper;

	public RescopedRef(Ref ref, Rescoper rescoper) {
		super(
				ref,
				new RescopedDistrubutor(
						ref,
						rescoper.getFinalScope()));
		this.ref = ref;
		this.rescoper = rescoper;
	}

	@Override
	public final TypeRef ancestor(LocationInfo location) {
		return this.ref.ancestor(location).rescope(this.rescoper);
	}

	@Override
	public String toString() {
		if (this.rescoper == null) {
			return super.toString();
		}
		return "Rescoped[" + this.rescoper + "](" + this.ref + ')';
	}

	@Override
	protected Ref resolveWrapped() {
		return this.rescoper.rescopeRef(this.ref);
	}

	private static final class RescopedDistrubutor extends Distributor {

		private final Ref ref;
		private final Scope scope;
		private final ScopePlace place;

		RescopedDistrubutor(Ref ref, Scope scope) {
			this.ref = ref;
			this.scope = scope;
			this.place = ref.getPlace();
		}

		@Override
		public Loggable getLoggable() {
			return this.ref.getLoggable();
		}

		@Override
		public CompilerContext getContext() {
			return this.ref.getContext();
		}

		@Override
		public ScopePlace getPlace() {
			return this.place;
		}

		@Override
		public Container getContainer() {
			return getScope().getContainer();
		}

		@Override
		public Scope getScope() {
			return this.scope;
		}

	}

}
