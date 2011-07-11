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
package org.o42a.core.st.sentence.declarative;

import org.o42a.core.Scope;
import org.o42a.core.ref.Logical;
import org.o42a.core.st.StatementEnv;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.value.ValueType;


final class InclusionEnv extends StatementEnv {

	private final StatementEnv initialEnv;
	private StatementEnv env;

	InclusionEnv(StatementEnv initialEnv) {
		this.initialEnv = initialEnv;
	}

	@Override
	public boolean hasPrerequisite() {
		return this.env.hasPrerequisite();
	}

	@Override
	public Logical prerequisite(Scope scope) {
		return this.env.prerequisite(scope);
	}

	@Override
	public Logical precondition(Scope scope) {
		return this.env.precondition(scope);
	}

	@Override
	protected ValueType<?> expectedType() {
		return this.env.getExpectedType();
	}

	void setBlock(DeclarativeBlock block) {
		assert this.env == null :
			"Inclusion environment already set";
		this.env = block.setEnv(this.initialEnv);
	}

}
