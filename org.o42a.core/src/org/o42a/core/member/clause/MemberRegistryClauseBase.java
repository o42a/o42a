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

import org.o42a.core.member.field.MemberRegistry;


public abstract class MemberRegistryClauseBase {

	private int clauseSeq;

	public ClauseBuilder newClause(ClauseDeclaration declaration) {
		return createClause(declaration);
	}

	final ClauseBuilder createClause(ClauseDeclaration declaration) {
		if (!declaration.isAnonymous()) {
			return createNewClause(declaration);
		}
		return createNewClause(
				declaration.setName("_clause_" + (++this.clauseSeq)));
	}

	private final ClauseBuilder createNewClause(ClauseDeclaration declaration) {
		return new ClauseBuilder((MemberRegistry) this, declaration);
	}

}
