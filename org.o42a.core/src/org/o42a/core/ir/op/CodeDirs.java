/*
    Compiler Core
    Copyright (C) 2011-2013 Ruslan Lopatin

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

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.*;
import org.o42a.codegen.debug.TaskBlock;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.cmd.LocalsCode;
import org.o42a.core.ir.value.ValHolderFactory;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.value.ValueType;
import org.o42a.util.string.ID;


public class CodeDirs {

	private static final ID VALUE_ID = ID.id("value");

	public static CodeDirs codeDirs(
			CodeBuilder builder,
			Block code,
			CodePos falseDir) {
		return new CodeDirs(builder, code, falseDir);
	}

	private final CodeBuilder builder;
	private final Block code;
	private final CodePos falseDir;

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

	public final LocalsCode locals() {

		final LocalsCode locals = getAllocator().get(LocalsCode.class);

		if (locals != null) {
			return locals;
		}

		return getBuilder().locals();
	}

	public final boolean isDebug() {
		return this.code.isDebug();
	}

	public final Allocator getAllocator() {
		return code().getAllocator();
	}

	public final Block code() {
		return this.code;
	}

	public final Block addBlock(String name) {
		return this.code.addBlock(name);
	}

	public final Block addBlock(ID name) {
		return this.code.addBlock(name);
	}

	public final CodeDirs sub(Block code) {
		return new CodeDirs(getBuilder(), code, falseDir());
	}

	public final CodeDirs nested() {
		return new CodeDirs(getBuilder(), code(), falseDir());
	}

	public CodeDirs begin(String id, String message) {
		return begin(id != null ? ID.id(id) : null, message);
	}

	public CodeDirs begin(ID id, String message) {
		return new DebugCodeDirs(this, this.code.begin(id, message));
	}

	public CodeDirs done() {
		return this;
	}

	public final ValDirs value(
			ValueType<?> valueType,
			ValHolderFactory holderFactory) {
		return value(VALUE_ID, valueType, holderFactory);
	}

	public final ValDirs value(
			String name,
			ValueType<?> valueType,
			ValHolderFactory holderFactory) {
		return value(
				name != null ? ID.id(name) : VALUE_ID,
				valueType,
				holderFactory);
	}

	public final ValDirs value(
			ID name,
			ValueType<?> valueType,
			ValHolderFactory holderFactory) {

		final AllocatorCodeDirs dirs =
				new AllocatorCodeDirs(this, code().allocator(name));

		return new ValDirs.TopLevelValDirs(dirs, valueType, holderFactory);
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

	private static final class AllocatorCodeDirs extends CodeDirs {

		private final CodeDirs enclosing;
		private boolean ended;

		AllocatorCodeDirs(CodeDirs enclosing, Allocator allocator) {
			super(enclosing.getBuilder(), allocator, enclosing.falseDir());
			this.enclosing = enclosing;
		}

		@Override
		public CodeDirs done() {
			assert !this.ended :
				"Already ended: " + this;
			this.ended = true;
			if (code().exists()) {
				code().go(this.enclosing.code().tail());
			}
			return this.enclosing;
		}

	}

	private static final class DebugCodeDirs extends CodeDirs {

		private final CodeDirs enclosing;
		private final TaskBlock task;
		private boolean ended;

		DebugCodeDirs(CodeDirs enclosing, TaskBlock task) {
			super(
					enclosing.getBuilder(),
					task.code(),
					enclosing.falseDir());
			this.enclosing = enclosing;
			this.task = task;
		}

		@Override
		public CodeDirs done() {
			assert !this.ended :
				"Already ended: " + this;
			this.ended = true;
			this.task.end();
			return this.enclosing;
		}

	}

}
