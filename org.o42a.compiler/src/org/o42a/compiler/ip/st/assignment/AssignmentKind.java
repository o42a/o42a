/*
    Compiler
    Copyright (C) 2011-2014 Ruslan Lopatin

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
import org.o42a.core.ir.cmd.Cmd;
import org.o42a.core.ir.cmd.InlineCmd;
import org.o42a.core.ref.*;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.action.Action;


abstract class AssignmentKind {

	private AssignmentStatement statement;

	AssignmentKind(AssignmentStatement statement) {
		this.statement = statement;
	}

	AssignmentKind() {
		this.statement = null;
	}

	public final AssignmentStatement getStatement() {
		return this.statement;
	}

	public boolean isError() {
		return false;
	}

	public abstract Action action(Resolver resolver);

	public abstract void resolve(FullResolver resolver);

	public abstract AssignmentStatement reproduce(
			Reproducer reproducer,
			AssignmentStatement prototype);

	public abstract InlineCmd inline(Normalizer normalizer, Scope origin);

	public abstract InlineCmd normalize(
			RootNormalizer normalizer,
			Scope origin);

	public abstract Cmd cmd();

	void init(AssignmentStatement statement) {
		this.statement = statement;
	}

}
