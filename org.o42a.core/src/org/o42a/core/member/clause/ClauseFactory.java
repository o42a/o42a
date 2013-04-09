/*
    Compiler Core
    Copyright (C) 2011-2013 Ruslan Lopatin

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

import static org.o42a.util.string.Capitalization.CASE_SENSITIVE;

import org.o42a.core.member.MemberRegistry;
import org.o42a.core.st.sentence.Statements;


public final class ClauseFactory {

	private final MemberRegistry memberRegistry;
	private int clauseSeq;

	public ClauseFactory(MemberRegistry memberRegistry) {
		this.memberRegistry = memberRegistry;
	}

	public ClauseBuilder newClause(
			Statements<?> statements,
			ClauseDeclaration declaration) {
		if (!declaration.isAnonymous()) {
			return createNewClause(statements, declaration);
		}
		return createNewClause(
				statements,
				declaration.setName(
						CASE_SENSITIVE.canonicalName(
								Integer.toString(++this.clauseSeq))));
	}

	private final ClauseBuilder createNewClause(
			Statements<?> statements,
			ClauseDeclaration declaration) {
		return new ClauseBuilder(
				statements,
				this.memberRegistry,
				declaration);
	}

}
