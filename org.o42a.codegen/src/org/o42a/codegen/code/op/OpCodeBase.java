/*
    Compiler Code Generator
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
package org.o42a.codegen.code.op;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.codegen.code.CondBlk;
import org.o42a.codegen.data.CodeBase;


public abstract class OpCodeBase extends CodeBase {

	protected static CodePos unwrapPos(CodePos codePos) {
		if (codePos == null || codePos.getClass() != Head.class) {
			return codePos;
		}
		return ((Head) codePos).unwrap();
	}

	@Override
	protected void assertIncomplete() {
		super.assertIncomplete();
	}

	protected abstract CondBlk choose(
			BoolOp condition,
			CodeId trueName,
			CodeId falseName);

	protected static final class Head implements CodePos {

		private final Code code;

		public Head(Code code) {
			this.code = code;
		}

		CodePos unwrap() {
			return this.code.writer().head();
		}

	}

}
