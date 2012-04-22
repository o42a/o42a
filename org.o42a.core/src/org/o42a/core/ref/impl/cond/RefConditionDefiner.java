/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import org.o42a.core.Scope;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.def.*;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.object.link.TargetResolver;
import org.o42a.core.ref.*;
import org.o42a.core.st.*;
import org.o42a.core.value.Directive;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;
import org.o42a.util.fn.Cancelable;


final class RefConditionDefiner extends Definer {

	private RefDefiner refDefiner;
	private Definer replacement;

	RefConditionDefiner(RefCondition statement, DefinerEnv env) {
		super(statement, env);
		this.refDefiner = statement.getRef().define(new Env(env));
	}

	public final Ref getRef() {
		return ((RefCondition) getStatement()).getRef();
	}

	public final RefDefiner getRefDefiner() {
		return this.refDefiner;
	}

	public final Definer getReplacement() {
		return this.replacement;
	}

	@Override
	public DefTargets getDefTargets() {
		if (!getRef().isConstant()) {
			return expressionDef();
		}
		return expressionDef().setConstant();
	}

	@Override
	public Definer replaceWith(Statement statement) {
		return this.replacement = statement.define(env());
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
	public DefTarget toTarget() {
		return DefTarget.NO_DEF_TARGET;
	}

	@Override
	public DefValue value(Resolver resolver) {

		final Value<?> value = getRef().value(resolver);

		return value.getKnowledge().toLogicalValue().toDefValue();
	}

	@Override
	public void resolveTargets(TargetResolver resolver) {
	}

	@Override
	public InlineEval inline(Normalizer normalizer, Scope origin) {

		final InlineValue value = getRef().inline(normalizer, origin);

		if (value != null) {
			return new Inline(value);
		}

		getRef().normalize(normalizer.getAnalyzer());

		return null;
	}

	@Override
	public InlineEval normalize(RootNormalizer normalizer, Scope origin) {
		return inline(normalizer.newNormalizer(), origin);
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
		getRef().resolve(resolver).resolveLogical();
	}

	@Override
	protected Eval createEval(CodeBuilder builder) {
		return new CondEval(builder, getRef(), getRefDefiner());
	}

	private static final class Env extends DefinerEnv {

		private final DefinerEnv initialEnv;

		Env(DefinerEnv initialEnv) {
			this.initialEnv = initialEnv;
		}

		@Override
		public String toString() {
			return this.initialEnv.toString();
		}

		@Override
		protected ValueStruct<?, ?> expectedValueStruct() {
			return null;// To prevent Ref adaption.
		}

	}

	private static final class Inline extends InlineEval {

		private final InlineValue value;

		Inline(InlineValue value) {
			super(null);
			this.value = value;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {
			this.value.writeCond(dirs.dirs(), host);
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

	private static final class CondEval extends Eval {

		private final RefEval refEval;

		CondEval(CodeBuilder builder, Ref ref, RefDefiner refDefiner) {
			super(builder, ref);
			this.refEval = refDefiner.eval(builder);
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {
			this.refEval.writeCond(dirs.dirs(), host);
		}

	}

}
