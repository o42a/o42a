/*
    Compiler Core
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
package org.o42a.core.ref.impl.cond;

import static org.o42a.core.ref.RefUsage.CONDITION_REF_USAGE;
import static org.o42a.core.ref.RefUsage.TARGET_REF_USAGE;

import org.o42a.core.Scope;
import org.o42a.core.ir.local.Cmd;
import org.o42a.core.ir.local.InlineCmd;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.ref.*;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.action.ExecuteCommand;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.directive.Directive;
import org.o42a.core.value.link.TargetResolver;


final class RefConditionCommand extends Command {

	private InlineValue normal;

	RefConditionCommand(RefCondition ref, CommandEnv env) {
		super(ref, env);
	}

	public final Ref getRef() {
		return getRefCondition().getRef();
	}

	public final RefCondition getRefCondition() {
		return (RefCondition) getStatement();
	}

	@Override
	public CommandTargets getCommandTargets() {
		if (!getRef().isConstant()) {
			return actionCommand();
		}
		return actionCommand().setConstant();
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {

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
	public Action initialValue(LocalResolver resolver) {
		return new ExecuteCommand(
				this,
				getRef().value(resolver).getKnowledge().getCondition());
	}

	@Override
	public Action initialCond(LocalResolver resolver) {
		throw new UnsupportedOperationException();
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
	public void normalize(RootNormalizer normalizer) {
		if (!getRefCondition().isLocal()) {
			this.normal = getRef().inline(
					normalizer.newNormalizer(),
					normalizer.getNormalizedScope());
		}
	}

	@Override
	public Cmd cmd() {
		assert getStatement().assertFullyResolved();
		if (this.normal == null) {
			return new RefConditionCmd(getRefCondition());
		}
		return new InlineRefConditionCmd(this.normal);
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
		getRef().resolveAll(resolver.setRefUsage(
				getRefCondition().isLocal() ?
				TARGET_REF_USAGE : CONDITION_REF_USAGE));
	}

}
