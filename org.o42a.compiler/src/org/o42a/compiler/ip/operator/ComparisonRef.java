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
package org.o42a.compiler.ip.operator;

import org.o42a.ast.expression.BinaryNode;
import org.o42a.common.adapter.BinaryOperatorInfo;
import org.o42a.core.CompilerContext;
import org.o42a.core.Distributor;
import org.o42a.core.LocationSpec;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


abstract class ComparisonRef extends CompareOperatorRef {

	public ComparisonRef(
			CompilerContext context,
			BinaryNode node,
			Distributor distributor) {
		super(context, node, distributor);
	}

	public ComparisonRef(
			LocationSpec location,
			Distributor distributor,
			BinaryNode node,
			Ref leftOperand,
			Ref rightOperand) {
		super(location, distributor, node, leftOperand, rightOperand);
	}

	public ComparisonRef(
			CompareOperatorRef prototype,
			Reproducer reproducer,
			Ref leftOperand,
			Ref rightOperand) {
		super(prototype, reproducer, leftOperand, rightOperand);
	}

	@Override
	protected BinaryOperatorRef createOperator(
			Distributor distributor,
			Ref leftOperand,
			Ref rightOperand) {
		return new CompareBinary(
				this,
				distributor,
				getNode(),
				leftOperand,
				rightOperand);
	}

	@Override
	protected boolean result(Value<?> value) {
		if (value.isFalse()) {
			// Value is false.
			return false;
		}

		final Long compareResult = ValueType.INTEGER.definiteValue(value);

		return result(compareResult);
	}

	protected abstract boolean result(Long value);

	static final class LessOp extends ComparisonRef {

		public LessOp(
				CompilerContext context,
				BinaryNode node,
				Distributor distributor) {
			super(context, node, distributor);
		}

		LessOp(
				CompareOperatorRef prototype,
				Reproducer reproducer,
				Ref leftOperand,
				Ref rightOperand) {
			super(prototype, reproducer, leftOperand, rightOperand);
		}

		@Override
		protected boolean result(Long value) {
			return value < 0;
		}

		@Override
		protected CompareOperatorRef reproduce(
				Reproducer reproducer,
				Ref leftOperand,
				Ref rightOperand) {
			return new LessOp(this, reproducer, leftOperand, rightOperand);
		}

	}

	static final class LessOrEqual extends ComparisonRef {

		public LessOrEqual(
				CompilerContext context,
				BinaryNode node,
				Distributor distributor) {
			super(context, node, distributor);
		}

		LessOrEqual(
				CompareOperatorRef prototype,
				Reproducer reproducer,
				Ref leftOperand,
				Ref rightOperand) {
			super(prototype, reproducer, leftOperand, rightOperand);
		}

		@Override
		protected boolean result(Long value) {
			return value <= 0;
		}

		@Override
		protected CompareOperatorRef reproduce(
				Reproducer reproducer,
				Ref leftOperand,
				Ref rightOperand) {
			return new LessOrEqual(this, reproducer, leftOperand, rightOperand);
		}

	}

	static final class Greater extends ComparisonRef {

		public Greater(
				CompilerContext context,
				BinaryNode node,
				Distributor distributor) {
			super(context, node, distributor);
		}

		Greater(
				CompareOperatorRef prototype,
				Reproducer reproducer,
				Ref leftOperand,
				Ref rightOperand) {
			super(prototype, reproducer, leftOperand, rightOperand);
		}

		@Override
		protected boolean result(Long value) {
			return value > 0;
		}

		@Override
		protected CompareOperatorRef reproduce(
				Reproducer reproducer,
				Ref leftOperand,
				Ref rightOperand) {
			return new Greater(this, reproducer, leftOperand, rightOperand);
		}

	}

	static final class GreaterOrEqual extends ComparisonRef {

		public GreaterOrEqual(
				CompilerContext context,
				BinaryNode node,
				Distributor distributor) {
			super(context, node, distributor);
		}

		public GreaterOrEqual(
				CompareOperatorRef prototype,
				Reproducer reproducer,
				Ref leftOperand,
				Ref rightOperand) {
			super(prototype, reproducer, leftOperand, rightOperand);
		}

		@Override
		protected boolean result(Long value) {
			return value >= 0;
		}

		@Override
		protected CompareOperatorRef reproduce(
				Reproducer reproducer,
				Ref leftOperand,
				Ref rightOperand) {
			return new GreaterOrEqual(
					this,
					reproducer,
					leftOperand,
					rightOperand);
		}

	}

	static final class EqualComparison extends ComparisonRef {

		public EqualComparison(
				LocationSpec location,
				Distributor distributor,
				BinaryNode node,
				Ref leftOperand,
				Ref rightOperand) {
			super(location, distributor, node, leftOperand, rightOperand);
		}

		EqualComparison(
				CompareOperatorRef prototype,
				Reproducer reproducer,
				Ref leftOperand,
				Ref rightOperand) {
			super(prototype, reproducer, leftOperand, rightOperand);
		}

		@Override
		protected boolean result(Long value) {
			return value == 0;
		}

		@Override
		protected CompareOperatorRef reproduce(
				Reproducer reproducer,
				Ref leftOperand,
				Ref rightOperand) {
			return new EqualComparison(
					this,
					reproducer,
					leftOperand,
					rightOperand);
		}

	}

	static final class NotEqualComparison extends ComparisonRef {

		public NotEqualComparison(
				LocationSpec location,
				Distributor distributor,
				BinaryNode node,
				Ref leftOperand,
				Ref rightOperand) {
			super(location, distributor, node, leftOperand, rightOperand);
		}

		NotEqualComparison(
				CompareOperatorRef prototype,
				Reproducer reproducer,
				Ref leftOperand,
				Ref rightOperand) {
			super(prototype, reproducer, leftOperand, rightOperand);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected boolean result(Long value) {
			return value != 0;
		}

		@Override
		protected CompareOperatorRef reproduce(
				Reproducer reproducer,
				Ref leftOperand,
				Ref rightOperand) {
			return new NotEqualComparison(
					this,
					reproducer,
					leftOperand,
					rightOperand);
		}

	}

	private static final class CompareBinary extends BinaryOperatorRef {

		CompareBinary(
				CompilerContext context,
				BinaryNode node,
				Distributor distributor) {
			super(context, node, distributor);
		}

		CompareBinary(
				LocationSpec location,
				Distributor distributor,
				BinaryNode node,
				Ref leftOperand,
				Ref rightOperand) {
			super(location, distributor, node, leftOperand, rightOperand);
		}

		@Override
		protected BinaryOperatorInfo getInfo() {
			return BinaryOperatorInfo.COMPARE;
		}

	}

}
