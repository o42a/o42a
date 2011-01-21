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
import static org.o42a.core.member.AdapterId.adapterId;
import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;
import static org.o42a.core.ref.path.PathBuilder.pathBuilder;
import static org.o42a.core.st.Conditions.emptyConditions;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.StaticTypeRef;
import org.o42a.core.artifact.common.IntrinsicObject;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.def.Definitions;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.PathBuilder;
import org.o42a.core.st.DefinitionTarget;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


public abstract class BinaryOpObj<T, L> extends IntrinsicObject {

	public static PathBuilder RIGHT_OPERAND =
		pathBuilder("operators", "binary_operator", "right_operand");

	private static FieldDeclaration declaration(
			Container enclosingContainer,
			StaticTypeRef adapterType,
			StaticTypeRef declaredIn) {

		final FieldDeclaration declaration =
			fieldDeclaration(
					enclosingContainer,
					declarativeDistributor(enclosingContainer),
					adapterId(adapterType)).prototype();

		if (declaredIn == null) {
			return declaration;
		}

		return declaration.override().setDeclaredIn(declaredIn);
	}

	private final ValueType<L> leftOperandType;
	private final String operator;

	public BinaryOpObj(
			Container enclosingContainer,
			StaticTypeRef adapterType,
			StaticTypeRef declaredIn,
			ValueType<T> resultType,
			ValueType<L> leftOperandType,
			String operator) {
		super(declaration(enclosingContainer, adapterType, declaredIn));
		setValueType(resultType);
		this.leftOperandType = leftOperandType;
		this.operator = operator;
	}

	@SuppressWarnings("unchecked")
	public final ValueType<T> getResultType() {
		return (ValueType<T>) getValueType();
	}

	public ValueType<L> getLeftOperandType() {
		return this.leftOperandType;
	}

	public String getOperator() {
		return this.operator;
	}

	@Override
	public String toString() {
		return ("Binary " + this.leftOperandType
				+ " " + this.operator
				+ "[" + getResultType() + "]");
	}

	@Override
	protected Ascendants createAscendants() {
		return new Ascendants(getScope()).setAncestor(
				getValueType().typeRef(
						this,
						getScope().getEnclosingScope()));
	}

	@Override
	protected Definitions explicitDefinitions() {

		final Ref selfOrDerived = selfOrDerived();

		selfOrDerived.setConditions(emptyConditions(this));

		return selfOrDerived.define(new DefinitionTarget(getScope()));
	}

	@Override
	protected Value<?> calculateValue(Scope scope) {

		final Artifact<?> leftOperand =
			getScope().getEnclosingScopePath().resolveArtifact(this, scope);
		final Field<?> rightOperand = getRightOperand().fieldOf(scope);
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
			getLogger().unsupportedRightOperand(
					rightOperand,
					rightType,
					getOperator());
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

	protected abstract PathBuilder getRightOperand();

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

}
