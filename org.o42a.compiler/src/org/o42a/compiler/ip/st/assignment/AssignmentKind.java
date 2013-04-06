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
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ir.local.Cmd;
import org.o42a.core.ir.local.InlineCmd;
import org.o42a.core.ref.*;
import org.o42a.core.st.DefValue;
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

	public abstract DefValue value(Resolver resolver);

	public abstract Action initialValue(Resolver resolver);

	public abstract void resolve(FullResolver resolver);

	public abstract AssignmentStatement reproduce(
			Reproducer reproducer,
			AssignmentStatement prototype);

	public abstract InlineEval inline(Normalizer normalizer, Scope origin);

	public abstract Eval eval(CodeBuilder builder, Scope origin);

	public abstract InlineEval normalize(
			RootNormalizer normalizer,
			Scope origin);

	public abstract InlineCmd inlineCommand(
			Normalizer normalizer,
			Scope origin);

	public abstract void normalizeCommand(RootNormalizer normalizer);

	public abstract Cmd cmd();

	void init(AssignmentStatement statement) {
		this.statement = statement;
	}

}
