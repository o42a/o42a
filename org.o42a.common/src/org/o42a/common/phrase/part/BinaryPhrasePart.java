/*
    Compiler Commons
    Copyright (C) 2011-2014 Ruslan Lopatin

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
import org.o42a.core.Distributor;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.RefBuilder;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.st.sentence.Statements;


public class BinaryPhrasePart extends PhraseContinuation {

	private final BinaryPhraseOperator operator;
	private final RefBuilder rightOperand;
	private ComparisonOperator comparisonOperator;

	BinaryPhrasePart(
			LocationInfo location,
			BinaryPhraseOperator operator,
			PhrasePart preceding,
			RefBuilder rightOperand) {
		super(location, preceding);
		this.operator = operator;
		this.rightOperand = rightOperand;
	}

	public final ComparisonOperator getComparisonOperator() {
		if (this.comparisonOperator == null) {
			getPhrase().build();
		}
		return this.comparisonOperator;
	}

	@Override
	public NextClause nextClause(PhraseContext context) {

		final NextBinaryClause first =
				this.operator.findFirst(this, context);

		this.comparisonOperator = first.getComparisonOperator();
		if (first.getNextClause().found()) {
			return first.getNextClause();
		}

		final NextBinaryClause second =
				this.operator.findSecond(this, context);

		if (second == null || !second.found()) {
			return first.getNextClause();
		}
		this.comparisonOperator = second.getComparisonOperator();

		return second.getNextClause();
	}

	@Override
	public Ref substitute(Distributor distributor) {
		if (this.rightOperand == null) {
			return null;
		}
		return this.rightOperand.buildRef(distributor);
	}

	@Override
	public void define(Block<?> definition) {
		if (this.rightOperand == null) {
			return;// Do not assign any value.
		}

		final Statements<?> statements =
				definition.declare(this).alternative(this);

		statements.selfAssign(this.rightOperand);
	}

	@Override
	public String toString() {
		if (this.operator == null) {
			return super.toString();
		}
		return this.operator.getSign() + this.rightOperand;
	}

}
