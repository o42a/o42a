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

import org.o42a.common.object.AnnotatedBuiltin;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.core.artifact.Accessor;
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


public abstract class BinaryResult<T, L, R> extends AnnotatedBuiltin {

	private final String leftOperandName;
	private final ValueType<L> leftOperandType;
	private Ref leftOperand;
	private final String rightOperandName;
	private final ValueType<R> rightOperandType;
	private Ref rightOperand;

	public BinaryResult(
			MemberOwner owner,
			AnnotatedSources sources,
			String leftOperandName,
			ValueType<L> leftOperandType,
			String rightOperandName,
			ValueType<R> rightOperandType) {
		super(owner, sources);
		this.leftOperandName = leftOperandName;
		this.leftOperandType = leftOperandType;
		this.rightOperandName = rightOperandName;
		this.rightOperandType = rightOperandType;
	}

	@SuppressWarnings("unchecked")
	public final ValueType<T> getResultType() {
		return (ValueType<T>) value().getValueType();
	}

	public final ValueType<L> getLeftOperandType() {
		return this.leftOperandType;
	}

	public final ValueType<R> getRightOperandType() {
		return this.rightOperandType;
	}

	@Override
	public Value<?> calculateBuiltin(Resolver resolver) {

		final Value<?> leftValue = leftOperand().value(resolver);
		final Value<?> rightValue = rightOperand().value(resolver);

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

		final Resolver resolver = object.value().proposition().resolver();

		leftOperand().resolveValues(resolver);
		rightOperand().resolveValues(resolver);
	}

	@Override
	public ValOp writeBuiltin(ValDirs dirs, HostOp host) {

		final ValDirs leftDirs =
			dirs.dirs().value(getLeftOperandType(), "left");
		final ValOp leftVal = leftOperand().op(host).writeValue(leftDirs);

		final ValDirs rightDirs =
			leftDirs.dirs().value(getRightOperandType(), "right");
		final ValOp rightVal = rightOperand().op(host).writeValue(rightDirs);

		final ValDirs resultDirs = rightDirs.dirs().value(dirs);
		final ValOp result = write(resultDirs, leftVal, rightVal);

		resultDirs.done();
		rightDirs.done();
		leftDirs.done();

		return result;
	}

	protected abstract T calculate(Resolver resolver, L left, R right);

	protected abstract ValOp write(
			ValDirs dirs,
			ValOp leftVal,
			ValOp rightVal);

	private final Ref leftOperand() {
		if (this.leftOperand != null) {
			return this.leftOperand;
		}

		final Member member =
				field(this.leftOperandName, Accessor.DECLARATION);
		final Path path = member.getKey().toPath();

		return this.leftOperand = path.target(this, distribute());
	}

	private final Ref rightOperand() {
		if (this.rightOperand != null) {
			return this.rightOperand;
		}

		final Member member =
				field(this.rightOperandName, Accessor.DECLARATION);
		final Path path = member.getKey().toPath();

		return this.rightOperand = path.target(this, distribute());
	}

}
