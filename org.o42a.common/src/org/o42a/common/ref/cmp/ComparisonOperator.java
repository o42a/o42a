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
package org.o42a.common.ref.cmp;

import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.clause.ClauseId;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


public abstract class ComparisonOperator {

	public static final ComparisonOperator EQUALS = new EqualsOperator();
	public static final ComparisonOperator NOT_EQUALS =
			new NotEqualsOperator();
	public static final ComparisonOperator COMPARE_EQUAL =
			new CompareEqualOperator();
	public static final ComparisonOperator COMPARE_NOT_EQUAL =
			new CompareNotEqualOperator();
	public static final ComparisonOperator GREATER = new GreaterOperator();
	public static final ComparisonOperator GREATER_OR_EQUAL =
			new GreaterOrEqualOperator();
	public static final ComparisonOperator LESS = new LessOperator();
	public static final ComparisonOperator LESS_OR_EQUAL =
			new LessOrEqualOperator();

	private final ClauseId clauseId;

	public ComparisonOperator(ClauseId clauseId) {
		this.clauseId = clauseId;
	}

	public final ClauseId getClauseId() {
		return this.clauseId;
	}

	public abstract ValueType<?> getValueType();

	/**
	 * Validate the phrase against operator considerations.
	 *
	 * @param phrase the phrase to validate.
	 * @param resolutionLogger a logger to report resolution errors to.
	 *
	 * @return <code>true</code> if error found,
	 * or <code>false</code> otherwise.
	 */
	public boolean checkForErrors(Ref phrase, CompilerLogger resolutionLogger) {
		return false;
	}

	public abstract boolean result(Value<?> value);

	public ValOp writeComparison(ValDirs dirs, RefOp cmp) {
		return cmp.writeValue(dirs);
	}

	public ValOp inlineComparison(
			ValDirs dirs,
			HostOp host,
			InlineValue cmp) {
		return cmp.writeValue(dirs, host);
	}

	public abstract ValOp write(ValDirs dirs, ValOp comparisonVal);

}
