/*
    Compiler
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.phrase.part.NextClause.errorClause;

import org.o42a.ast.expression.UnaryNode;
import org.o42a.compiler.ip.phrase.ref.PhraseContext;
import org.o42a.core.Distributor;
import org.o42a.core.member.clause.ClauseId;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.sentence.Block;


public class UnaryPhrasePart extends PhraseContinuation {

	private final UnaryNode node;

	public UnaryPhrasePart(UnaryNode node, PhrasePart preceding) {
		super(location(preceding.getPhrase(), node.getSign()), preceding);
		this.node = node;
	}

	@Override
	public NextClause nextClause(PhraseContext context) {

		final ClauseId clauseId = clauseId();

		if (clauseId == null) {
			return errorClause(this.node);
		}

		return context.clauseById(this, clauseId);
	}

	@Override
	public Ref substitute(Distributor distributor) {
		return getPhrase().getAncestor().getRef().rescope(
				distributor.getScope());
	}

	@Override
	public void define(Block<?> definition) {
	}

	@Override
	public String toString() {
		return this.node.getOperator().getSign();
	}

	private ClauseId clauseId() {
		switch (this.node.getOperator()) {
		case PLUS:
			return ClauseId.PLUS;
		case MINUS:
			return ClauseId.MINUS;
		case IS_TRUE:
		case NOT:
		case KNOWN:
		case UNKNOWN:
		}

		getLogger().error(
				"unsupported_unary",
				this.node.getSign(),
				"Unary operator '%s' is not supported",
				this.node.getOperator().getSign());

		return null;
	}

}
