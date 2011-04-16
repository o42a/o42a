/*
    Compiler
    Copyright (C) 2011 Ruslan Lopatin

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

import static org.o42a.compiler.ip.phrase.part.NextClause.errorClause;

import org.o42a.ast.expression.BinaryNode;
import org.o42a.compiler.ip.phrase.ref.PhraseContext;
import org.o42a.core.member.clause.ClauseId;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.st.sentence.Statements;


public class BinaryPhrasePart extends PhraseContinuation {

	private final BinaryNode node;
	private ClauseId clauseId;

	public BinaryPhrasePart(BinaryNode node, PhrasePart preceding) {
		super(preceding, preceding);
		this.node = node;
	}

	public final ClauseId getClauseId() {
		if (this.clauseId == null) {
			getPhrase().build();
		}
		return this.clauseId;
	}

	@Override
	public NextClause nextClause(PhraseContext context) {

		final ClauseId clauseId = clauseId();

		if (clauseId == null) {
			return errorClause(this.node);
		}

		final NextClause first = context.clauseById(this, clauseId);

		if (first.found()) {
			this.clauseId = clauseId;
			return first;
		}

		final NextClause second = findSecond(context);

		if (second != null && second.found()) {
			return second;
		}

		return first;
	}

	@Override
	public void define(Block<?> definition) {
		if (getPhrase().createsObject()) {
			// Phrase constructs object. No need to put the left operand.
			return;
		}

		final Statements<?> statements =
			definition.propose(this).alternative(this);

		statements.assign(getPhrase().getAncestor().getRef());
	}

	@Override
	public String toString() {
		return this.node.getOperator().getSign();
	}

	private ClauseId clauseId() {
		switch (this.node.getOperator()) {
		case ADD:
			return ClauseId.ADD;
		case SUBTRACT:
			return ClauseId.SUBTRACT;
		case MULTIPLY:
			return ClauseId.MULTIPLY;
		case DIVIDE:
			return ClauseId.DIVIDE;
		case EQUAL:
		case NOT_EQUAL:
			return ClauseId.EQUALS;
		case LESS:
		case LESS_OR_EQUAL:
		case GREATER:
		case GREATER_OR_EQUAL:
			return ClauseId.COMPARE;
		}

		getLogger().error(
				"unsupported_binary",
				this.node.getSign(),
				"Binary operator '%s' is not supported",
				this.node.getOperator().getSign());

		return null;
	}

	private NextClause findSecond(PhraseContext context) {
		switch (this.node.getOperator()) {
		case EQUAL:
		case NOT_EQUAL:
			this.clauseId = ClauseId.COMPARE;
			return context.clauseById(this, this.clauseId);
		default:
			return null;
		}
	}

}
