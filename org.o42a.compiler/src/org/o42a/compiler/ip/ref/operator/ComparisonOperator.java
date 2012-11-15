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
package org.o42a.compiler.ip.ref.operator;

import static org.o42a.ast.expression.BinaryOperator.*;

import java.util.EnumMap;

import org.o42a.ast.expression.BinaryOperator;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.clause.ClauseId;
import org.o42a.core.ref.Ref;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


public abstract class ComparisonOperator {

	private static final ComparisonOperator EQUALS = new EqualsOperator();
	private static final ComparisonOperator NOT_EQUALS =
			new NotEqualsOperator();

	private static final EnumMap<BinaryOperator, CompareOperator> operators =
			new EnumMap<BinaryOperator, CompareOperator>(BinaryOperator.class);

	static {
		operators.put(NOT_EQUAL, new CompareNotEqualOperator());
		operators.put(EQUAL, new CompareEqualOperator());
		operators.put(GREATER, new GreaterOperator());
		operators.put(GREATER_OR_EQUAL, new GreaterOrEqualOperator());
		operators.put(LESS, new LessOperator());
		operators.put(LESS_OR_EQUAL, new LessOrEqualOperator());
	}

	public static ComparisonOperator comparisonOperator(
			BinaryOperator operator) {
		return operators.get(operator);
	}

	public static ComparisonOperator equalityOperator(BinaryOperator operator) {
		if (operator == EQUAL) {
			return EQUALS;
		}
		if (operator == NOT_EQUAL) {
			return NOT_EQUALS;
		}
		return null;
	}

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
	 *
	 * @return <code>true</code> if error found,
	 * or <code>false</code> otherwise.
	 */
	public boolean checkError(Ref phrase) {
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
