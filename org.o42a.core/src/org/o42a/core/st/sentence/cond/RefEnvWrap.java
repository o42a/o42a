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
package org.o42a.core.st.sentence.cond;

import org.o42a.core.Scope;
import org.o42a.core.ref.Logical;
import org.o42a.core.st.StatementEnv;
import org.o42a.core.value.ValueType;


final class RefEnvWrap extends StatementEnv {

	private final RefCondition ref;
	private final StatementEnv initialEnv;
	private StatementEnv wrapped;

	RefEnvWrap(RefCondition ref, StatementEnv initialEnv) {
		this.ref = ref;
		this.initialEnv = initialEnv;
	}

	@Override
	public boolean hasPrerequisite() {
		return getWrapped().hasPrerequisite();
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
		return this.initialEnv + ", " + this.ref;
	}

	@Override
	protected ValueType<?> expectedType() {
		return getInitialEnv().getExpectedType();
	}

	final StatementEnv getInitialEnv() {
		return this.initialEnv;
	}

	final void setWrapped(StatementEnv wrapped) {
		assert this.wrapped == null :
			"Environment already assigned to " + this.ref;
		this.wrapped = wrapped;
	}

	final void removeWrapped() {
		assert this.wrapped == null :
			"Environment already assigned to " + this.ref;
		this.wrapped = this.initialEnv;
	}

	private StatementEnv getWrapped() {
		if (this.wrapped != null) {
			return this.wrapped;
		}
		return this.wrapped = this.ref.getConditionalEnv();
	}

}
