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

import org.o42a.core.Distributor;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.impl.declarative.DeclarativeIssueFactory;
import org.o42a.core.st.impl.imperative.ImperativeGroupFactory;
import org.o42a.core.st.impl.imperative.ImperativeIssueFactory;
import org.o42a.util.string.Name;


public abstract class SentenceFactory<
		S extends Statements<S>,
		T extends Sentence<S>,
		B extends Block<S>> {

	public static final DeclarativeFactory DECLARATIVE_FACTORY =
			new DeclarativeFactory();
	public static final ImperativeFactory IMPERATIVE_FACTORY =
			new ImperativeFactory();
	public static final DeclarativeFactory DECLARATIVE_ISSUE_FACTORY =
			new DeclarativeIssueFactory();
	public static final ImperativeFactory IMPERATIVE_ISSUE_FACTORY =
			new ImperativeIssueFactory();
	public static final ImperativeFactory IMPERATIVE_GROUP_FACTORY =
			new ImperativeGroupFactory();

	SentenceFactory() {
	}

	public final boolean isDeclarative() {
		return toDeclarativeFactory() != null;
	}

	public abstract B createParentheses(
			LocationInfo location,
			Distributor distributor,
			S enclosing);

	public abstract B groupParentheses(
			Group group,
			Distributor distributor,
			MemberRegistry memberRegistry);

	public abstract ImperativeBlock createBraces(
			LocationInfo location,
			Distributor distributor,
			S enclosing,
			Name name);

	public abstract ImperativeBlock groupBraces(
			Group group,
			Distributor distributor,
			Name name,
			MemberRegistry memberRegistry);

	public abstract T propose(LocationInfo location, B block);

	public abstract T claim(LocationInfo location, B block);

	public abstract T issue(LocationInfo location, B block);

	public abstract S createAlternative(LocationInfo location, T sentence);

	public abstract DeclarativeFactory toDeclarativeFactory();

	public abstract ImperativeFactory toImperativeFactory();

}
