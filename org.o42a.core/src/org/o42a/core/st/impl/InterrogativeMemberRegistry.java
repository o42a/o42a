/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.core.st.impl;

import static org.o42a.core.member.Inclusions.NO_INCLUSIONS;
import static org.o42a.core.st.impl.SentenceErrors.prohibitedInterrogativeClause;
import static org.o42a.core.st.impl.SentenceErrors.prohibitedInterrogativeField;

import org.o42a.core.member.MemberRegistry;
import org.o42a.core.member.ProxyMemberRegistry;
import org.o42a.core.member.clause.ClauseBuilder;
import org.o42a.core.member.clause.ClauseDeclaration;
import org.o42a.core.member.clause.ClauseKind;
import org.o42a.core.st.sentence.Statements;


final class InterrogativeMemberRegistry extends ProxyMemberRegistry {

	InterrogativeMemberRegistry(MemberRegistry registry) {
		super(NO_INCLUSIONS, registry);
	}

	@Override
	public ClauseBuilder newClause(
			Statements statements,
			ClauseDeclaration declaration) {
		if (statements.getContainer().toClause() == null) {
			prohibitedInterrogativeClause(declaration);
			return null;
		}
		if (declaration.getKind() == ClauseKind.OVERRIDER) {
			prohibitedInterrogativeField(declaration);
			return null;
		}
		return registry().newClause(statements, declaration);
	}

}
