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
import static org.o42a.util.use.User.dummyUser;

import org.o42a.common.object.IntrinsicBuiltin;
import org.o42a.core.artifact.Accessor;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.util.use.UserInfo;


public abstract class BinaryResult<T, L, R> extends IntrinsicBuiltin {

	private final String leftOperandName;
	private final ValueType<L> leftOperandType;
	private MemberKey leftOperandKey;
	private final String rightOperandName;
	private final ValueType<R> rightOperandType;
	private MemberKey rightOperandKey;

	public BinaryResult(
			MemberOwner owner,
			String name,
			ValueType<T> resultType,
			String leftOperandName,
			ValueType<L> leftOperandType,
			String rightOperandName,
			ValueType<R> rightOperandType,
			String sourcePath) {
		super(
				owner,
				sourcedDeclaration(owner, name, sourcePath).prototype());
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
	public Value<?> calculateBuiltin(Resolver resolver) {

		final Obj leftObject =
			resolver.getScope().getContainer()
			.member(leftOperandKey())
			.substance(resolver)
			.toArtifact()
			.materialize();
		final Value<?> leftValue = leftObject.value(resolver).getValue();
		final Obj rightObject =
			resolver.getScope().getContainer()
			.member(rightOperandKey())
			.substance(resolver)
			.toArtifact()
			.materialize();
		final Value<?> rightValue = rightObject.value(resolver).getValue();

		if (leftValue.isFalse() || rightValue.isFalse()) {
			return getResultType().falseValue();
		}
		if (!leftValue.isDefinite() || !rightValue.isDefinite()) {
			return getResultType().runtimeValue();
		}

		final L left =
			getLeftOperandType().cast(leftValue).getDefiniteValue();
		final R right =
			getRightOperandType().cast(rightValue).getDefiniteValue();

		final T result = calculate(resolver, left, right);

		if (result == null) {
			return getResultType().falseValue();
		}

		return getResultType().constantValue(result);
	}

	@Override
	public void resolveBuiltin(Obj object) {

		final UserInfo user = object.value(dummyUser());
		final Obj leftObject =
			object.member(leftOperandKey())
			.substance(user)
			.toArtifact()
			.materialize();
		final Obj rightObject =
			object.member(rightOperandKey())
			.substance(user)
			.toArtifact()
			.materialize();

		leftObject.value(user);
		rightObject.value(user);
	}

	@Override
	public ValOp writeBuiltin(ValDirs dirs, HostOp host) {

		final ValDirs leftDirs =
			dirs.dirs().value(getLeftOperandType(), "left");
		final ObjectOp leftObject =
			host.field(leftDirs.dirs(), leftOperandKey())
			.materialize(leftDirs.dirs());
		final ValOp leftVal = leftObject.writeValue(leftDirs);

		final ValDirs rightDirs =
			leftDirs.dirs().value(getRightOperandType(), "right");
		final ObjectOp rightObject =
			host.field(rightDirs.dirs(), rightOperandKey())
			.materialize(rightDirs.dirs());
		final ValOp rightVal = rightObject.writeValue(rightDirs);

		final ValDirs resultDirs = rightDirs.dirs().value(dirs);
		final ValOp result = write(resultDirs, leftVal, rightVal);

		resultDirs.done();
		rightDirs.done();
		leftDirs.done();

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

	protected abstract T calculate(Resolver resolver, L left, R right);

	protected abstract ValOp write(
			ValDirs dirs,
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

}
