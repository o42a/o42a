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
package org.o42a.core.st.sentence.declarative;

import static org.o42a.core.st.DefinitionTarget.conditionDefinition;
import static org.o42a.core.st.DefinitionTargets.noDefinitions;

import org.o42a.codegen.code.Code;
import org.o42a.core.Scope;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.local.Control;
import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.local.StOp;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.ref.Logical;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.value.ValueType;


public final class RefCondition extends Statement {

	private final Ref ref;

	public RefCondition(Ref ref) {
		super(ref, ref.distribute());
		this.ref = ref;
	}

	@Override
	public Instruction toInstruction(Resolver resolver, boolean assignment) {
		return this.ref.toInstruction(resolver, false);
	}

	@Override
	public DefinitionTargets getDefinitionTargets() {

		final DefinitionTarget target = this.ref.getDefinitionTargets();

		if (target.haveDefinition()) {
			return conditionDefinition(this.ref);
		}

		return noDefinitions();
	}

	@Override
	public ValueType<?> getValueType() {
		return ValueType.VOID;
	}

	@Override
	public Definitions define(Scope scope) {
		return this.ref.toCondDef().toDefinitions();
	}

	@Override
	public StatementEnv setEnv(StatementEnv env) {
		return this.ref.setEnv(new ConditionalEnv(env));
	}

	@Override
	public Action initialValue(LocalResolver resolver) {
		return initialLogicalValue(resolver);
	}

	@Override
	public Action initialLogicalValue(LocalResolver resolver) {
		return this.ref.initialLogicalValue(resolver);
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
	protected void fullyResolve() {
		this.ref.resolveAll();
	}

	@Override
	protected void fullyResolveValues(Resolver resolver) {
		this.ref.resolveValues(resolver);
	}

	@Override
	protected StOp createOp(LocalBuilder builder) {
		return new Op(builder, this.ref);
	}

	private static final class Op extends StOp {

		Op(LocalBuilder builder, Statement statement) {
			super(builder, statement);
		}

		@Override
		public void allocate(LocalBuilder builder, Code code) {
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

	private static final class ConditionalEnv extends StatementEnv {

		private final StatementEnv env;

		ConditionalEnv(StatementEnv conditions) {
			this.env = conditions;
		}

		@Override
		public boolean hasPrerequisite() {
			return this.env.hasPrerequisite();
		}

		@Override
		public Logical prerequisite(Scope scope) {
			return this.env.prerequisite(scope);
		}

		@Override
		public Logical precondition(Scope scope) {
			return this.env.precondition(scope);
		}

		@Override
		public String toString() {
			return this.env.toString();
		}

		@Override
		protected ValueType<?> expectedType() {
			return null;// To prevent Ref adaption.
		}

	}

}
