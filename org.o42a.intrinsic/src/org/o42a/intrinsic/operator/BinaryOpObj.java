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

import static org.o42a.core.Distributor.declarativeDistributor;
import static org.o42a.core.ir.op.CodeDirs.falseWhenUnknown;
import static org.o42a.core.ir.op.ValOp.VAL_TYPE;
import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;
import static org.o42a.core.ref.path.Path.SELF_PATH;
import static org.o42a.core.st.StatementEnv.defaultEnv;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodeBlk;
import org.o42a.common.adapter.BinaryOperatorInfo;
import org.o42a.common.intrinsic.IntrinsicObject;
import org.o42a.core.*;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.member.AdapterId;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.util.log.LoggableData;


public abstract class BinaryOpObj<T, L> extends IntrinsicObject {

	private static FieldDeclaration declaration(
			Container enclosingContainer,
			BinaryOperatorInfo operator,
			StaticTypeRef declaredIn) {

		final Location location = new Location(
				enclosingContainer.getContext(),
				new LoggableData("<ROOT>"));
		final Distributor distributor =
			declarativeDistributor(enclosingContainer);
		final AdapterId adapterId =
			operator.getPath().toAdapterId(location, distributor);
		final FieldDeclaration declaration =
			fieldDeclaration(location, distributor, adapterId).prototype();

		if (declaredIn == null) {
			return declaration;
		}

		return declaration.override().setDeclaredIn(declaredIn);
	}

	private final ValueType<L> leftOperandType;
	private final BinaryOperatorInfo operator;
	private Ref left;

	public BinaryOpObj(
			Container enclosingContainer,
			BinaryOperatorInfo operator,
			StaticTypeRef declaredIn,
			ValueType<T> resultType,
			ValueType<L> leftOperandType) {
		super(declaration(enclosingContainer, operator, declaredIn));
		setValueType(resultType);
		this.leftOperandType = leftOperandType;
		this.operator = operator;
	}

	@SuppressWarnings("unchecked")
	public final ValueType<T> getResultType() {
		return (ValueType<T>) getValueType();
	}

	public final ValueType<L> getLeftOperandType() {
		return this.leftOperandType;
	}

	public final BinaryOperatorInfo getOperator() {
		return this.operator;
	}

	@Override
	public void resolveAll() {
		super.resolveAll();
		left();
	}

	@Override
	public String toString() {
		return ("Binary " + this.leftOperandType
				+ " " + this.operator.getSign()
				+ "[" + getResultType() + "]");
	}

	@Override
	protected Ascendants createAscendants() {

		final Scope enclosing = getScope().getEnclosingScope();

		return new Ascendants(this).setAncestor(
				SELF_PATH.target(this, enclosing.distribute()).toTypeRef());
	}

	@Override
	protected Definitions explicitDefinitions() {

		final Ref self = selfRef();

		self.setEnv(defaultEnv(this));

		return self.define(getScope());
	}

	@Override
	protected Value<?> calculateValue(Scope scope) {

		final Artifact<?> leftOperand = left().resolve(scope).toArtifact();
		final Field<?> rightOperand =
			getOperator().getRightOperand().fieldOf(scope);
		final Value<?> leftValue =
			leftOperand.materialize().getValue();
		final Value<?> rightValue =
			rightOperand.getArtifact().materialize().getValue();

		if (leftValue.getLogicalValue().isFalse()
				|| rightValue.getLogicalValue().isFalse()) {
			// one of operands is false - result is false too
			return getResultType().falseValue();
		}
		if (!leftValue.isDefinite() || !rightValue.isDefinite()) {
			// one of operands can not be resolved at compile time
			// result is run-time too
			return getResultType().runtimeValue();
		}

		if (leftValue.isVoid() || rightValue.isVoid()) {
			return getResultType().runtimeValue();
		}

		@SuppressWarnings("rawtypes")
		final ValueType rightType = rightValue.getValueType();

		if (!rightOperandSupported(rightType)) {
			getLogger().error(
					"unsupported_right_operand",
					rightOperand,
					"Right operand of type '%s'"
					+ " is not supported by operator '%s'",
					rightType,
					getOperator().getSign());
			return getResultType().falseValue();
		}

		@SuppressWarnings("unchecked")
		final Value<?> result = buildResult(
				scope,
				getLeftOperandType().definiteValue(leftValue),
				rightType,
				rightType.cast(rightValue.getDefiniteValue()));

		if (result == null) {
			return getResultType().falseValue();
		}

		return result;
	}

	protected abstract boolean rightOperandSupported(ValueType<?> valueType);

	protected <R> Value<T> buildResult(
			Scope scope,
			L left,
			ValueType<R> rightType,
			R right) {

		final T res = calculate(scope, left, rightType, right);

		if (res == null) {
			return null;
		}

		return getResultType().definiteValue(res);
	}

	protected abstract <R> T calculate(
			Scope scope,
			L left,
			ValueType<R> rightType,
			R right);

	@Override
	protected ObjectValueIR createValueIR(ObjectIR objectIR) {
		return new ValueIR(objectIR);
	}

	protected abstract void calculate(
			CodeDirs dirs,
			ObjectOp host,
			ValOp leftVal,
			ValOp rightVal);

	final Ref left() {
		if (this.left != null) {
			return this.left;
		}
		return this.left =
			getScope().getEnclosingScopePath().target(this, distribute());
	}

	private static final class ValueIR extends ProposedValueIR {

		ValueIR(ObjectIR objectIR) {
			super(objectIR);
		}

		@Override
		protected void proposition(Code code, ValOp result, ObjectOp host) {

			final BinaryOpObj<?, ?> object =
				(BinaryOpObj<?, ?>) getObjectIR().getObject();
			final RefOp left = object.left().op(host);
			final CodeBlk failure = code.addBlock("binary_failure");
			final CodeDirs dirs = falseWhenUnknown(code, failure.head());
			final ValOp rightVal =
				code.allocate(code.id("right_val"), VAL_TYPE);

			left.writeValue(dirs, result);

			final FldOp right = host.field(
					dirs,
					object.getOperator().getRightOperand().memberKey(
							object.getContext()));

			rightVal.storeIndefinite(code);
			right.materialize(dirs).writeValue(dirs, rightVal);

			object.calculate(dirs, host, result, rightVal);

			if (failure.exists()) {
				result.storeFalse(failure);
				failure.go(code.tail());
			}
		}

	}

}
