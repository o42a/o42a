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
package org.o42a.core.st;

import org.o42a.core.*;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.local.StOp;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.st.sentence.ImperativeBlock;
import org.o42a.core.value.ValueType;


public abstract class Statement extends Placed {

	private StOp op;

	public Statement(LocationInfo location, Distributor distributor) {
		super(location, distributor);
	}

	public Instruction toInstruction(Scope scope, boolean assignment) {
		return null;
	}

	public DeclarativeBlock toDeclarativeBlock() {
		return null;
	}

	public ImperativeBlock toImperativeBlock() {
		return null;
	}

	public abstract DefinitionTargets getDefinitionTargets();

	public abstract ValueType<?> getValueType();

	public abstract StatementEnv setEnv(StatementEnv env);

	public abstract Definitions define(Scope scope);

	public abstract Action initialValue(LocalScope scope);

	public abstract Action initialLogicalValue(LocalScope scope);

	public abstract Statement reproduce(Reproducer reproducer);

	public final StOp op(LocalBuilder builder) {

		final StOp op = this.op;

		if (op != null && op.getBuilder() == builder) {
			return op;
		}

		return this.op = createOp(builder);
	}

	protected abstract StOp createOp(LocalBuilder builder);

	@Override
	protected Statement clone() throws CloneNotSupportedException {

		final Statement clone = (Statement) super.clone();

		clone.op = null;

		return clone;
	}

}
