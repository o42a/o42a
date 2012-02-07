/*
    Compiler Code Generator
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
package org.o42a.codegen.code.op;

import static org.o42a.codegen.code.op.OpBlockBase.unwrapPos;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.*;


public abstract class BoolOp implements Op {

	@Override
	public void allocated(AllocationCode code, StructOp<?> enclosing) {
	}

	public abstract <O extends Op> O select(
			CodeId id,
			Code code,
			O trueValue,
			O falseValue);

	public final CondBlock branch(Block source) {
		return branch(source, null);
	}

	public final CondBlock branch(Block source, String conditionName) {
		return branch(source, conditionName, null);
	}

	public final CondBlock branch(
			Block source,
			String trueName,
			String falseName) {
		return branch(
				source,
				trueName != null ? source.getGenerator().id(trueName) : null,
				falseName != null ? source.getGenerator().id(falseName) : null);
	}

	public final CondBlock branch(
			Block source,
			CodeId trueName,
			CodeId falseName) {

		final OpBlockBase src = source;

		return src.choose(
				this,
				trueName != null ? trueName : source.getGenerator().id("true"),
				falseName != null
				? falseName
				: (trueName != null
						? source.getGenerator().id("not_" + trueName)
						: source.getGenerator().id("false")));
	}

	public final void go(Block source, CodePos truePos) {
		go(source, truePos, null);
	}

	public final void goUnless(Block source, CodePos falsePos) {
		go(source, null, falsePos);
	}

	public final void go(Block source, CodePos truePos, CodePos falsePos) {
		source.writer().go(this, unwrapPos(truePos), unwrapPos(falsePos));
	}

	public abstract void returnValue(Block code);

}
