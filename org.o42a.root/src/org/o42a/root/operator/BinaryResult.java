/*
    Root Object Definition
    Copyright (C) 2010-2013 Ruslan Lopatin

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
package org.o42a.root.operator;

import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;
import static org.o42a.core.member.MemberName.fieldName;
import static org.o42a.util.string.Capitalization.CASE_INSENSITIVE;

import org.o42a.common.builtin.AnnotatedBuiltin;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.Accessor;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberName;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.Path;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.util.fn.Cancelable;
import org.o42a.util.string.ID;


public abstract class BinaryResult<T, L, R> extends AnnotatedBuiltin {

	public static final ID LEFT_ID = ID.id("left");
	public static final ID LEFT_PTR_ID = ID.id("left_ptr");
	public static final ID RIGHT_ID = ID.id("right");
	public static final ID RIGHT_PTR_ID = ID.id("right_ptr");

	private final MemberName leftOperandId;
	private final ValueType<L> leftOperandType;
	private Ref leftOperand;
	private final MemberName rightOperandId;
	private final ValueType<R> rightOperandType;
	private Ref rightOperand;

	public BinaryResult(
			Obj owner,
			AnnotatedSources sources,
			String leftOperandName,
			ValueType<L> leftOperandType,
			String rightOperandName,
			ValueType<R> rightOperandType) {
		super(owner, sources);
		this.leftOperandId =
				fieldName(CASE_INSENSITIVE.canonicalName(leftOperandName));
		this.leftOperandType = leftOperandType;
		this.rightOperandId =
				fieldName(CASE_INSENSITIVE.canonicalName(rightOperandName));
		this.rightOperandType = rightOperandType;
	}

	@SuppressWarnings("unchecked")
	public final TypeParameters<T> getResultParameters() {
		return (TypeParameters<T>) type().getParameters();
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

		if (leftValue.getKnowledge().isFalse()
				|| rightValue.getKnowledge().isFalse()) {
			return type().getParameters().falseValue();
		}
		if (!leftValue.getKnowledge().isKnown()
				|| !rightValue.getKnowledge().isKnown()) {
			return type().getParameters().runtimeValue();
		}

		final L left =
				getLeftOperandType().cast(leftValue).getCompilerValue();
		final R right =
				getRightOperandType().cast(rightValue).getCompilerValue();

		final T result = calculate(resolver, left, right);

		if (result == null) {
			return type().getParameters().falseValue();
		}

		return getResultParameters().compilerValue(result);
	}

	@Override
	public void resolveBuiltin(FullResolver resolver) {
		leftOperand().resolveAll(resolver);
		rightOperand().resolveAll(resolver);
	}

	@Override
	public InlineEval inlineBuiltin(Normalizer normalizer, Scope origin) {

		final InlineValue leftValue = leftOperand().inline(normalizer, origin);
		final InlineValue rightValue =
				rightOperand().inline(normalizer, origin);

		if (leftValue == null || rightValue == null) {
			return null;
		}

		return new InlineBinary(this, leftValue, rightValue);
	}

	@Override
	public Eval evalBuiltin() {
		return new BinaryEval(this);
	}

	protected abstract T calculate(Resolver resolver, L left, R right);

	protected abstract ValOp write(ValDirs dirs, ValOp leftVal, ValOp rightVal);

	private final Ref leftOperand() {
		if (this.leftOperand != null) {
			return this.leftOperand;
		}

		final Member member =
				member(this.leftOperandId, Accessor.DECLARATION);
		final Path path = member.getMemberKey().toPath().dereference();

		return this.leftOperand =
				path.bind(this, getScope()).target(distribute());
	}

	private final Ref rightOperand() {
		if (this.rightOperand != null) {
			return this.rightOperand;
		}

		final Member member =
				member(this.rightOperandId, Accessor.DECLARATION);
		final Path path = member.getMemberKey().toPath().dereference();

		return this.rightOperand =
				path.bind(this, getScope()).target(distribute());
	}

	private static final class InlineBinary extends InlineEval {

		private final BinaryResult<?, ?, ?> binary;
		private final InlineValue leftValue;
		private final InlineValue rightValue;

		InlineBinary(
				BinaryResult<?, ?, ?> binary,
				InlineValue leftValue,
				InlineValue rightValue) {
			super(null);
			this.binary = binary;
			this.leftValue = leftValue;
			this.rightValue = rightValue;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {

			final ValDirs leftDirs = dirs.dirs().nested().value(
					"left",
					this.binary.getLeftOperandType(),
					TEMP_VAL_HOLDER);
			final ValOp leftVal = this.leftValue.writeValue(leftDirs, host);

			final ValDirs rightDirs = leftDirs.dirs().nested().value(
					"right",
					this.binary.getRightOperandType(),
					TEMP_VAL_HOLDER);
			final ValOp rightVal = this.rightValue.writeValue(rightDirs, host);

			final ValDirs resultDirs =
					rightDirs.dirs().nested().value(dirs.valDirs());
			final ValOp result =
					this.binary.write(resultDirs, leftVal, rightVal);

			dirs.returnValue(resultDirs.code(), result);

			resultDirs.done();
			rightDirs.done();
			leftDirs.done();
		}

		@Override
		public String toString() {
			if (this.binary == null) {
				return super.toString();
			}
			return this.binary.toString();
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

	private static final class BinaryEval implements Eval {

		private final BinaryResult<?, ?, ?> binary;

		BinaryEval(BinaryResult<?, ?, ?> binary) {
			this.binary = binary;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {

			final ValDirs leftDirs = dirs.dirs().nested().value(
					"left",
					this.binary.getLeftOperandType(),
					TEMP_VAL_HOLDER);
			final ValOp leftVal =
					this.binary.leftOperand().op(host).writeValue(leftDirs);

			final ValDirs rightDirs = leftDirs.dirs().nested().value(
					"right",
					this.binary.getRightOperandType(),
					TEMP_VAL_HOLDER);
			final ValOp rightVal =
					this.binary.rightOperand().op(host).writeValue(rightDirs);

			final ValDirs resultDirs =
					rightDirs.dirs().nested().value(dirs.valDirs());
			final ValOp result =
					this.binary.write(resultDirs, leftVal, rightVal);

			dirs.returnValue(resultDirs.code(), result);

			resultDirs.done();
			rightDirs.done();
			leftDirs.done();
		}

		@Override
		public String toString() {
			if (this.binary == null) {
				return super.toString();
			}
			return this.binary.toString();
		}

	}

}
