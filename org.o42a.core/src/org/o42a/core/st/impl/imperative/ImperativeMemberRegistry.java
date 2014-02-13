/*
    Compiler Core
    Copyright (C) 2013,2014 Ruslan Lopatin

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
package org.o42a.core.st.impl.imperative;

import static org.o42a.core.member.Inclusions.NO_INCLUSIONS;

import org.o42a.core.member.MemberRegistry;
import org.o42a.core.member.ProxyMemberRegistry;
import org.o42a.core.member.clause.ClauseBuilder;
import org.o42a.core.member.clause.ClauseDeclaration;
import org.o42a.core.member.field.FieldBuilder;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.st.sentence.Statements;


public class ImperativeMemberRegistry extends ProxyMemberRegistry {

	public ImperativeMemberRegistry(MemberRegistry registry) {
		super(NO_INCLUSIONS, registry);
	}

	@Override
	public FieldBuilder newField(
			FieldDeclaration declaration,
			FieldDefinition definition) {
		prohibitedDeclaration(declaration);
		return null;
	}

	@Override
	public ClauseBuilder newClause(
			Statements<?> statements,
			ClauseDeclaration declaration) {
		declaration.getLogger()
		.prohibitedClauseDeclaration(declaration.getLocation());
		return null;
	}

}
