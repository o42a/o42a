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
package org.o42a.core.ref.impl;

import org.o42a.core.Scope;
import org.o42a.core.ref.Logical;
import org.o42a.core.ref.RefDefiner;
import org.o42a.core.st.StatementEnv;
import org.o42a.core.value.ValueStruct;


public final class RefEnv extends StatementEnv {

	private final RefDefiner definer;

	public RefEnv(RefDefiner definer) {
		this.definer = definer;
	}

	@Override
	public boolean hasPrerequisite() {
		return this.definer.env().hasPrerequisite();
	}

	@Override
	public Logical prerequisite(Scope scope) {
		return this.definer.env().prerequisite(scope);
	}

	@Override
	public boolean hasPrecondition() {
		return true;
	}

	@Override
	public Logical precondition(Scope scope) {
		return this.definer.env().precondition(scope).and(
				this.definer.getValueAdapter().logical(scope));
	}

	@Override
	public String toString() {
		return this.definer.env() + ", " + this.definer;
	}

	@Override
	protected ValueStruct<?, ?> expectedValueStruct() {
		return this.definer.env().getExpectedValueStruct();
	}

}
