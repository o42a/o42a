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
package org.o42a.core.st.impl.imperative;

import static org.o42a.core.ref.Logical.logicalTrue;

import org.o42a.core.Scope;
import org.o42a.core.ref.Logical;
import org.o42a.core.st.StatementEnv;
import org.o42a.core.st.sentence.Imperatives;
import org.o42a.core.value.ValueStruct;


public class ImperativeStatementEnv extends StatementEnv {

	private final Imperatives imperatives;
	private final StatementEnv initialEnv;

	public ImperativeStatementEnv(
			Imperatives imperatives,
			StatementEnv initialEnv) {
		this.imperatives = imperatives;
		this.initialEnv = initialEnv;
	}

	@Override
	public boolean hasPrerequisite() {
		return false;
	}

	@Override
	public Logical prerequisite(Scope scope) {
		return logicalTrue(this.imperatives, scope);
	}

	@Override
	public boolean hasPrecondition() {
		return false;
	}

	@Override
	public Logical precondition(Scope scope) {
		return logicalTrue(this.imperatives, scope);
	}

	@Override
	protected ValueStruct<?, ?> expectedValueStruct() {
		return this.initialEnv.getExpectedValueStruct();
	}

}
