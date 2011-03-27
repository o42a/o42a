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
package org.o42a.core.st.sentence;

import static org.o42a.core.st.DefinitionTarget.conditionDefinition;

import org.o42a.codegen.code.Code;
import org.o42a.core.Scope;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.local.Control;
import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.local.StOp;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.Logical;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.value.ValueType;


final class RefCondition extends Statement {

	private final Ref ref;

	RefCondition(Ref ref) {
		super(ref, ref.distribute());
		this.ref = ref;
	}

	@Override
	public Instruction toInstruction(Scope scope, boolean assignment) {
		return this.ref.toInstruction(scope, false);
	}

	@Override
	public DefinitionTargets getDefinitionTargets() {

		final DefinitionTargets targets = this.ref.getDefinitionTargets();

		if (targets.haveDefinition()) {
			return conditionDefinition();
		}

		return DefinitionTargets.noDefinitions();
	}

	@Override
	public ValueType<?> getValueType() {
		return ValueType.VOID;
	}

	@Override
	public Definitions define(Scope scope) {
		return null;
	}

	@Override
	public Conditions setConditions(Conditions conditions) {
		return this.ref.setConditions(new RefConditions(conditions));
	}

	@Override
	public Action initialValue(LocalScope scope) {
		return initialLogicalValue(scope);
	}

	@Override
	public Action initialLogicalValue(LocalScope scope) {
		return this.ref.initialLogicalValue(scope);
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

	private static final class RefConditions extends Conditions {

		private final Conditions conditions;

		RefConditions(Conditions conditions) {
			this.conditions = conditions;
		}

		@Override
		public Logical prerequisite(Scope scope) {
			return this.conditions.prerequisite(scope);
		}

		@Override
		public Logical precondition(Scope scope) {
			return this.conditions.precondition(scope);
		}

		@Override
		public String toString() {
			return this.conditions.toString();
		}

		@Override
		protected ValueType<?> expectedType() {
			return null;// To prevent Ref adaption.
		}

	}

}
