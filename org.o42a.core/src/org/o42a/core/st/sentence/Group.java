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

import static org.o42a.core.st.impl.SentenceErrors.prohibitedIssueBraces;

import org.o42a.core.Contained;
import org.o42a.core.member.clause.ClauseBuilder;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.string.Name;


public final class Group extends Contained {

	private final ClauseBuilder builder;
	private final Statements<?> statements;

	Group(
			LocationInfo location,
			Statements<?> statements,
			ClauseBuilder builder) {
		super(location, statements.nextDistributor());
		this.statements = statements;
		this.builder = builder;
	}

	public final Statements<?> getStatements() {
		return this.statements;
	}

	public final ClauseBuilder getBuilder() {
		return this.builder;
	}

	public final Block<?> parentheses() {

		final ClauseBuilderBase builder = this.builder;

		return builder.parentheses(this);
	}

	public final ImperativeBlock braces(Name name) {
		if (this.statements.isInsideIssue()) {
			prohibitedIssueBraces(this);
			this.statements.dropStatement();
			return null;
		}

		final ClauseBuilderBase builder = this.builder;

		return builder.braces(this, name);
	}

	@Override
	public String toString() {
		return "Group[" + this.builder.getDeclaration() + "]";
	}

}
