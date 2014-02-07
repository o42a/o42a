/*
    Compiler Core
    Copyright (C) 2013,2014 Ruslan Lopatin

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

import static org.o42a.core.source.SectionTag.IMPLICIT_SECTION_TAG;

import org.o42a.core.Scope;
import org.o42a.core.ir.cmd.Cmd;
import org.o42a.core.ir.cmd.InlineCmd;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.ref.*;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.link.TargetResolver;


final class InclusionCommand extends Command implements Instruction {

	private Command replacement;

	InclusionCommand(Inclusion statement, CommandEnv env) {
		super(statement, env);
	}

	public final Inclusion getInclusion() {
		return (Inclusion) getStatement();
	}

	public final Command getReplacement() {
		return this.replacement;
	}

	@Override
	public final CommandTargets getTargets() {
		return noCommands();
	}

	@Override
	public final void execute(InstructionContext context) {

		final DeclarativeBlock block = context.getBlock().toDeclarativeBlock();

		if (!getInclusion().include()) {
			return;
		}

		getContext().include(block, IMPLICIT_SECTION_TAG);
	}

	@Override
	public TypeParameters<?> typeParameters(Scope scope) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DefTarget toTarget(Scope origin) {
		return DefTarget.NO_DEF_TARGET;
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {
		if (!getInclusion().include()) {
			return SKIP_INSTRUCTION;
		}
		return this;
	}

	@Override
	public Action action(Resolver resolver) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void resolveTargets(TargetResolver resolver, Scope origin) {
		throw new UnsupportedOperationException();
	}

	@Override
	public InlineCmd inline(Normalizer normalizer, Scope origin) {
		throw new UnsupportedOperationException();
	}

	@Override
	public InlineCmd normalize(RootNormalizer normalizer, Scope origin) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Command replaceWith(Statement statement) {
		return this.replacement = statement.command(env());
	}

	@Override
	public Cmd cmd(Scope origin) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
		throw new UnsupportedOperationException();
	}

}
