/*
    Compiler
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.compiler.ip.assignment;

import static org.o42a.core.ir.local.Cmd.noCmd;

import org.o42a.core.Scope;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.local.Cmd;
import org.o42a.core.ir.local.InlineCmd;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.st.Reproducer;


final class AssignmentError extends AssignmentKind {

	AssignmentError(AssignmentStatement statement) {
		super(statement);
	}

	@Override
	public boolean isError() {
		return true;
	}

	@Override
	public void resolve(LocalResolver resolver) {
	}

	@Override
	public InlineCmd inline(
			Normalizer normalizer,
			Scope origin) {
		return null;
	}

	@Override
	public AssignmentKind reproduce(
			AssignmentStatement statement,
			Reproducer reproducer) {
		return new AssignmentError(statement);
	}

	@Override
	public Cmd op(CodeBuilder builder) {
		return noCmd(builder, getStatement());
	}

}
