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
import org.o42a.core.st.impl.declarative.DeclarativeInterrogationFactory;
import org.o42a.core.st.impl.imperative.ImperativeGroupFactory;
import org.o42a.core.st.impl.imperative.ImperativeInterrogationFactory;
import org.o42a.util.string.Name;


public abstract class SentenceFactory {

	public static final DeclarativeFactory DECLARATIVE_FACTORY =
			new DeclarativeFactory();
	public static final ImperativeFactory IMPERATIVE_FACTORY =
			new ImperativeFactory();
	public static final DeclarativeFactory DECLARATIVE_INTERROGATION_FACTORY =
			new DeclarativeInterrogationFactory();
	public static final ImperativeFactory IMPERATIVE_INTERROGATION_FACTORY =
			new ImperativeInterrogationFactory();
	public static final ImperativeFactory IMPERATIVE_GROUP_FACTORY =
			new ImperativeGroupFactory();

	SentenceFactory() {
	}

	public final boolean isDeclarative() {
		return toDeclarativeFactory() != null;
	}

	public abstract Block createParentheses(
			LocationInfo location,
			Distributor distributor,
			Statements enclosing);

	public abstract Block groupParentheses(
			Group group,
			Distributor distributor,
			MemberRegistry memberRegistry);

	public abstract ImperativeBlock createBraces(
			LocationInfo location,
			Distributor distributor,
			Statements enclosing,
			Name name);

	public abstract ImperativeBlock groupBraces(
			Group group,
			Distributor distributor,
			Name name,
			MemberRegistry memberRegistry);

	public abstract Sentence declare(LocationInfo location, Block block);

	public abstract Sentence exit(LocationInfo location, Block block);

	public abstract Sentence interrogate(LocationInfo location, Block block);

	public abstract Statements createAlternative(
			LocationInfo location,
			Sentence sentence);

	public abstract DeclarativeFactory toDeclarativeFactory();

	public abstract ImperativeFactory toImperativeFactory();

}
