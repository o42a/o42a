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
package org.o42a.core.member.clause;

import org.o42a.core.Scope;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.local.StOp;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.Cond;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.value.ValueType;


final class ClauseVariant extends St {

	private final Clause clause;

	ClauseVariant(ClauseBuilder builder, Clause clause) {
		super(builder, builder.distribute());
		this.clause = clause;
	}

	public Clause getClause() {
		return this.clause;
	}

	@Override
	public StatementKind getKind() {
		return StatementKind.CLAUSE;
	}

	@Override
	public ValueType<?> getValueType() {
		return null;
	}

	@Override
	public Cond condition(Scope scope) {
		return null;
	}

	@Override
	public Definitions define(DefinitionTarget target) {
		return null;
	}

	@Override
	public Action initialValue(LocalScope scope) {
		return null;
	}

	@Override
	public Action initialCondition(LocalScope scope) {
		return null;
	}

	@Override
	public St reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());
		reproducer.applyClause(this, getClause());
		return null;
	}

	@Override
	protected StOp createOp(LocalBuilder builder) {
		return null;
	}

}
