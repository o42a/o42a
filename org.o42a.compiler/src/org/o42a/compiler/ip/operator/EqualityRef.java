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
import org.o42a.core.Distributor;
import org.o42a.core.LocationSpec;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.Value;


abstract class EqualityRef extends CompareOperatorRef {

	protected EqualityRef(
			CompareOperatorRef prototype,
			Reproducer reproducer,
			Ref leftOperand,
			Ref rightOperand) {
		super(prototype, reproducer, leftOperand, rightOperand);
	}

	public EqualityRef(
			LocationSpec location,
			Distributor distributor,
			BinaryNode node,
			Ref leftOperand,
			Ref rightOperand) {
		super(location, distributor, node, leftOperand, rightOperand);
	}

	@Override
	protected BinaryOperatorRef createOperator(
			Distributor distributor,
			Ref leftOperand,
			Ref rightOperand) {
		return new EqualBinary(
				this,
				distributor,
				getNode(),
				leftOperand,
				rightOperand);
	}

	@Override
	protected boolean result(Value<?> value) {
		return result(!value.isFalse());
	}

	protected abstract boolean result(boolean value);

	static final class Equal extends EqualityRef {

		public Equal(
				LocationSpec location,
				Distributor distributor,
				BinaryNode node,
				Ref leftOperand,
				Ref rightOperand) {
			super(location, distributor, node, leftOperand, rightOperand);
		}

		Equal(
				CompareOperatorRef prototype,
				Reproducer reproducer,
				Ref leftOperand,
				Ref rightOperand) {
			super(prototype, reproducer, leftOperand, rightOperand);
		}

		@Override
		protected boolean result(boolean value) {
			return value;
		}

		@Override
		protected CompareOperatorRef reproduce(
				Reproducer reproducer,
				Ref leftOperand,
				Ref rightOperand) {
			return new Equal(this, reproducer, leftOperand, rightOperand);
		}

	}

	static final class NotEqual extends EqualityRef {

		public NotEqual(
				LocationSpec location,
				Distributor distributor,
				BinaryNode node,
				Ref leftOperand,
				Ref rightOperand) {
			super(location, distributor, node, leftOperand, rightOperand);
		}

		NotEqual(
				CompareOperatorRef prototype,
				Reproducer reproducer,
				Ref leftOperand,
				Ref rightOperand) {
			super(prototype, reproducer, leftOperand, rightOperand);
		}

		@Override
		protected boolean result(boolean value) {
			return !value;
		}

		@Override
		protected CompareOperatorRef reproduce(
				Reproducer reproducer,
				Ref leftOperand,
				Ref rightOperand) {
			return new NotEqual(this, reproducer, leftOperand, rightOperand);
		}

	}

	private static final class EqualBinary extends BinaryOperatorRef {

		EqualBinary(
				LocationSpec location,
				Distributor distributor,
				BinaryNode node,
				Ref leftOperand,
				Ref rightOperand) {
			super(location, distributor, node, leftOperand, rightOperand);
		}

		@Override
		protected BinaryOperatorInfo getInfo() {
			return BinaryOperatorInfo.EQUAL;
		}

	}

}
