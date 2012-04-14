/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.core.st.impl.declarative;

import org.o42a.core.Distributor;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.*;
import org.o42a.util.fn.Lambda;


public class DeclarativeIssueFactory extends DeclarativeFactory {

	public static void prohibitedIssueBraces(
			LocationInfo location,
			Statements<?, ?> enclosing) {
		enclosing.getLogger().error(
				"prohibited_issue_braces",
				location,
				"Issue can not contain braces");
	}

	public static void prohibitedIssueAlt(
			LocationInfo location,
			Sentence<?, ?> sentence) {
		sentence.getLogger().error(
				"prohibited_issue_alt",
				location,
				"Issue can only contain opposites. Use '|' instead of ';'");
	}

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
		prohibitedIssueBraces(location, enclosing);
		return null;
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
		return new DeclarativeIssue(location, block);
	}

	@Override
	public Declaratives createAlternative(
			LocationInfo location,
			DeclarativeSentence sentence,
			Declaratives oppositeOf,
			boolean inhibit) {
		if (oppositeOf == null && !sentence.getAlternatives().isEmpty()) {
			prohibitedIssueAlt(location, sentence);
			return null;
		}
		return super.createAlternative(location, sentence, oppositeOf, inhibit);
	}

	@Override
	public DeclarativeSentence claim(
			LocationInfo location,
			DeclarativeBlock block) {
		block.getLogger().error(
				"prohibited_issue_claim",
				location,
				"A claim can not be placed inside an issue");
		return null;
	}

}
