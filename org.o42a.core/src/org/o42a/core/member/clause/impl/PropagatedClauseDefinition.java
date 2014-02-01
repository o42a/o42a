/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.core.member.clause.impl;

import static org.o42a.core.object.def.Definitions.emptyDefinitions;
import static org.o42a.core.object.type.FieldAscendants.NO_FIELD_ASCENDANTS;

import org.o42a.core.member.Member;
import org.o42a.core.member.clause.PlainClause;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectMembers;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.object.type.Ascendants;


final class PropagatedClauseDefinition extends Obj {

	private final PlainClause clause;

	PropagatedClauseDefinition(PlainClause clause, PlainClause overridden) {
		super(clause, overridden.getObject());
		this.clause = clause;
	}

	@Override
	public final Member toMember() {
		return toClause().toMember();
	}

	@Override
	public final PlainClause toClause() {
		return this.clause;
	}

	@Override
	public boolean isPropagated() {
		return true;
	}

	@Override
	public String toString() {
		if (this.clause != null) {
			return super.toString();
		}
		return this.clause.toString();
	}

	@Override
	protected Nesting createNesting() {
		return toClause().getDefinitionNesting();
	}

	@Override
	protected Ascendants buildAscendants() {
		return new Ascendants(this).declareField(NO_FIELD_ASCENDANTS);
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
	}

	@Override
	protected Definitions explicitDefinitions() {
		return emptyDefinitions(this, getScope());
	}

}
