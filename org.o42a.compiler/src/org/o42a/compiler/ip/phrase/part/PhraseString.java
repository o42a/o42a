/*
    Compiler
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
package org.o42a.compiler.ip.phrase.part;

import org.o42a.compiler.ip.phrase.ref.PhraseContext;
import org.o42a.core.LocationInfo;
import org.o42a.core.member.clause.ClauseId;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.st.sentence.Statements;
import org.o42a.core.value.ValueType;


public class PhraseString extends PhraseContinuation {

	private final String string;

	PhraseString(
			LocationInfo location,
			PhrasePart preceding,
			String string) {
		super(location, preceding);
		this.string = string;
	}

	public final String getString() {
		return this.string;
	}

	@Override
	public NextClause nextClause(PhraseContext context) {
		return context.clauseById(this, ClauseId.STRING);
	}

	@Override
	public void define(Block<?> definition) {

		final Statements<?> statements =
			definition.propose(this).alternative(this);

		statements.assign(ValueType.STRING.definiteRef(
				this,
				statements.nextDistributor(),
				this.string));
	}

	@Override
	public String toString() {
		return '\'' + this.string + '\'';
	}

}
