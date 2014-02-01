/*
    Compiler Commons
    Copyright (C) 2013,2014 Ruslan Lopatin

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
import org.o42a.common.ref.cmp.ComparisonOperator;
import org.o42a.core.member.clause.ClauseId;


public enum BinaryPhraseOperator {

	ADD("+", ClauseId.ADD),
	SUBTRACT("-", ClauseId.SUBTRACT),
	MULTIPLY("*", ClauseId.MULTIPLY),
	DIVIDE("/", ClauseId.DIVIDE),
	COMPARE("<=>", ClauseId.COMPARE),
	EQUALS("==", ComparisonOperator.EQUALS, ComparisonOperator.COMPARE_EQUAL),
	NOT_EQUALS(
			"<>",
			ComparisonOperator.NOT_EQUALS,
			ComparisonOperator.COMPARE_NOT_EQUAL),
	GREATER(">", ComparisonOperator.GREATER),
	GREATER_OR_EQUAL(">=", ComparisonOperator.GREATER_OR_EQUAL),
	LESS("<", ComparisonOperator.LESS),
	LESS_OR_EQUAL("<=", ComparisonOperator.LESS_OR_EQUAL);

	private final String sign;
	private final ClauseId firstClauseId;
	private final ComparisonOperator comparisonOperator;
	private final ComparisonOperator equalityComparisonOperator;

	BinaryPhraseOperator(String sign, ClauseId firstClauseId) {
		this.sign = sign;
		this.firstClauseId = firstClauseId;
		this.comparisonOperator = null;
		this.equalityComparisonOperator = null;
	}

	BinaryPhraseOperator(
			String sign,
			ComparisonOperator comparisonOperator) {
		this.sign = sign;
		this.firstClauseId = comparisonOperator.getClauseId();
		this.comparisonOperator = comparisonOperator;
		this.equalityComparisonOperator = null;
	}

	BinaryPhraseOperator(
			String sign,
			ComparisonOperator comparisonOperator,
			ComparisonOperator equalityComparisonOperator) {
		this.sign = sign;
		this.firstClauseId = comparisonOperator.getClauseId();
		this.comparisonOperator = comparisonOperator;
		this.equalityComparisonOperator = equalityComparisonOperator;
	}

	public final String getSign() {
		return this.sign;
	}

	public final ClauseId getFirstClauseId() {
		return this.firstClauseId;
	}

	public final ComparisonOperator getComparisonOperator() {
		return this.comparisonOperator;
	}

	NextBinaryClause findFirst(
			BinaryPhrasePart part,
			PhraseContext context) {
		return new NextBinaryClause(
				context.clauseById(part, this.firstClauseId),
				this.comparisonOperator);
	}

	NextBinaryClause findSecond(
			BinaryPhrasePart part,
			PhraseContext context) {
		if (this.equalityComparisonOperator == null) {
			return null;
		}

		final NextClause found = context.clauseById(
				part,
				this.equalityComparisonOperator.getClauseId());

		return new NextBinaryClause(
				found,
				this.equalityComparisonOperator);
	}

}
