/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.core.st.impl.cond;

import static org.o42a.core.st.DefinitionTarget.conditionDefinition;
import static org.o42a.core.st.DefinitionTargets.noDefinitions;

import org.o42a.core.Scope;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.local.Control;
import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.local.StOp;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.Resolver;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.value.Directive;
import org.o42a.core.value.ValueStruct;


public final class RefCondition extends Statement {

	private final Ref ref;
	private RefEnvWrap env;
	private StatementEnv conditionalEnv;

	public RefCondition(Ref ref) {
		super(ref, ref.distribute());
		this.ref = ref;
	}

	public final Ref getRef() {
		return this.ref;
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {

		final Resolution resolution = this.ref.resolve(resolver);

		if (resolution == null) {
			return null;
		}

		final Directive directive = resolution.toDirective(resolver);

		if (directive == null) {
			return null;
		}

		return new ApplyDirective(this, resolver, directive);
	}

	@Override
	public DefinitionTargets getDefinitionTargets() {

		final DefinitionTargets targets = this.ref.getDefinitionTargets();

		if (targets.haveDefinition()) {
			return conditionDefinition(this.ref);
		}

		return noDefinitions();
	}

	@Override
	public ValueStruct<?, ?> valueStruct(Scope scope) {
		return ValueStruct.VOID;
	}

	@Override
	public Definitions define(Scope scope) {
		return this.ref.toCondDef().toDefinitions();
	}

	@Override
	public StatementEnv setEnv(StatementEnv env) {
		assert this.env == null :
			"Environment already assigned for: " + this;
		this.conditionalEnv = this.ref.setEnv(new ConditionalEnv(env));
		return this.env = new RefEnvWrap(this, env);
	}

	@Override
	public Action initialValue(LocalResolver resolver) {
		return this.ref.initialLogicalValue(resolver);
	}

	@Override
	public Action initialLogicalValue(LocalResolver resolver) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Statement reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final Ref ref = this.ref.reproduce(reproducer);

		if (ref == null) {
			return null;
		}

		return new RefCondition(ref);
	}

	@Override
	public String toString() {
		return this.ref.toString();
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
		this.ref.resolveAll(resolver);
	}

	@Override
	protected void fullyResolveValues(Resolver resolver) {
		this.ref.resolveValues(resolver);
	}

	@Override
	protected StOp createOp(LocalBuilder builder) {
		return new Op(builder, this.ref);
	}

	final StatementEnv getConditionalEnv() {
		return this.conditionalEnv;
	}

	final RefEnvWrap getEnv() {
		return this.env;
	}

	private static final class Op extends StOp {

		Op(LocalBuilder builder, Statement statement) {
			super(builder, statement);
		}

		@Override
		public void writeAssignment(Control control, ValOp result) {
			writeLogicalValue(control);
		}

		@Override
		public void writeLogicalValue(Control control) {
			getStatement().op(getBuilder()).writeLogicalValue(control);
		}

	}

}
