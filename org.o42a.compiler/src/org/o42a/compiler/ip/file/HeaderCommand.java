/*
    Compiler
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
package org.o42a.compiler.ip.file;

import static org.o42a.core.st.Instruction.SKIP_INSTRUCTION;

import org.o42a.core.Scope;
import org.o42a.core.ir.cmd.Cmd;
import org.o42a.core.ir.cmd.InlineCmd;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.ref.*;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.directive.Directive;
import org.o42a.core.value.link.TargetResolver;


final class HeaderCommand extends Command {

	HeaderCommand(HeaderStatement header, CommandEnv env) {
		super(header, env);
	}

	public final HeaderStatement getHeader() {
		return (HeaderStatement) getStatement();
	}

	@Override
	public CommandTargets getTargets() {
		return noCommands();
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {

		final Ref ref = getHeader().getRef();
		final Directive directive = ref.resolve(resolver).toDirective();

		if (directive == null) {
			HeaderStatement.notDirective(getLogger(), getLocation());
			return SKIP_INSTRUCTION;
		}

		return new HeaderInstruction(ref, directive);
	}

	@Override
	public DefTarget toTarget(Scope origin) {
		return null;
	}

	@Override
	public void resolveTargets(TargetResolver resolver, Scope origin) {
		throw new UnsupportedOperationException();
	}

	@Override
	public TypeParameters<?> typeParameters(Scope scope) {
		return null;
	}

	@Override
	public Action action(Resolver resolver) {
		throw new UnsupportedOperationException();
	}

	@Override
	public InlineCmd<?> inline(Normalizer normalizer, Scope origin) {
		throw new UnsupportedOperationException();
	}

	@Override
	public InlineCmd<?> normalize(RootNormalizer normalizer, Scope origin) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Cmd<?> cmd(Scope origin) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
		throw new UnsupportedOperationException();
	}
}

