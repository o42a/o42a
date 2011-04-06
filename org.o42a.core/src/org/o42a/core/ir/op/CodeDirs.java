/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.core.ir.op;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.op.BoolOp;


public class CodeDirs {

	public static CodeDirs ignoreCondition(Code code) {
		return new CodeDirs(code, null, null, null);
	}

	public static CodeDirs continueWhenUnknown(
			Code code,
			CodePos truePos,
			CodePos falsePos) {
		return new CodeDirs(code, truePos, falsePos, null);
	}

	public static CodeDirs exitWhenUnknown(
			Code code,
			CodePos exit) {
		return new CodeDirs(code, null, exit, exit);
	}

	public static CodeDirs splitWhenUnknown(
			Code code,
			CodePos falsePos,
			CodePos unknownPos) {
		return new CodeDirs(code, null, falsePos, unknownPos);
	}

	private final Code code;
	private final CodePos truePos;
	private final CodePos falsePos;
	private final CodePos unknownPos;

	CodeDirs(
			Code code,
			CodePos truePos,
			CodePos falsePos,
			CodePos unknownPos) {
		assert code != null :
			"Code not specified";
		this.code = code;
		this.truePos = truePos;
		this.falsePos = falsePos;
		this.unknownPos = unknownPos;
	}

	public final boolean isDebug() {
		return this.code.isDebug();
	}

	public final Code code() {
		return this.code;
	}

	public CodeDirs begin(String id, String message) {
		if (!isDebug()) {
			return this;
		}

		this.code.begin(message);

		return new Nested(
				this,
				end(id + "_true", this.truePos),
				end(id + "_false", this.falsePos),
				end(id + "_unknown", this.unknownPos));
	}

	public CodeDirs end() {
		if (!isDebug()) {
			return this;
		}
		throw new IllegalStateException("Not a nested code dirs: " + this);
	}

	public final void goWhenTrue(Code code) {
		go(code, this.truePos);
	}

	public final void goWhenFalse(Code code) {
		go(code, this.falsePos);
	}

	public final void goWhenUnknown(Code code) {
		go(code, this.unknownPos);
	}

	public final void go(Code code, CondOp cond) {

		final BoolOp condition = cond.loadCondition(code);

		if (this.unknownPos == this.falsePos) {
			if (this.falsePos == this.truePos) {
				goWhenTrue(code);
				return;
			}

			final CodePos trueDir = dir(code, this.truePos);

			condition.go(code, trueDir, dir(code, this.falsePos));

			return;
		}

		final CondBlk condTrue = condition.branch(code, "true", "false");
		final CodeBlk condFalse = condTrue.otherwise();

		goWhenTrue(condTrue);

		cond.loadUnknown(condFalse).go(
				condFalse,
				dir(condFalse, this.unknownPos),
				dir(condFalse, this.falsePos));
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();
		boolean semicolon = false;

		out.append("CodeDirs[").append(this.code).append(": ");
		if (this.truePos != null) {
			out.append("true->").append(this.truePos);
			semicolon = true;
		}
		if (this.falsePos != null) {
			if (semicolon) {
				out.append("; ");
			} else {
				semicolon = true;
			}
			if (this.falsePos == this.unknownPos) {
				out.append("false,unknown->").append(this.falsePos);
			} else {
				out.append("false->").append(this.falsePos);
			}
		}
		if (this.unknownPos != null && this.unknownPos != this.falsePos) {
			if (semicolon) {
				out.append("; ");
			}
			out.append("unknown->").append(this.unknownPos);
		}
		out.append(']');

		return out.toString();
	}

	private CodePos end(String id, CodePos dir) {
		if (dir == null) {
			return null;
		}

		final CodeBlk block = this.code.addBlock(id);

		block.end();
		block.go(dir);

		return block.head();
	}

	private final void go(Code code, CodePos codePos) {

		final CodePos dir = dir(code, codePos);

		if (dir != null) {
			code.go(dir);
		}
	}

	private final CodePos dir(Code code, CodePos codePos) {
		if (codePos != null) {
			return codePos;
		}
		if (code != this.code) {
			return this.code.tail();
		}
		return null;
	}

	private static final class Nested extends CodeDirs {

		private final CodeDirs enclosing;
		private boolean ended;

		Nested(
				CodeDirs enclosing,
				CodePos truePos,
				CodePos falsePos,
				CodePos unknownPos) {
			super(enclosing.code, truePos, falsePos, unknownPos);
			this.enclosing = enclosing;
		}

		@Override
		public CodeDirs end() {
			assert !this.ended :
				"Already ended: " + this;
			code().end();
			this.ended = true;
			return this.enclosing;
		}

	}

}
