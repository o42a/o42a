/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.core.ref.impl.cond;

import static org.o42a.core.ref.RefUsage.CONDITION_REF_USAGE;
import static org.o42a.core.ref.RefUsage.CONTAINER_REF_USAGE;

import org.o42a.analysis.escape.EscapeAnalyzer;
import org.o42a.analysis.escape.EscapeFlag;
import org.o42a.core.Scope;
import org.o42a.core.ir.cmd.Cmd;
import org.o42a.core.ir.cmd.InlineCmd;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.ref.*;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.action.ExecuteCommand;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.directive.Directive;
import org.o42a.core.value.link.TargetResolver;


final class RefConditionCommand extends Command {

	RefConditionCommand(RefCondition ref, CommandEnv env) {
		super(ref, env);
	}

	public final Ref getRef() {
		return getRefCondition().ref();
	}

	public final RefCondition getRefCondition() {
		return (RefCondition) getStatement();
	}

	@Override
	public CommandTargets getTargets() {
		if (!getRef().isConstant()) {
			return actionCommand();
		}
		return actionCommand().setConstant();
	}

	@Override
	public Command replaceWith(Statement statement) {
		return statement.command(env());
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {
		if (getRefCondition().isLocal()) {
			return null;
		}

		final Directive directive = getRef().resolve(resolver).toDirective();

		if (directive == null) {
			return null;
		}

		return new ApplyDirective(getRef(), resolver, directive);
	}

	@Override
	public DefTarget toTarget(Scope origin) {
		return DefTarget.NO_DEF_TARGET;
	}

	@Override
	public TypeParameters<?> typeParameters(Scope scope) {
		return null;
	}

	@Override
	public Action action(Resolver resolver) {
		return new ExecuteCommand(
				this,
				getRef().value(resolver).getKnowledge().getCondition());
	}

	@Override
	public EscapeFlag escapeFlag(EscapeAnalyzer analyzer, Scope scope) {
		return getRef().escapeFlag(analyzer, scope);
	}

	@Override
	public void resolveTargets(TargetResolver resolver, Scope origin) {
	}

	@Override
	public InlineCmd inline(Normalizer normalizer, Scope origin) {
		if (!getRefCondition().isLocal()) {

			final InlineValue value = getRef().inline(normalizer, origin);

			if (value != null) {
				return new InlineRefConditionCmd(value);
			}
		}

		getRef().normalize(normalizer.getAnalyzer());

		return null;
	}

	@Override
	public InlineCmd normalize(RootNormalizer normalizer, Scope origin) {
		return inline(normalizer.newNormalizer(), origin);
	}

	@Override
	public Cmd cmd(Scope origin) {
		assert getStatement().assertFullyResolved();
		return new RefConditionCmd(getRefCondition());
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
		getRef().resolveAll(resolver.setRefUsage(
				getRefCondition().isLocal() ?
				CONTAINER_REF_USAGE : CONDITION_REF_USAGE));
	}

}
