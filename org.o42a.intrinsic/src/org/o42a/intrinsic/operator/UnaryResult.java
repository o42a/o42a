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
import org.o42a.core.artifact.Accessor;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.Path;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;


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
	public ValOp writeBuiltin(ValDirs dirs, HostOp host) {

		final ValDirs operandDirs =
				dirs.dirs().value(getOperandStruct(), "operand");
		final ValOp operandVal = operand().op(host).writeValue(operandDirs);

		final ValDirs resultDirs = operandDirs.dirs().value(dirs);
		final ValOp result = write(resultDirs, operandVal);

		resultDirs.done();
		operandDirs.done();

		return result;
	}

	protected abstract T calculate(O operand);

	protected abstract ValOp write(ValDirs dirs, ValOp operand);

	private final Ref operand() {
		if (this.operand != null) {
			return this.operand;
		}

		final Member member =
				field(this.operandName, Accessor.DECLARATION);
		final Path path = member.getKey().toPath();

		return this.operand = path.bind(this, getScope()).target(distribute());
	}

}
