/*
    Compiler
    Copyright (C) 2011-2013 Ruslan Lopatin

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
import org.o42a.core.Distributor;
import org.o42a.core.member.clause.ClauseId;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.st.sentence.Statements;


public class OperandPhrasePart extends PhraseContinuation {

	private final Ref value;

	public OperandPhrasePart(Ref value, PhrasePart preceding) {
		super(value, preceding);
		this.value = value;
	}

	@Override
	public NextClause nextClause(PhraseContext context) {
		return context.clauseById(this, ClauseId.ARGUMENT);
	}

	@Override
	public Ref substitute(Distributor distributor) {
		if (this.value == null) {
			return null;
		}
		return this.value.rescope(distributor.getScope());
	}

	@Override
	public void define(Block<?, ?> definition) {
		if (this.value == null) {
			return;// Do not assign operand value.
		}

		final Statements<?, ?> statements =
				definition.propose(this).alternative(this);

		statements.selfAssign(this.value);
	}

	@Override
	public String toString() {
		return this.value.toString();
	}

}
