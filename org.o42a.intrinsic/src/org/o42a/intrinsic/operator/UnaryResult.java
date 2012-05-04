/*
    Intrinsics
    Copyright (C) 2010-2012 Ruslan Lopatin

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
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.object.Accessor;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.Path;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;
import org.o42a.util.fn.Cancelable;


public abstract class UnaryResult<T, O> extends AnnotatedBuiltin {

	private final String operandName;
	private final ValueStruct<?, O> operandStruct;
	private Ref operand;

	public UnaryResult(
			MemberOwner owner,
			AnnotatedSources sources,
			String operandName,
			ValueStruct<?, O> operandStruct) {
		super(owner, sources);
		this.operandName = operandName;
		this.operandStruct = operandStruct;
	}

	@SuppressWarnings("unchecked")
	public final ValueStruct<?, T> getResultStruct() {
		return (ValueStruct<?, T>) value().getValueStruct();
	}

	public final ValueStruct<?, O> getOperandStruct() {
		return this.operandStruct;
	}

	@Override
	public Value<?> calculateBuiltin(Resolver resolver) {

		final Value<?> operandValue = operand().value(resolver);

		if (operandValue.getKnowledge().isFalse()) {
			return getResultStruct().falseValue();
		}
		if (!operandValue.getKnowledge().isKnown()) {
			return getResultStruct().runtimeValue();
		}

		final O operand =
				getOperandStruct().cast(operandValue).getCompilerValue();
		final T result = calculate(operand);

		if (result == null) {
			return getResultStruct().falseValue();
		}

		return getResultStruct().compilerValue(result);
	}

	@Override
	public void resolveBuiltin(Resolver resolver) {
		operand().resolve(resolver).resolveValue();
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
				field(this.operandName, Accessor.DECLARATION);
		final Path path = member.getKey().toPath().dereference();

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
					this.unary.getOperandStruct(),
					"operand");
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
					this.unary.getOperandStruct(),
					"operand");
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
