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

import static org.o42a.core.st.DefinitionTarget.conditionDefinition;
import static org.o42a.core.st.DefinitionTargets.noDefinitions;

import org.o42a.core.Scope;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.ref.*;
import org.o42a.core.st.*;
import org.o42a.core.value.Directive;
import org.o42a.core.value.ValueStruct;


final class RefConditionDefiner extends AbstractDefiner {

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
	public DefinerEnv nextEnv() {
		return new RefEnvWrap(this);
	}

	@Override
	public DefinitionTargets getDefinitionTargets() {

		final DefinitionTargets targets =
				getRefDefiner().getDefinitionTargets();

		if (targets.haveDefinition()) {
			return conditionDefinition(getRef());
		}

		return noDefinitions();
	}

	@Override
	public ValueStruct<?, ?> valueStruct(Scope scope) {
		return null;
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
	public Definitions define(Scope scope) {
		return getRefDefiner().getValueAdapter().condDef().toDefinitions(
				env().getExpectedValueStruct());
	}

	private static final class Env extends DefinerEnv {

		private final DefinerEnv initialEnv;

		Env(DefinerEnv initialEnv) {
			this.initialEnv = initialEnv;
		}

		@Override
		public boolean hasPrerequisite() {
			return this.initialEnv.hasPrerequisite();
		}

		@Override
		public Logical prerequisite(Scope scope) {
			return this.initialEnv.prerequisite(scope);
		}

		@Override
		public boolean hasPrecondition() {
			return this.initialEnv.hasPrecondition();
		}

		@Override
		public Logical precondition(Scope scope) {
			return this.initialEnv.precondition(scope);
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

}
