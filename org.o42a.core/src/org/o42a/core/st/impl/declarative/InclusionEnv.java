/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.core.st.impl.declarative;

import org.o42a.core.Scope;
import org.o42a.core.ref.Logical;
import org.o42a.core.st.DefinerEnv;
import org.o42a.core.value.ValueStruct;


final class InclusionEnv extends DefinerEnv {

	private final InclusionDefiner<?> definer;
	private DefinerEnv wrapped;

	InclusionEnv(InclusionDefiner<?> definer) {
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
	protected ValueStruct<?, ?> expectedValueStruct() {
		return getWrapped().getExpectedValueStruct();
	}

	private final DefinerEnv getWrapped() {
		if (this.wrapped != null) {
			return this.wrapped;
		}
		return this.wrapped = this.definer.nextEnv();
	}

}
