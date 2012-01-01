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

import static org.o42a.compiler.ip.operator.ComparisonOperator.comparisonOperator;
import static org.o42a.compiler.ip.operator.ComparisonOperator.equalityOperator;
import static org.o42a.compiler.ip.phrase.part.NextClause.errorClause;

import org.o42a.ast.expression.BinaryNode;
import org.o42a.ast.expression.BinaryOperator;
import org.o42a.compiler.ip.operator.ComparisonOperator;
import org.o42a.compiler.ip.phrase.ref.PhraseContext;
import org.o42a.core.Distributor;
import org.o42a.core.member.clause.ClauseId;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.st.sentence.Statements;


public class BinaryPhrasePart extends PhraseContinuation {

	private final BinaryNode node;
	private ComparisonOperator comparisonOperator;

	public BinaryPhrasePart(BinaryNode node, PhrasePart preceding) {
		super(preceding, preceding);
		this.node = node;
	}

	public final ComparisonOperator getComparisonOperator() {
		if (this.comparisonOperator == null) {
			getPhrase().build();
		}
		return this.comparisonOperator;
	}

	@Override
	public NextClause nextClause(PhraseContext context) {

		final NextClause first = findFirst(context);

		if (first == null) {
			return errorClause(this.node);
		}
		if (first.found()) {
			return first;
		}

		final NextClause second = findSecond(context);

		if (second != null && second.found()) {
			return second;
		}

		return first;
	}

	@Override
	public Ref substitute(Distributor distributor) {
		return getPhrase().getAncestor().getRescopedRef().rescope(
				distributor.getScope());
	}

	@Override
	public void define(Block<?> definition) {
		if (getPhrase().createsObject()) {
			// Phrase constructs object. No need to put the left operand.
			return;
		}

		final Statements<?> statements =
				definition.propose(this).alternative(this);

		statements.selfAssign(getPhrase().getAncestor().getRescopedRef());
	}

	@Override
	public String toString() {
		return this.node.getOperator().getSign();
	}

	private NextClause findFirst(PhraseContext context) {

		final BinaryOperator operator = this.node.getOperator();
		final ClauseId clauseId;

		if (operator.isArithmetic()) {
			switch (operator) {
			case ADD:
				clauseId = ClauseId.ADD;
				break;
			case SUBTRACT:
				clauseId = ClauseId.SUBTRACT;
				break;
			case MULTIPLY:
				clauseId = ClauseId.MULTIPLY;
				break;
			case DIVIDE:
				clauseId = ClauseId.DIVIDE;
				break;
			default:
				clauseId = null;
				break;
			}
		} else {
			if (operator.isEquality()) {
				this.comparisonOperator = equalityOperator(operator);
			} else {
				this.comparisonOperator = comparisonOperator(operator);
			}
			if (this.comparisonOperator != null) {
				clauseId = this.comparisonOperator.getClauseId();
			} else {
				clauseId = null;
			}
		}

		if (clauseId == null) {
			getLogger().error(
					"unsupported_binary",
					this.node.getSign(),
					"Binary operator '%s' is not supported",
					this.node.getOperator().getSign());
			return null;
		}

		return context.clauseById(this, clauseId);
	}

	private NextClause findSecond(PhraseContext context) {

		final BinaryOperator operator = this.node.getOperator();

		if (!operator.isEquality()) {
			return null;
		}

		final ComparisonOperator comparisonOperator =
				comparisonOperator(operator);

		if (comparisonOperator == null) {
			return null;
		}

		final NextClause found =
				context.clauseById(this, comparisonOperator.getClauseId());

		this.comparisonOperator = comparisonOperator;

		return found;
	}

}
