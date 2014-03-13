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
package org.o42a.core.st.sentence;

import static org.o42a.core.st.sentence.ImperativeBlock.nestedImperativeBlock;

import org.o42a.core.Distributor;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.impl.DefaultSentence;
import org.o42a.core.st.impl.InterrogativeSentence;
import org.o42a.core.st.impl.imperative.LoopExitSentence;
import org.o42a.util.string.Name;


public class ImperativeFactory extends SentenceFactory {

	protected ImperativeFactory() {
	}

	@Override
	public ImperativeBlock createParentheses(
			LocationInfo location,
			Distributor distributor,
			Statements enclosing) {
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
			Statements enclosing,
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
	public Sentence interrogate(LocationInfo location, Block block) {
		return new InterrogativeSentence(
				location,
				block,
				IMPERATIVE_INTERROGATION_FACTORY);
	}

	@Override
	public Statements createAlternative(
			LocationInfo location,
			Sentence sentence) {
		return new Statements(location, sentence);
	}

	@Override
	public Sentence declare(LocationInfo location, Block block) {
		return new DefaultSentence(location, block, this);
	}

	@Override
	public Sentence exit(LocationInfo location, Block block) {
		return new LoopExitSentence(location, block, this);
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
