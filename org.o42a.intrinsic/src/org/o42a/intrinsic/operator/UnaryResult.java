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

import static org.o42a.core.member.MemberId.memberName;

import org.o42a.common.object.IntrinsicBuiltin;
import org.o42a.common.resolution.StatementResolver;
import org.o42a.core.artifact.Accessor;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.Path;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


public abstract class UnaryResult<T, O> extends IntrinsicBuiltin {

	private final ValueType<O> operandType;
	private final String operandName;
	private final StatementResolver resolver = new StatementResolver();
	private Ref operand;

	public UnaryResult(
			MemberOwner owner,
			String name,
			ValueType<T> resultType,
			String operandName,
			ValueType<O> operandType,
			String sourcePath) {
		super(
				owner,
				sourcedDeclaration(owner, name, sourcePath).prototype());
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
	public Value<?> calculateBuiltin(Resolver resolver) {

		final Value<?> operandValue = operand().value(resolver);

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

	@Override
	public void resolveBuiltin(Obj object) {
		this.resolver.resolveBuiltin(object, operand());
	}

	@Override
	public ValOp writeBuiltin(ValDirs dirs, HostOp host) {

		final ValDirs operandDirs =
			dirs.dirs().value(getOperandType(), "operand");
		final ValOp operandVal = operand().op(host).writeValue(operandDirs);

		final ValDirs resultDirs = operandDirs.dirs().value(dirs);
		final ValOp result = write(resultDirs, operandVal);

		resultDirs.done();
		operandDirs.done();

		return result;
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

	protected abstract T calculate(O operand);

	protected abstract ValOp write(ValDirs dirs, ValOp operand);

	private final Ref operand() {
		if (this.operand != null) {
			return this.operand;
		}

		final Member member =
			member(memberName(this.operandName), Accessor.DECLARATION);
		final Path path = member.getKey().toPath();

		return this.operand = path.target(this, distribute());
	}

}
