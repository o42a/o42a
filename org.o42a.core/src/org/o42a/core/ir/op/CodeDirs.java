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
import org.o42a.codegen.code.CodePos;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.value.ValueType;


public class CodeDirs {

	public static CodeDirs falseWhenUnknown(
			CodeBuilder builder,
			Code code,
			CodePos falseDir) {
		return new CodeDirs(builder, code, falseDir, falseDir);
	}

	public static CodeDirs splitWhenUnknown(
			CodeBuilder builder,
			Code code,
			CodePos falseDir,
			CodePos unknownDir) {
		return new CodeDirs(builder, code, falseDir, unknownDir);
	}

	private final Code code;
	private final CodePos falseDir;
	private final CodePos unknownDir;
	private final CodeBuilder builder;

	CodeDirs(
			CodeBuilder builder,
			Code code,
			CodePos falseDir,
			CodePos unknownDir) {
		assert builder != null :
			"Code builder not specified";
		assert code != null :
			"Code not specified";
		assert falseDir != null :
			"False direction not specified";
		assert unknownDir != null :
			"Unknown direction not specified";
		this.builder = builder;
		this.code = code;
		this.falseDir = falseDir;
		this.unknownDir = unknownDir;
	}

	public final Generator getGenerator() {
		return this.code.getGenerator();
	}

	public final CodeBuilder getBuilder() {
		return this.builder;
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
		return new CodeDirs(getBuilder(), code, this.falseDir, this.unknownDir);
	}

	public CodeDirs begin(String id, String message) {
		if (!isDebug()) {
			return this;
		}

		this.code.begin(message);

		final Code falseCode = this.code.addBlock(id + "_false");
		final Code unknownCode;

		if (isFalseWhenUnknown()) {
			unknownCode = falseCode;
		} else {
			unknownCode = this.code.addBlock(id + "_unknown");
		}

		return new Nested(this, falseCode, unknownCode);
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

	public final ValDirs value(ValueType<?> valueType) {
		return new ValDirs.TopLevelValDirs(this, id("value"), valueType);
	}

	public final ValDirs value(ValueType<?> valueType, String name) {
		return new ValDirs.TopLevelValDirs(this, id(name), valueType);
	}

	public final ValDirs value(ValueType<?> valueType, CodeId name) {
		return new ValDirs.TopLevelValDirs(this, name, valueType);
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
		return new CodeDirs(getBuilder(), code(), falseDir(), falseDir());
	}

	public final CodeDirs unknownWhenFalse() {
		if (isFalseWhenUnknown()) {
			return this;
		}
		return new CodeDirs(getBuilder(), code(), unknownDir(), unknownDir());
	}

	public final CodeDirs splitWhenUnknown(
			CodePos falseDir,
			CodePos unknownDir) {
		return new CodeDirs(getBuilder(), code(), falseDir, unknownDir);
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

	public final void go(Code code, CondOp cond) {

		final BoolOp condition = cond.loadCondition(null, code);

		if (isFalseWhenUnknown()) {
			condition.goUnless(code, falseDir());
			return;
		}

		final Code condFalse = code.addBlock("false");

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

	private static final class Nested extends CodeDirs {

		private final CodeDirs enclosing;
		private final Code falseCode;
		private final Code unknownCode;
		private boolean ended;

		Nested(
				CodeDirs enclosing,
				Code falseCode,
				Code unknownCode) {
			super(
					enclosing.getBuilder(),
					enclosing.code(),
					falseCode.head(),
					unknownCode.head());
			this.enclosing = enclosing;
			this.falseCode = falseCode;
			this.unknownCode = unknownCode;
		}

		@Override
		public CodeDirs end() {
			assert !this.ended :
				"Already ended: " + this;
			code().end();
			if (this.falseCode.exists()) {
				this.falseCode.end();
				this.falseCode.go(this.enclosing.falseDir());
			}
			if (!isFalseWhenUnknown() && this.unknownCode.exists()) {
				this.unknownCode.end();
				this.unknownCode.go(this.enclosing.unknownDir());
			}
			this.ended = true;
			return this.enclosing;
		}

	}

}
