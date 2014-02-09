/*
    Compiler Commons
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
package org.o42a.common.phrase.part;

import org.o42a.common.phrase.PhraseContext;
import org.o42a.core.Distributor;
import org.o42a.core.member.clause.ClauseId;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.st.sentence.BlockBuilder;
import org.o42a.core.st.sentence.Statements;


public class PhraseImperative extends PhraseContinuation {

	private final BlockBuilder imperatives;

	PhraseImperative(PhrasePart preceding, BlockBuilder imperatives) {
		super(imperatives, preceding);
		this.imperatives = imperatives;
	}

	public final BlockBuilder getImperatives() {
		return this.imperatives;
	}

	@Override
	public NextClause nextClause(PhraseContext context) {
		return context.clauseById(this, ClauseId.IMPERATIVE);
	}

	@Override
	public Ref substitute(Distributor distributor) {
		return null;
	}

	@Override
	public void define(Block<?> definition) {

		final Statements<?> statements =
				definition.declare(this).alternative(this);

		this.imperatives.buildBlock(statements.braces(this));
	}

	@Override
	public String toString() {
		if (this.imperatives == null) {
			return "{}";
		}

		final String string = this.imperatives.toString();

		if (string.startsWith("{")) {
			return string;
		}

		return '{' + string + '}';
	}

}
