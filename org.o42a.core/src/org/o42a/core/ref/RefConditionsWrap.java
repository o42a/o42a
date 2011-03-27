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

import org.o42a.core.Scope;
import org.o42a.core.st.Conditions;
import org.o42a.core.value.ValueType;


final class RefConditionsWrap extends Conditions {

	private final Ref ref;
	private final Conditions initialConditions;
	private Conditions wrapped;

	RefConditionsWrap(Ref ref, Conditions initialConditions) {
		this.ref = ref;
		this.initialConditions = initialConditions;
	}

	@Override
	public Logical prerequisite(Scope scope) {
		return getWrapped().prerequisite(scope);
	}

	@Override
	public Logical precondition(Scope scope) {
		return getWrapped().precondition(scope);
	}

	@Override
	public String toString() {
		if (this.wrapped != null) {
			return this.wrapped.toString();
		}
		return this.initialConditions + ", " + this.ref;
	}

	@Override
	protected ValueType<?> expectedType() {
		return getInitialConditions().getExpectedType();
	}

	final Conditions getInitialConditions() {
		return this.initialConditions;
	}

	final void setWrapped(Conditions wrapped) {
		assert this.wrapped == null :
			"Conditions already built for " + this.ref;
		this.wrapped = wrapped;
	}

	private Conditions getWrapped() {
		if (this.wrapped != null) {
			return this.wrapped;
		}
		return this.wrapped =
			new RefConditions(this.ref, this.initialConditions);
	}

	private static final class RefConditions extends Conditions {

		private final Ref ref;
		private final Conditions initialConditions;

		RefConditions(Ref ref, Conditions initialConditions) {
			this.ref = ref;
			this.initialConditions = initialConditions;
		}

		@Override
		public Logical prerequisite(Scope scope) {
			return this.initialConditions.prerequisite(scope);
		}

		@Override
		public Logical precondition(Scope scope) {
			return this.initialConditions.precondition(scope).and(
					this.ref.expectedTypeAdapter()
					.rescope(scope).getLogical());
		}

		@Override
		public String toString() {
			return this.initialConditions + ", " + this.ref;
		}

		@Override
		protected ValueType<?> expectedType() {
			return this.initialConditions.getExpectedType();
		}

	}

}