/*
    Compiler Core
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
package org.o42a.core.ref.impl.cond;

import static org.o42a.core.ref.RefUsage.CONDITION_REF_USAGE;
import static org.o42a.core.ref.RefUsage.TARGET_REF_USAGE;

import org.o42a.core.Scope;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.ref.*;
import org.o42a.core.st.*;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.Value;
import org.o42a.core.value.directive.Directive;
import org.o42a.core.value.link.TargetResolver;


final class RefConditionDefiner extends Definer {

	private Definer replacement;

	RefConditionDefiner(RefCondition statement, CommandEnv env) {
		super(statement, env);
	}

	public final Ref getRef() {
		return getRefCondition().getRef();
	}

	public final RefCondition getRefCondition() {
		return (RefCondition) getStatement();
	}

	public final Definer getReplacement() {
		return this.replacement;
	}

	@Override
	public CommandTargets getTargets() {
		if (!getRef().isConstant()) {
			return actionCommand();
		}
		return actionCommand().setConstant();
	}

	@Override
	public Definer replaceWith(Statement statement) {
		return this.replacement = statement.define(env());
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
	public DefValue value(Resolver resolver) {

		final Value<?> value = getRef().value(resolver);

		return value.getKnowledge().getCondition().toDefValue();
	}

	@Override
	public void resolveTargets(TargetResolver resolver, Scope origin) {
	}

	@Override
	public InlineEval inline(Normalizer normalizer, Scope origin) {
		if (!getRefCondition().isLocal()) {

			final InlineValue value = getRef().inline(normalizer, origin);

			if (value != null) {
				return new InlineRefCondition(value);
			}
		}

		getRef().normalize(normalizer.getAnalyzer());

		return null;
	}

	@Override
	public InlineEval normalize(RootNormalizer normalizer, Scope origin) {
		return inline(normalizer.newNormalizer(), origin);
	}

	@Override
	public Eval eval(CodeBuilder builder, Scope origin) {
		assert getStatement().assertFullyResolved();
		return new RefConditionEval(getRefCondition());
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
		getRef().resolveAll(resolver.setRefUsage(
				getRefCondition().isLocal() ?
				TARGET_REF_USAGE : CONDITION_REF_USAGE));
	}

}
