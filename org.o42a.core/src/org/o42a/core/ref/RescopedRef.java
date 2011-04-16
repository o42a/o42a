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
package org.o42a.core.ref;

import org.o42a.core.*;
import org.o42a.core.def.Rescoper;
import org.o42a.core.ref.common.Wrap;
import org.o42a.core.ref.path.Path;
import org.o42a.util.log.Loggable;


final class RescopedRef extends Wrap {

	private final Rescoped rescoped;

	RescopedRef(Ref ref, Rescoper rescoper) {
		super(
				ref,
				new RescopedDistrubutor(
						ref,
						rescoper.getFinalScope()));
		this.rescoped = new Rescoped(ref, rescoper, distribute());
	}

	@Override
	public String toString() {
		if (this.rescoped == null) {
			return super.toString();
		}
		return this.rescoped.toString();
	}

	@Override
	protected Ref resolveWrapped() {

		final Path path = this.rescoped.getPath();

		if (path == null) {
			return this.rescoped;
		}

		return path.target(this, distribute());
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
