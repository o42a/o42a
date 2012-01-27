/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.core.ref.impl.cond;

import org.o42a.core.Scope;
import org.o42a.core.ref.Logical;
import org.o42a.core.st.Definer;
import org.o42a.core.st.StatementEnv;
import org.o42a.core.value.ValueStruct;


final class RefEnvWrap extends StatementEnv {

	private final RefConditionDefiner definer;
	private StatementEnv wrapped;

	RefEnvWrap(RefConditionDefiner definer) {
		this.definer = definer;
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
	public boolean hasPrecondition() {
		return getWrapped().hasPrecondition();
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
		return this.definer.env() + ", " + this.definer;
	}

	@Override
	protected ValueStruct<?, ?> expectedValueStruct() {
		return getInitialEnv().getExpectedValueStruct();
	}

	final StatementEnv getInitialEnv() {
		return this.definer.env();
	}

	private StatementEnv getWrapped() {
		if (this.wrapped != null) {
			return this.wrapped;
		}

		final Definer replacement = this.definer.getReplacement();

		if (replacement != null) {
			return this.wrapped = replacement.nextEnv();
		}

		return this.wrapped = this.definer.getRefDefiner().nextEnv();
	}


}
