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

import static org.o42a.core.st.sentence.ImperativeBlock.topLevelImperativeBlock;

import org.o42a.core.Distributor;
import org.o42a.core.LocationInfo;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.st.sentence.*;
import org.o42a.util.Lambda;


public class DeclarativeIssueFactory extends DeclarativeFactory {

	@Override
	public DeclarativeBlock groupParentheses(
			Group group,
			Distributor distributor,
			MemberRegistry memberRegistry) {
		group.getLogger().prohibitedClauseDeclaration(group);
		return null;
	}

	@Override
	public ImperativeBlock createBraces(
			LocationInfo location,
			Distributor distributor,
			Declaratives enclosing,
			String name) {
		return topLevelImperativeBlock(
				location,
				distributor,
				enclosing,
				name,
				IMPERATIVE_ISSUE_FACTORY,
				null);
	}

	@Override
	public ImperativeBlock groupBraces(
			Group group,
			Distributor distributor,
			String name,
			Lambda<MemberRegistry, LocalScope> memberRegistry) {
		group.getLogger().prohibitedClauseDeclaration(group);
		return null;
	}

	@Override
	public DeclarativeSentence propose(
			LocationInfo location,
			DeclarativeBlock block) {
		return new DeclarativeIssue.Proposing(location, block, this);
	}

	@Override
	public DeclarativeSentence claim(
			LocationInfo location,
			DeclarativeBlock block) {
		return new DeclarativeIssue.Claiming(location, block, this);
	}

}
