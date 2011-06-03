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

import static org.o42a.core.ir.op.ValOp.VAL_TYPE;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;


public abstract class ValDirs {

	private final Code code;
	private Code falseCode;
	private Code unknownCode;
	private CodeDirs dirs;

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

	public final ValOp value() {

		final TopLevelValDirs topLevel = topLevel();

		if (topLevel.value != null) {
			return topLevel.value;
		}

		topLevel.allocation = topLevel.enclosing.allocate("value");
		topLevel.value =
			topLevel.allocation.allocate(id("value"), VAL_TYPE)
			.storeIndefinite(topLevel.allocation.code());

		return topLevel.value;
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

		if (dirs.falsePos() == dirs.unknownPos()) {
			return this;
		}

		return new FalseWhenUnknownValDirs(this, code());
	}

	public ValDirs begin(String id, String message) {
		if (!isDebug()) {
			return this;
		}

		this.code.begin(message);

		return new NestedValDirs(this, id(id));
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

		final CodePos falsePos = enclosing.falsePos();
		final CodePos unknownPos = enclosing.unknownPos();

		if (falsePos == null) {
			this.falseCode = null;
		} else {
			this.falseCode = createDir("false");
		}
		if (unknownPos == falsePos) {
			this.unknownCode = this.falseCode;
		} else if (unknownPos == null) {
			this.unknownCode = null;
		} else {
			this.unknownCode = createDir("unknown");
		}

		return new CodeDirs(
				this.code,
				this.falseCode != null ? this.falseCode.head() : null,
				this.unknownCode != null ? this.unknownCode.head() : null);
	}

	abstract Code createDir(String name);

	void handleDirs(CodeDirs enclosing) {
		dirs();
		if (this.falseCode != null) {
			enclosing.goWhenFalse(this.falseCode);
		}
		if (this.unknownCode != null) {
			enclosing.goWhenUnknown(this.unknownCode);
		}
	}

	static final class TopLevelValDirs extends ValDirs {

		private final CodeDirs enclosing;
		private AllocationDirs allocation;
		private ValOp value;

		TopLevelValDirs(CodeDirs enclosing, CodeId name) {
			super(enclosing.addBlock(name));
			this.enclosing = enclosing;
		}

		public TopLevelValDirs(CodeDirs enclosing, CodeId name, ValOp value) {
			super(enclosing.addBlock(name));
			this.enclosing = enclosing;
			this.value = value;
		}

		@Override
		public void done() {
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

	private static final class NestedValDirs extends ValDirs {

		private final ValDirs enclosing;
		private final TopLevelValDirs topLevel;

		NestedValDirs(ValDirs enclosing, CodeId name) {
			super(enclosing.addBlock(name));
			this.enclosing = enclosing;
			this.topLevel = enclosing.topLevel();
			enclosing.code().go(code().head());
		}

		@Override
		public void done() {
			code().end();
			code().go(this.enclosing.code().tail());
			handleDirs(this.enclosing.dirs);
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
