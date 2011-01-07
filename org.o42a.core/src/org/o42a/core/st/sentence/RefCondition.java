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

import org.o42a.codegen.code.Code;
import org.o42a.core.Scope;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.local.Control;
import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.local.StOp;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.Cond;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.value.ValueType;


final class RefCondition extends St {

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
	public StatementKind getKind() {

		final StatementKind kind = this.ref.getKind();

		return kind.hasCondition()
		? StatementKind.CONDITION : StatementKind.EMPTY;
	}

	@Override
	public ValueType<?> getValueType() {
		return ValueType.VOID;
	}

	@Override
	public Definitions define(DefinitionTarget target) {
		return null;
	}

	@Override
	public Cond condition(Scope scope) {
		return this.ref.condition(scope);
	}

	@Override
	public Action initialValue(LocalScope scope) {
		return initialCondition(scope);
	}

	@Override
	public Action initialCondition(LocalScope scope) {
		return this.ref.initialCondition(scope);
	}

	@Override
	public St reproduce(Reproducer reproducer) {
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

		Op(LocalBuilder builder, St statement) {
			super(builder, statement);
		}

		@Override
		public void allocate(LocalBuilder builder, Code code) {
		}

		@Override
		public void writeAssignment(Control control, ValOp result) {
			writeCondition(control);
		}

		@Override
		public void writeCondition(Control control) {
			getStatement().op(getBuilder()).writeCondition(control);
		}

	}

}
