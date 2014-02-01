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

import org.o42a.common.ref.cmp.ComparisonOperator;


final class NextBinaryClause {

	private final NextClause nextClause;
	private final ComparisonOperator comparisonOperator;

	NextBinaryClause(
			NextClause nextClause,
			ComparisonOperator comparisonOperator) {
		this.nextClause = nextClause;
		this.comparisonOperator = comparisonOperator;
	}

	public final boolean found() {
		return getNextClause() != null && getNextClause().found();
	}

	public final NextClause getNextClause() {
		return this.nextClause;
	}

	public final ComparisonOperator getComparisonOperator() {
		return this.comparisonOperator;
	}

	@Override
	public String toString() {
		if (this.nextClause == null) {
			return "NotFound";
		}
		return this.nextClause.toString();
	}

}
