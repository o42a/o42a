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

import static org.o42a.core.ir.op.CodeDirs.falseWhenUnknown;
import static org.o42a.core.member.MemberId.memberName;
import static org.o42a.core.st.StatementEnv.defaultEnv;
import static org.o42a.intrinsic.operator.UnaryResult.declaration;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodeBlk;
import org.o42a.common.intrinsic.IntrinsicObject;
import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.artifact.Accessor;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.ref.Ref;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


public abstract class BinaryResult<T, L, R> extends IntrinsicObject {

	private final String leftOperandName;
	private final ValueType<L> leftOperandType;
	private MemberKey leftOperandKey;
	private final String rightOperandName;
	private final ValueType<R> rightOperandType;
	private MemberKey rightOperandKey;

	public BinaryResult(
			Container enclosingContainer,
			String name,
			ValueType<T> resultType,
			String leftOperandName,
			ValueType<L> leftOperandType,
			String rightOperandName,
			ValueType<R> rightOperandType,
			String sourcePath) {
		super(declaration(enclosingContainer, name, sourcePath));
		this.rightOperandName = rightOperandName;
		this.rightOperandType = rightOperandType;
		setValueType(resultType);
		this.leftOperandName = leftOperandName;
		this.leftOperandType = leftOperandType;
	}

	@SuppressWarnings("unchecked")
	public final ValueType<T> getResultType() {
		return (ValueType<T>) getValueType();
	}

	public final ValueType<L> getLeftOperandType() {
		return this.leftOperandType;
	}

	public final ValueType<R> getRightOperandType() {
		return this.rightOperandType;
	}

	@Override
	protected Ascendants createAscendants() {
		return new Ascendants(this).setAncestor(
				getValueType().typeRef(this, getScope().getEnclosingScope()));
	}

	@Override
	protected void postResolve() {
		super.postResolve();
		includeSource();
	}

	@Override
	protected Definitions explicitDefinitions() {

		final Ref self = selfRef();

		self.setEnv(defaultEnv(this));

		return self.define(getScope());
	}

	@Override
	protected Value<?> calculateValue(Scope scope) {

		final Obj leftObject =
			scope.getContainer()
			.member(leftOperandKey())
			.substance(scope)
			.toArtifact()
			.materialize();
		final Value<?> leftValue = leftObject.value().useBy(scope).getValue();

		if (leftValue.isFalse()) {
			return getResultType().falseValue();
		}

		final Obj rightObject =
			scope.getContainer()
			.member(rightOperandKey())
			.substance(scope)
			.toArtifact()
			.materialize();
		final Value<?> rightValue = rightObject.value().useBy(scope).getValue();

		if (rightValue.isFalse()) {
			return getResultType().falseValue();
		}

		if (!leftValue.isDefinite() || !rightValue.isDefinite()) {
			return getResultType().runtimeValue();
		}

		final L left =
			getLeftOperandType().cast(leftValue).getDefiniteValue();
		final R right =
			getRightOperandType().cast(rightValue).getDefiniteValue();

		final T result = calculate(scope, left, right);

		if (result == null) {
			return getResultType().falseValue();
		}

		return getResultType().definiteValue(result);
	}

	protected abstract T calculate(Scope scope, L left, R right);

	@Override
	protected ObjectValueIR createValueIR(ObjectIR objectIR) {
		return new ValueIR(objectIR);
	}

	protected abstract void write(
			CodeDirs dirs,
			ValOp result,
			ValOp leftVal,
			ValOp rightVal);

	private final MemberKey leftOperandKey() {
		if (this.leftOperandKey != null) {
			return this.leftOperandKey;
		}

		final Member operandMember =
			member(memberName(this.leftOperandName), Accessor.DECLARATION);

		return this.leftOperandKey = operandMember.getKey();
	}

	private final MemberKey rightOperandKey() {
		if (this.rightOperandKey != null) {
			return this.rightOperandKey;
		}

		final Member operandMember =
			member(memberName(this.rightOperandName), Accessor.DECLARATION);

		return this.rightOperandKey = operandMember.getKey();
	}

	private static final class ValueIR extends ProposedValueIR {

		ValueIR(ObjectIR objectIR) {
			super(objectIR);
		}

		@Override
		protected void proposition(Code code, ValOp result, ObjectOp host) {

			final BinaryResult<?, ?, ?> object =
				(BinaryResult<?, ?, ?>) getObjectIR().getObject();
			final CodeBlk failure = code.addBlock("binary_failure");
			final CodeDirs dirs = falseWhenUnknown(code, failure.head());
			final ObjectOp leftObject =
				host.field(dirs, object.leftOperandKey()).materialize(dirs);
			final ValOp leftVal = leftObject.writeValue(dirs);
			final ObjectOp rightObject =
				host.field(dirs, object.rightOperandKey()).materialize(dirs);
			final ValOp rightVal = rightObject.writeValue(dirs);

			object.write(dirs, result, leftVal, rightVal);

			if (failure.exists()) {
				result.storeFalse(failure);
				failure.go(code.tail());
			}
		}

	}

}
