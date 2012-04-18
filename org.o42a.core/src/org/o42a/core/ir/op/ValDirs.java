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

import static org.o42a.core.ir.value.ValOp.allocateVal;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValStoreMode;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;


public abstract class ValDirs {

	private final CodeBuilder builder;
	private final Block code;
	private final ValueStruct<?, ?> valueStruct;
	private Block falseCode;
	private Block unknownCode;
	protected CodeDirs dirs;

	ValDirs(CodeBuilder builder, Block code, ValueStruct<?, ?> valueStruct) {
		this.builder = builder;
		this.code = code;
		this.valueStruct = valueStruct;
	}

	public final ValStoreMode getStoreMode() {
		return topLevel().value().getStoreMode();
	}

	public final ValDirs setStoreMode(ValStoreMode storeMode) {
		topLevel().value().setStoreMode(storeMode);
		return this;
	}

	public final Generator getGenerator() {
		return this.code.getGenerator();
	}

	public final CodeBuilder getBuilder() {
		return this.builder;
	}

	public final ValueType<?> getValueType() {
		return getValueStruct().getValueType();
	}

	public final ValueStruct<?, ?> getValueStruct() {
		return this.valueStruct;
	}

	public final boolean isDebug() {
		return this.code.isDebug();
	}

	public final Block code() {
		return this.code;
	}

	public final CodeId id() {
		return code().id();
	}

	public final CodeId id(String name) {
		return code().id(name);
	}

	public final Block addBlock(String name) {
		return this.code.addBlock(name);
	}

	public final Block addBlock(CodeId name) {
		return this.code.addBlock(name);
	}

	public final boolean isFalseWhenUnknown() {
		return dirs().isFalseWhenUnknown();
	}

	public final CodePos falseDir() {
		return dirs().falseDir();
	}

	public final CodePos unknownDir() {
		return dirs().unknownDir();
	}

	public ValOp value() {
		return topLevel().value();
	}

	public abstract void done();

	public final CodeDirs dirs() {
		if (this.dirs != null) {
			return this.dirs;
		}
		return this.dirs = createDirs();
	}

	public final ValDirs sub(String name) {
		return sub(addBlock(name));
	}

	public final ValDirs sub(CodeId name) {
		return sub(addBlock(name));
	}

	public final ValDirs sub(Block code) {
		return dirs().sub(code).value(this);
	}

	public final ValDirs falseWhenUnknown() {

		final CodeDirs dirs = dirs();

		if (dirs.falseDir() == dirs.unknownDir()) {
			return this;
		}

		return dirs.falseWhenUnknown().value(this);
	}

	public final ValDirs falseWhenUnknown(CodePos falseDir) {

		final CodeDirs oldDirs = dirs();
		final CodeDirs newDirs = oldDirs.falseWhenUnknown(falseDir);

		if (oldDirs == newDirs) {
			return this;
		}

		return newDirs.value(this);
	}

	public ValDirs begin(String message) {
		if (!isDebug()) {
			return this;
		}

		final DebugValDirs result = new DebugValDirs(this);

		result.code().begin(message);

		return result;
	}

	@Override
	public String toString() {
		return topLevel().enclosing.toString(
				getClass().getSimpleName(),
				code());
	}

	abstract TopLevelValDirs topLevel();

	abstract CodeDirs createDirs();

	final CodeDirs createDirs(CodeDirs enclosing) {
		this.falseCode = addBlock("false");
		if (enclosing.isFalseWhenUnknown()) {
			this.unknownCode = this.falseCode;
		} else {
			this.unknownCode = addBlock("unknown");
		}
		return new CodeDirs(
				getBuilder(),
				code(),
				this.falseCode.head(),
				this.unknownCode.head());
	}

	void endDirs(CodeDirs enclosing) {
		dirs();
		if (this.falseCode.exists()) {
			endFalse(enclosing, this.falseCode);
		}
		if (this.unknownCode.exists() && this.unknownCode != this.falseCode) {
			endUnknown(enclosing, this.unknownCode);
		}
	}

	void endFalse(CodeDirs enclosing, Block code) {
		code.go(enclosing.falseDir());
	}

	void endUnknown(CodeDirs enclosing, Block code) {
		code.go(enclosing.unknownDir());
	}

	static final class TopLevelValDirs extends ValDirs {

		private final CodeDirs enclosing;
		private final AllocationDirs allocation;
		private final ValOp value;

		TopLevelValDirs(
				CodeDirs enclosing,
				CodeId name,
				ValueStruct<?, ?> valueStruct) {
			super(
					enclosing.getBuilder(),
					enclosing.code(),
					valueStruct);
			this.dirs = enclosing;
			this.enclosing = enclosing;
			this.allocation = enclosing.allocate(name);
			this.value = allocateVal(
					"value",
					this.allocation.code(),
					getBuilder(),
					valueStruct);
		}

		TopLevelValDirs(CodeDirs enclosing, ValOp value) {
			super(
					enclosing.getBuilder(),
					enclosing.code(),
					value.getValueStruct());
			this.dirs = enclosing;
			this.enclosing = enclosing;
			this.value = value;
			this.allocation = null;
		}

		@Override
		public final ValOp value() {
			return this.value;
		}

		@Override
		public void done() {
			if (this.allocation != null) {
				this.allocation.done();
			}
		}

		@Override
		final TopLevelValDirs topLevel() {
			return this;
		}

		@Override
		CodeDirs createDirs() {
			throw new UnsupportedOperationException();
		}

	}

	static final class NestedValDirs extends ValDirs {

		private TopLevelValDirs topLevel;

		NestedValDirs(CodeDirs enclosing, ValDirs storage) {
			super(
					enclosing.getBuilder(),
					enclosing.code(),
					storage.getValueStruct());
			this.topLevel = storage.topLevel();
			this.dirs = enclosing;
		}

		@Override
		public void done() {
		}

		@Override
		TopLevelValDirs topLevel() {
			return this.topLevel;
		}

		@Override
		CodeDirs createDirs() {
			throw new UnsupportedOperationException();
		}

	}

	private static final class DebugValDirs extends ValDirs {

		private final ValDirs enclosing;
		private final TopLevelValDirs topLevel;

		DebugValDirs(ValDirs enclosing) {
			super(
					enclosing.getBuilder(),
					enclosing.code(),
					enclosing.getValueStruct());
			this.enclosing = enclosing;
			this.topLevel = enclosing.topLevel();
		}

		@Override
		public void done() {
			code().end();
			endDirs(this.enclosing.dirs());
		}

		@Override
		final TopLevelValDirs topLevel() {
			return this.topLevel;
		}

		@Override
		CodeDirs createDirs() {
			return createDirs(this.enclosing.dirs());
		}

		@Override
		void endFalse(CodeDirs enclosing, Block code) {
			code.end();
			super.endFalse(enclosing, code);
		}

		@Override
		void endUnknown(CodeDirs enclosing, Block code) {
			code.end();
			super.endUnknown(enclosing, code);
		}

	}

}
