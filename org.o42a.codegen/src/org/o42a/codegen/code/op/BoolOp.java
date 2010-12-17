/*
    Compiler Code Generator
    Copyright (C) 2010 Ruslan Lopatin

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

import static org.o42a.codegen.code.op.OpCodeBase.unwrapPos;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.codegen.code.CondBlk;


public abstract class BoolOp implements Op {

	public final CondBlk branch(Code source) {
		return branch(source, null);
	}

	public final CondBlk branch(Code source, String conditionName) {
		return branch(source, conditionName, null);
	}

	public final CondBlk branch(
			Code source,
			String trueName,
			String falseName) {

		final OpCodeBase src = source;

		return src.choose(
				this,
				trueName != null ? trueName : "true",
				falseName != null ? falseName
						: (trueName != null ? "not_" + trueName : "false"));
	}

	public final void go(Code source, CodePos truePos) {
		go(source, truePos, null);
	}

	public final void goUnless(Code source, CodePos falsePos) {
		go(source, null, falsePos);
	}

	public final void go(Code source, CodePos truePos, CodePos falsePos) {

		final OpCodeBase src = source;

		src.writer().go(this, unwrapPos(truePos), unwrapPos(falsePos));
	}

	public abstract void returnValue(Code code);

}
