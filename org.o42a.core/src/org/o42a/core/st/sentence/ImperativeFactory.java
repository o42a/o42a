/*
    Compiler Core
    Copyright (C) 2010-2013 Ruslan Lopatin

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

import static org.o42a.core.st.sentence.ImperativeBlock.nestedImperativeBlock;

import org.o42a.core.Distributor;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.impl.imperative.ImperativeClaim;
import org.o42a.core.st.impl.imperative.ImperativeIssue;
import org.o42a.core.st.impl.imperative.ImperativeProposition;
import org.o42a.util.string.Name;


public class ImperativeFactory extends SentenceFactory<
		Imperatives,
		ImperativeSentence,
		ImperativeBlock> {

	protected ImperativeFactory() {
	}

	@Override
	public ImperativeBlock createParentheses(
			LocationInfo location,
			Distributor distributor,
			Imperatives enclosing) {
		return nestedImperativeBlock(
				location,
				distributor,
				enclosing,
				true,
				null,
				enclosing.getMemberRegistry(),
				this);
	}

	@Override
	public ImperativeBlock groupParentheses(
			Group group,
			Distributor distributor,
			MemberRegistry memberRegistry) {
		group.getLogger().prohibitedClauseDeclaration(group.getLocation());
		return null;
	}

	@Override
	public ImperativeBlock createBraces(
			LocationInfo location,
			Distributor distributor,
			Imperatives enclosing,
			Name name) {
		return nestedImperativeBlock(
				location,
				distributor,
				enclosing,
				false,
				name,
				enclosing.getMemberRegistry(),
				this);
	}

	@Override
	public ImperativeBlock groupBraces(
			Group group,
			Distributor distributor,
			Name name,
			MemberRegistry memberRegistry) {
		group.getLogger().prohibitedClauseDeclaration(group.getLocation());
		return null;
	}

	@Override
	public ImperativeSentence issue(
			LocationInfo location,
			ImperativeBlock block) {
		return new ImperativeIssue(location, block);
	}

	@Override
	public Imperatives createAlternative(
			LocationInfo location,
			ImperativeSentence sentence) {
		return new Imperatives(location, sentence);
	}

	@Override
	public ImperativeSentence propose(
			LocationInfo location,
			ImperativeBlock block) {
		return new ImperativeProposition(location, block, this);
	}

	@Override
	public ImperativeSentence claim(
			LocationInfo location,
			ImperativeBlock block) {
		return new ImperativeClaim(location, block, this);
	}

	@Override
	public final DeclarativeFactory toDeclarativeFactory() {
		return null;
	}

	@Override
	public final ImperativeFactory toImperativeFactory() {
		return this;
	}

}
