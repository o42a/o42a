/*
    Compiler
    Copyright (C) 2011-2013 Ruslan Lopatin

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
package org.o42a.compiler.ip.st.assignment;

import org.o42a.core.Scope;
import org.o42a.core.ir.local.Cmd;
import org.o42a.core.ir.local.InlineCmd;
import org.o42a.core.ref.FullResolver;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.ref.RootNormalizer;
import org.o42a.core.st.Reproducer;


abstract class AssignmentKind {

	private final AssignmentStatement statement;

	AssignmentKind(AssignmentStatement statement) {
		this.statement = statement;
	}

	public final AssignmentStatement getStatement() {
		return this.statement;
	}

	public boolean isError() {
		return false;
	}

	public abstract void resolve(FullResolver resolver);

	public abstract AssignmentKind reproduce(
			AssignmentStatement statement,
			Reproducer reproducer);

	public abstract InlineCmd inline(Normalizer normalizer, Scope origin);

	public abstract void normalize(RootNormalizer normalizer);

	public abstract Cmd cmd();

	@Override
	public String toString() {
		if (this.statement == null) {
			return super.toString();
		}
		return this.statement.toString();
	}

}
