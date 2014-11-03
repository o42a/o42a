/*
    Root Object Definition
    Copyright (C) 2010-2014 Ruslan Lopatin

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
import static org.o42a.core.member.MemberIdKind.FIELD_NAME;
import static org.o42a.util.string.Capitalization.CASE_INSENSITIVE;

import org.o42a.common.builtin.AnnotatedBuiltin;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.core.Scope;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ir.op.HostOp;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.Accessor;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberName;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.EscapeMode;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.Path;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.util.fn.Cancelable;
import org.o42a.util.string.ID;


public abstract class UnaryResult<T, O> extends AnnotatedBuiltin {

	public static final ID OPERAND_VALUE_ID = ID.id("operand_value");
	public static final ID OPERAND_PTR_ID = ID.id("operand_ptr");

	private final MemberName operandId;
	private final ValueType<O> operandType;
	private Ref operand;

	public UnaryResult(
			Obj owner,
			AnnotatedSources sources,
			String operandName,
			ValueType<O> operandType) {
		super(owner, sources);
		this.operandId = FIELD_NAME.memberName(
				CASE_INSENSITIVE.canonicalName(operandName));
		this.operandType = operandType;
	}

	public final ValueType<O> getOperandType() {
		return this.operandType;
	}

	@SuppressWarnings("unchecked")
	public final TypeParameters<T> getResultParameters() {
		return (TypeParameters<T>) type().getParameters();
	}

	@Override
	public EscapeMode escapeMode(Scope scope) {
		return operand().escapeMode(scope);
	}

	@Override
	public Value<?> calculateBuiltin(Resolver resolver) {

		final Value<?> operandValue = operand().value(resolver);

		if (operandValue.getKnowledge().isFalse()) {
			return type().getParameters().falseValue();
		}
		if (!operandValue.getKnowledge().isKnown()) {
			return type().getParameters().runtimeValue();
		}

		final O operand =
				getOperandType().cast(operandValue).getCompilerValue();
		final T result = calculate(operand);

		if (result == null) {
			return type().getParameters().falseValue();
		}

		return getResultParameters().compilerValue(result);
	}

	@Override
	public void resolveBuiltin(FullResolver resolver) {
		operand().resolveAll(resolver);
	}

	@Override
	public InlineEval inlineBuiltin(Normalizer normalizer, Scope origin) {

		final InlineValue operandValue = operand().inline(normalizer, origin);

		if (operandValue == null) {
			return null;
		}

		return new InlineUnary(this, operandValue);
	}

	@Override
	public Eval evalBuiltin() {
		return new UnaryEval(this);
	}

	protected abstract T calculate(O operand);

	protected abstract ValOp write(ValDirs dirs, ValOp operand);

	private final Ref operand() {
		if (this.operand != null) {
			return this.operand;
		}

		final Member member =
				member(this.operandId, Accessor.DECLARATION);
		final Path path = member.getMemberKey().toPath().dereference();

		return this.operand = path.bind(this, getScope()).target(distribute());
	}

	private static final class InlineUnary extends InlineEval {

		private final UnaryResult<?, ?> unary;
		private final InlineValue operandValue;

		InlineUnary(UnaryResult<?, ?> unary, InlineValue operandValue) {
			super(null);
			this.unary = unary;
			this.operandValue = operandValue;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {

			final ValDirs operandDirs = dirs.dirs().nested().value(
					"operand",
					this.unary.getOperandType(),
					TEMP_VAL_HOLDER);
			final ValOp operandVal =
					this.operandValue.writeValue(operandDirs, host);

			final ValDirs resultDirs =
					operandDirs.dirs().nested().value(dirs.valDirs());
			final ValOp result = this.unary.write(resultDirs, operandVal);

			dirs.returnValue(resultDirs.code(), result);
			resultDirs.done();
			operandDirs.done();
		}

		@Override
		public String toString() {
			if (this.unary == null) {
				return super.toString();
			}
			return this.unary.toString();
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

	private static final class UnaryEval implements Eval {

		private final UnaryResult<?, ?> unary;

		UnaryEval(UnaryResult<?, ?> unary) {
			this.unary = unary;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {

			final ValDirs operandDirs = dirs.dirs().nested().value(
					"operand",
					this.unary.getOperandType(),
					TEMP_VAL_HOLDER);
			final ValOp operandVal =
					this.unary.operand().op(host).writeValue(operandDirs);

			final ValDirs resultDirs =
					operandDirs.dirs().nested().value(dirs.valDirs());
			final ValOp result = this.unary.write(resultDirs, operandVal);

			dirs.returnValue(resultDirs.code(), result);

			resultDirs.done();
			operandDirs.done();
		}

		@Override
		public String toString() {
			if (this.unary == null) {
				return super.toString();
			}
			return this.unary.toString();
		}

	}

}
