/*
    Intrinsics
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.intrinsic.operator;

import static org.o42a.core.ref.path.Path.absolutePath;

import org.o42a.ast.expression.BinaryOperator;
import org.o42a.core.CompilerContext;
import org.o42a.core.Scope;
import org.o42a.core.artifact.intrinsic.IntrinsicObject;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.path.AbsolutePath;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.Void;
import org.o42a.intrinsic.root.FloatObject;
import org.o42a.intrinsic.root.IntegerObject;


public abstract class NumericEqualsOpObj<L extends Number>
		extends BinaryOpObj<org.o42a.core.value.Void, L> {

	private static MemberKey equalsToKey;

	public static MemberKey getEqualsToKey(CompilerContext context) {
		if (equalsToKey != null) {
			if (context.compatible(equalsToKey.getOrigin())) {
				return equalsToKey;
			}
		}

		final AbsolutePath path =
			absolutePath(context, "operators", "equals", "to");
		final Field<?> rightOperand =
			path.resolveArtifact(context).getScope().toField();

		return equalsToKey = rightOperand.getKey();
	}

	public NumericEqualsOpObj(
			IntrinsicObject owner,
			ValueType<L> leftOperandType) {
		super(
				owner.getContainer(),
				absolutePath(
						owner.getContext(),
						"operators",
						"equals")
						.target(owner.getScope()).toStaticTypeRef(),
				owner.getAncestor().toStatic(),
				ValueType.VOID,
				leftOperandType,
				BinaryOperator.EQUAL.getSign());
	}

	@Override
	protected MemberKey rightOperandKey() {
		return getEqualsToKey(getContext());
	}

	@Override
	protected boolean rightOperandSupported(ValueType<?> valueType) {

		final Class<?> valueClass = valueType.getValueClass();

		return Number.class.isAssignableFrom(valueClass);
	}

	@Override
	protected final <R> Void calculate(
			Scope scope,
			L left,
			ValueType<R> rightType,
			R right) {
		if (!compare(left, (Number) right)) {
			return null;
		}
		return Void.VOID;
	}

	protected abstract boolean compare(L left, Number right);

	public static class IntegerEquals extends NumericEqualsOpObj<Long> {

		public IntegerEquals(IntegerObject owner) {
			super(owner, ValueType.INTEGER);
		}

		@Override
		protected boolean compare(Long left, Number right) {
			return left.longValue() == right.longValue();
		}

	}

	public static class FloatEquals extends NumericEqualsOpObj<Double> {

		public FloatEquals(FloatObject owner) {
			super(owner, ValueType.FLOAT);
		}

		@Override
		protected boolean compare(Double left, Number right) {
			return left.doubleValue() == right.doubleValue();
		}

	}

}
