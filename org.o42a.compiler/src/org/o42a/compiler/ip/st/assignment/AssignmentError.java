/*
    Compiler
    Copyright (C) 2012,2013 Ruslan Lopatin

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

import static org.o42a.core.ir.def.Eval.NO_EVAL;
import static org.o42a.core.ir.local.Cmd.NO_CMD;

import org.o42a.core.Scope;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ir.local.Cmd;
import org.o42a.core.ir.local.InlineCmd;
import org.o42a.core.ref.*;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.action.ExecuteCommand;
import org.o42a.core.value.Condition;


final class AssignmentError extends AssignmentKind {

	AssignmentError(AssignmentStatement statement) {
		super(statement);
	}

	@Override
	public boolean isError() {
		return true;
	}

	@Override
	public Action action(Resolver resolver) {
		return new ExecuteCommand(getStatement(), Condition.FALSE);
	}

	@Override
	public void resolve(FullResolver resolver) {
	}

	@Override
	public InlineEval inline(Normalizer normalizer, Scope origin) {
		return null;
	}

	@Override
	public Eval eval(Scope origin) {
		return NO_EVAL;
	}

	@Override
	public InlineEval normalize(RootNormalizer normalizer, Scope origin) {
		return null;
	}

	@Override
	public InlineCmd inlineCommand(Normalizer normalizer, Scope origin) {
		return null;
	}

	@Override
	public InlineCmd normalizeCommand(RootNormalizer normalizer, Scope origin) {
		return null;
	}

	@Override
	public AssignmentStatement reproduce(
			Reproducer reproducer,
			AssignmentStatement prototype) {
		return null;
	}

	@Override
	public Cmd cmd() {
		return NO_CMD;
	}

	@Override
	public String toString() {
		return "ASSIGNMENT ERROR";
	}

}
