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
import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;
import static org.o42a.core.ref.path.Path.SELF_PATH;
import static org.o42a.core.st.Conditions.emptyConditions;

import org.o42a.common.intrinsic.IntrinsicObject;
import org.o42a.core.*;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.StaticTypeRef;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.def.Definitions;
import org.o42a.core.member.AdapterId;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.DefinitionTarget;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.util.log.LoggableData;


public abstract class UnaryOpObj<T, O> extends IntrinsicObject {

	private static FieldDeclaration declaration(
			Container enclosingContainer,
			UnaryOperator operator,
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

	private final ValueType<O> operandType;
	private final UnaryOperator operator;

	public UnaryOpObj(
			Container enclosingContainer,
			UnaryOperator operator,
			StaticTypeRef declaredIn,
			ValueType<T> resultType,
			ValueType<O> operandType) {
		super(declaration(
				enclosingContainer,
				operator,
				declaredIn));
		setValueType(resultType);
		this.operandType = operandType;
		this.operator = operator;
	}

	@SuppressWarnings("unchecked")
	public final ValueType<T> getResultType() {
		return (ValueType<T>) getValueType();
	}

	public final ValueType<O> getOperandType() {
		return this.operandType;
	}

	public final UnaryOperator getOperator() {
		return this.operator;
	}

	@Override
	public String toString() {
		return ("Unary " + this.operator.getSign()
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

		self.setConditions(emptyConditions(this));

		return self.define(new DefinitionTarget(getScope()));
	}

	@Override
	protected Value<?> calculateValue(Scope scope) {

		final Artifact<?> operand =
			getScope().getEnclosingScopePath().resolveArtifact(this, scope);
		final O operandValue =
			getOperandType().definiteValue(operand.materialize().getValue());

		if (operandValue == null) {
			return getResultType().runtimeValue();
		}

		final T result = calculate(operandValue);

		if (result == null) {
			return getResultType().falseValue();
		}

		return getResultType().definiteValue(result);
	}

	protected abstract T calculate(O operand);

	public static class Plus<O extends Number> extends UnaryOpObj<O, O> {

		public Plus(
				Container enclosingContainer,
				StaticTypeRef declaredIn,
				ValueType<O> operandType) {
			super(
					enclosingContainer,
					UnaryOperator.PLUS,
					declaredIn,
					operandType,
					operandType);
		}

		@Override
		protected O calculate(O operand) {
			return operand;
		}

	}

	public static abstract class Minus<O extends Number>
			extends UnaryOpObj<O, O> {

		public Minus(
				Container enclosingContainer,
				StaticTypeRef declaredIn,
				ValueType<O> operandType) {
			super(
					enclosingContainer,
					UnaryOperator.MINUS,
					declaredIn,
					operandType,
					operandType);
		}

	}

}
