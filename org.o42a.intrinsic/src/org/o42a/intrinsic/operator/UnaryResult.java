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
import static org.o42a.core.member.MemberId.memberName;
import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;
import static org.o42a.core.st.StatementEnv.defaultEnv;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodeBlk;
import org.o42a.common.intrinsic.IntrinsicObject;
import org.o42a.core.*;
import org.o42a.core.artifact.Accessor;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.ref.Ref;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


public abstract class UnaryResult<T, O> extends IntrinsicObject {

	static FieldDeclaration declaration(
			Container enclosingContainer,
			String name,
			String sourcePath) {

		final CompilerContext context;

		try {
			context = enclosingContainer.getContext().contextFor(sourcePath);
		} catch (Exception e) {
			throw new ExceptionInInitializerError(e);
		}

		final Location location = new Location(context, context.getSource());
		final Distributor distributor =
			declarativeDistributor(enclosingContainer);

		return fieldDeclaration(
				location,
				distributor,
				memberName(name)).prototype();
	}

	private final ValueType<O> operandType;
	private final String operandName;
	private MemberKey operandKey;

	public UnaryResult(
			Container enclosingContainer,
			String name,
			ValueType<T> resultType,
			String operandName,
			ValueType<O> operandType,
			String sourcePath) {
		super(declaration(enclosingContainer, name, sourcePath));
		this.operandName = operandName;
		this.operandType = operandType;
		setValueType(resultType);
	}

	@SuppressWarnings("unchecked")
	public final ValueType<T> getResultType() {
		return (ValueType<T>) getValueType();
	}

	public final ValueType<O> getOperandType() {
		return this.operandType;
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

		final Obj operandObject =
			scope.getContainer()
			.member(operandKey())
			.substance(scope)
			.toArtifact()
			.materialize();
		final Value<?> operandValue =
			operandObject.value().useBy(scope).getValue();

		if (operandValue.isFalse()) {
			return getResultType().falseValue();
		}
		if (!operandValue.isDefinite()) {
			return getResultType().runtimeValue();
		}

		final O operand =
			getOperandType().cast(operandValue).getDefiniteValue();
		final T result = calculate(operand);

		if (result == null) {
			return getResultType().falseValue();
		}

		return getResultType().constantValue(result);
	}

	protected abstract T calculate(O operand);

	@Override
	protected ObjectValueIR createValueIR(ObjectIR objectIR) {
		return new ValueIR(objectIR);
	}

	protected abstract void write(CodeDirs dirs, ValOp result, ValOp operand);

	private final MemberKey operandKey() {
		if (this.operandKey != null) {
			return this.operandKey;
		}

		final Member operandMember =
			member(memberName(this.operandName), Accessor.DECLARATION);

		return this.operandKey = operandMember.getKey();
	}

	private static final class ValueIR extends ProposedValueIR {

		ValueIR(ObjectIR objectIR) {
			super(objectIR);
		}

		@Override
		protected void proposition(Code code, ValOp result, ObjectOp host) {

			final UnaryResult<?, ?> object =
				(UnaryResult<?, ?>) getObjectIR().getObject();
			final CodeBlk failure = code.addBlock("unary_failure");
			final CodeDirs dirs = falseWhenUnknown(code, failure.head());
			final ObjectOp operand =
				host.field(dirs, object.operandKey()).materialize(dirs);
			final ValOp operandVal = operand.writeValue(dirs);

			object.write(dirs, result, operandVal);

			if (failure.exists()) {
				result.storeFalse(failure);
				failure.go(code.tail());
			}
		}

	}

}
