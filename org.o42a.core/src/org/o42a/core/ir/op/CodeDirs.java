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

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodeBlk;
import org.o42a.codegen.code.CodePos;
import org.o42a.codegen.code.op.BoolOp;


public class CodeDirs {

	public static CodeDirs falseWhenUnknown(Code code, CodePos falseDir) {
		return new CodeDirs(code, falseDir, falseDir);
	}

	public static CodeDirs splitWhenUnknown(
			Code code,
			CodePos falseDir,
			CodePos unknownDir) {
		return new CodeDirs(code, falseDir, unknownDir);
	}

	private final Code code;
	private final CodePos falseDir;
	private final CodePos unknownDir;

	CodeDirs(Code code, CodePos falseDir, CodePos unknownDir) {
		assert code != null :
			"Code not specified";
		assert falseDir != null :
			"False direction not specified";
		assert unknownDir != null :
			"Unknown direction not specified";
		this.code = code;
		this.falseDir = falseDir;
		this.unknownDir = unknownDir;
	}

	public final Generator getGenerator() {
		return this.code.getGenerator();
	}

	public final boolean isDebug() {
		return this.code.isDebug();
	}

	public final Code code() {
		return this.code;
	}

	public final CodeId id() {
		return this.code.id();
	}

	public final CodeId id(String string) {
		return this.code.id(string);
	}

	public final Code addBlock(String name) {
		return this.code.addBlock(name);
	}

	public final Code addBlock(CodeId name) {
		return this.code.addBlock(name);
	}

	public final CodeDirs sub(Code code) {
		return new CodeDirs(code, this.falseDir, this.unknownDir);
	}

	public CodeDirs begin(String id, String message) {
		if (!isDebug()) {
			return this;
		}

		this.code.begin(message);

		final CodePos falsePos = end(id + "_false", this.falseDir);
		final CodePos unknownPos;

		if (this.falseDir == this.unknownDir) {
			unknownPos = falsePos;
		} else {
			unknownPos = end(id + "_unknown", this.unknownDir);
		}

		return new Nested(this, falsePos, unknownPos);
	}

	public CodeDirs end() {
		if (!isDebug()) {
			return this;
		}
		throw new IllegalStateException("Not a nested code dirs: " + this);
	}

	public final AllocationDirs allocate() {
		return new AllocationDirs(this, code().allocate());
	}

	public final AllocationDirs allocate(String name) {
		return new AllocationDirs(this, code().allocate(name));
	}

	public final AllocationDirs allocate(CodeId name) {
		return new AllocationDirs(this, code().allocate(name));
	}

	public final ValDirs value() {
		return new ValDirs.TopLevelValDirs(this, id("value"));
	}

	public final ValDirs value(String name) {
		return new ValDirs.TopLevelValDirs(this, id(name));
	}

	public final ValDirs value(CodeId name) {
		return new ValDirs.TopLevelValDirs(this, name);
	}

	public final ValDirs value(CodeId name, ValOp value) {
		return new ValDirs.TopLevelValDirs(this, name, value);
	}

	public final ValDirs value(ValDirs storage) {
		return new ValDirs.NestedValDirs(this, storage);
	}

	public final CodeDirs falseWhenUnknown() {
		if (isFalseWhenUnknown()) {
			return this;
		}
		return new CodeDirs(this.code, this.falseDir, this.falseDir);
	}

	public final CodeDirs unknownWhenFalse() {
		if (isFalseWhenUnknown()) {
			return this;
		}
		return new CodeDirs(this.code, this.unknownDir, this.unknownDir);
	}

	public final CodeDirs splitWhenUnknown(
			CodePos falseDir,
			CodePos unknownDir) {
		return new CodeDirs(this.code, falseDir, unknownDir);
	}

	public final void goWhenFalse(Code code) {
		code.go(falseDir());
	}

	public final void goWhenUnknown(Code code) {
		code.go(unknownDir());
	}

	public final boolean isFalseWhenUnknown() {
		return this.falseDir == this.unknownDir;
	}

	public final CodePos falseDir() {
		return this.falseDir;
	}

	public final CodePos unknownDir() {
		return this.unknownDir;
	}

	public final void go(Code code, BoolOp bool) {
		bool.goUnless(code, falseDir());
	}

	public final void go(Code code, CondOp cond) {

		final BoolOp condition = cond.loadCondition(null, code);

		if (isFalseWhenUnknown()) {
			condition.goUnless(code, falseDir());
			return;
		}

		final CodeBlk condFalse = code.addBlock("false");

		condition.goUnless(code, condFalse.head());
		if (condFalse.exists()) {
			cond.loadUnknown(null, condFalse).go(
					condFalse,
					unknownDir(),
					falseDir());
		}
	}

	@Override
	public String toString() {
		return toString("CodeDirs", this.code);
	}

	String toString(String title, Code code) {

		final StringBuilder out = new StringBuilder();
		boolean semicolon = false;

		out.append(title).append('[').append(code).append(": ");
		if (this.falseDir != null) {
			if (semicolon) {
				out.append("; ");
			} else {
				semicolon = true;
			}
			if (this.falseDir == this.unknownDir) {
				out.append("false,unknown->").append(this.falseDir);
			} else {
				out.append("false->").append(this.falseDir);
			}
		}
		if (this.unknownDir != null && this.unknownDir != this.falseDir) {
			if (semicolon) {
				out.append("; ");
			}
			out.append("unknown->").append(this.unknownDir);
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

	private static final class Nested extends CodeDirs {

		private final CodeDirs enclosing;
		private boolean ended;

		Nested(
				CodeDirs enclosing,
				CodePos falsePos,
				CodePos unknownPos) {
			super(enclosing.code, falsePos, unknownPos);
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
