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


public abstract class ValDirs {

	private final Code code;
	private Code falseCode;
	private Code unknownCode;
	protected ValOp value;
	protected CodeDirs dirs;

	ValDirs(Code code) {
		this.code = code;
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
		return code().id();
	}

	public final CodeId id(String name) {
		return code().id(name);
	}

	public final Code addBlock(String name) {
		return this.code.addBlock(name);
	}

	public final Code addBlock(CodeId name) {
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
		if (this.value != null) {
			return this.value;
		}

		final TopLevelValDirs topLevel = topLevel();

		if (code() == topLevel.code()) {
			return this.value = topLevel.value();
		}

		return this.value = topLevel.value();
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

	public final ValDirs sub(Code code) {
		return new SubValDirs(this, code);
	}

	public final ValDirs falseWhenUnknown() {

		final CodeDirs dirs = dirs();

		if (dirs.falseDir() == dirs.unknownDir()) {
			return this;
		}

		return new FalseWhenUnknownValDirs(this, code());
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

	CodeDirs createDirs(CodeDirs enclosing) {
		this.falseCode = createDir("false");
		if (enclosing.isFalseWhenUnknown()) {
			this.unknownCode = this.falseCode;
		} else {
			this.unknownCode = createDir("unknown");
		}
		return new CodeDirs(
				this.code,
				this.falseCode.head(),
				this.unknownCode.head());
	}

	abstract Code createDir(String name);

	void handleDirs(CodeDirs enclosing) {
		dirs();
		if (this.falseCode.exists()) {
			enclosing.goWhenFalse(this.falseCode);
		}
		if (this.unknownCode.exists() && this.unknownCode != this.falseCode) {
			enclosing.goWhenUnknown(this.unknownCode);
		}
	}

	static final class TopLevelValDirs extends ValDirs {

		private final CodeDirs enclosing;
		private final boolean allocatable;
		private AllocationDirs allocation;
		private ValOp value;

		TopLevelValDirs(CodeDirs enclosing, CodeId name) {
			super(enclosing.addBlock(name));
			this.allocatable = true;
			this.enclosing = enclosing;
		}

		public TopLevelValDirs(CodeDirs enclosing, CodeId name, ValOp value) {
			super(enclosing.code());
			this.dirs = enclosing;
			this.allocatable = false;
			this.enclosing = enclosing;
			this.value = value;
		}

		@Override
		public ValOp value() {
			if (this.value != null) {
				return this.value;
			}

			assert this.allocatable :
				"Can not allocate value";

			this.allocation = this.enclosing.allocate("value");

			return this.value =
				this.allocation.allocate(id("value"), ValOp.VAL_TYPE)
				.storeIndefinite(this.allocation.code());
		}

		@Override
		public void done() {
			if (!this.allocatable) {
				return;
			}
			if (this.allocation == null) {
				this.enclosing.code().go(code().head());
				code().go(this.enclosing.code().tail());
				handleDirs(this.enclosing);
			} else {
				this.allocation.code().go(code().head());
				code().go(this.allocation.code().tail());
				handleDirs(this.allocation.dirs());
				this.allocation.done();
			}
		}

		@Override
		final TopLevelValDirs topLevel() {
			return this;
		}

		@Override
		CodeDirs createDirs() {
			return createDirs(this.enclosing);
		}

		@Override
		Code createDir(String name) {
			return addBlock(name);
		}

	}

	static final class NestedValDirs extends ValDirs {

		private TopLevelValDirs topLevel;

		NestedValDirs(CodeDirs enclosing, ValDirs storage) {
			super(enclosing.code());
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

		@Override
		Code createDir(String name) {
			throw new UnsupportedOperationException();
		}

	}

	private static class SubValDirs extends ValDirs {

		final ValDirs enclosing;
		private final TopLevelValDirs topLevel;

		SubValDirs(ValDirs enclosing, Code code) {
			super(code);
			this.enclosing = enclosing;
			this.topLevel = enclosing.topLevel();
		}

		@Override
		public void done() {
		}

		@Override
		final TopLevelValDirs topLevel() {
			return this.topLevel;
		}

		@Override
		CodeDirs createDirs() {
			return this.enclosing.dirs().sub(code());
		}

		@Override
		Code createDir(String name) {
			throw new UnsupportedOperationException();
		}

	}

	private static final class FalseWhenUnknownValDirs extends SubValDirs {

		FalseWhenUnknownValDirs(ValDirs enclosing, Code code) {
			super(enclosing, code);
		}

		@Override
		CodeDirs createDirs() {
			return this.enclosing.dirs().falseWhenUnknown();
		}

	}

	private static final class DebugValDirs extends ValDirs {

		private final ValDirs enclosing;
		private final TopLevelValDirs topLevel;

		DebugValDirs(ValDirs enclosing) {
			super(enclosing.code());
			this.enclosing = enclosing;
			this.topLevel = enclosing.topLevel();
		}

		@Override
		public void done() {
			code().end();
			handleDirs(this.enclosing.dirs());
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
		Code createDir(String name) {

			final Code dir = addBlock(name);

			dir.end();

			return dir;
		}

	}

}
