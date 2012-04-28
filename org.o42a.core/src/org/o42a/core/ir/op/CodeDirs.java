/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.value.ValueStruct;


public class CodeDirs {

	public static CodeDirs codeDirs(
			CodeBuilder builder,
			Block code,
			CodePos falseDir) {
		return new CodeDirs(builder, code, falseDir);
	}

	private final Block code;
	private final CodePos falseDir;
	private final CodeBuilder builder;

	CodeDirs(CodeBuilder builder, Block code, CodePos falseDir) {
		assert builder != null :
			"Code builder not specified";
		assert code != null :
			"Code not specified";
		assert falseDir != null :
			"False direction not specified";
		this.builder = builder;
		this.code = code;
		this.falseDir = falseDir;
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

	public final Block code() {
		return this.code;
	}

	public final CodeId id() {
		return this.code.id();
	}

	public final CodeId id(String string) {
		return this.code.id(string);
	}

	public final Block addBlock(String name) {
		return this.code.addBlock(name);
	}

	public final Block addBlock(CodeId name) {
		return this.code.addBlock(name);
	}

	public final CodeDirs sub(Block code) {
		return new CodeDirs(getBuilder(), code, this.falseDir);
	}

	public CodeDirs begin(String id, String message) {
		return begin(id(id), message);
	}

	public CodeDirs begin(CodeId id, String message) {
		if (!isDebug()) {
			return this;
		}

		this.code.begin(message);

		final Block falseCode = this.code.addBlock(id.detail("false"));

		return new Nested(this, falseCode);
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

	public final ValDirs value(ValueStruct<?, ?> valueStruct) {
		return new ValDirs.TopLevelValDirs(this, id("value"), valueStruct);
	}

	public final ValDirs value(ValueStruct<?, ?> valueStruct, String name) {
		return new ValDirs.TopLevelValDirs(this, id(name), valueStruct);
	}

	public final ValDirs value(ValueStruct<?, ?> valueStruct, CodeId name) {
		return new ValDirs.TopLevelValDirs(this, name, valueStruct);
	}

	public final ValDirs value(ValOp value) {
		return new ValDirs.TopLevelValDirs(this, value);
	}

	public final ValDirs value(ValDirs storage) {
		return new ValDirs.NestedValDirs(this, storage);
	}

	public final CodeDirs setFalseDir(CodePos falseDir) {
		return new CodeDirs(getBuilder(), code(), falseDir);
	}

	public final CodePos falseDir() {
		return this.falseDir;
	}

	@Override
	public String toString() {
		return toString("CodeDirs", this.code);
	}

	public String toString(String title, Code code) {

		final StringBuilder out = new StringBuilder();
		boolean semicolon = false;

		out.append(title).append('[').append(code).append(": ");
		if (this.falseDir != null) {
			if (semicolon) {
				out.append("; ");
			} else {
				semicolon = true;
			}
			out.append("false->").append(this.falseDir);
		}
		out.append(']');

		return out.toString();
	}

	private static final class Nested extends CodeDirs {

		private final CodeDirs enclosing;
		private final Block falseCode;
		private boolean ended;

		Nested(CodeDirs enclosing, Block falseCode) {
			super(
					enclosing.getBuilder(),
					enclosing.code(),
					falseCode.head());
			this.enclosing = enclosing;
			this.falseCode = falseCode;
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
			this.ended = true;
			return this.enclosing;
		}

	}

}
