/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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

import static org.o42a.core.ir.value.ValOp.stackAllocatedVal;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Allocator;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.DefStore;
import org.o42a.core.ir.value.ValHolderFactory;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.value.ValueType;
import org.o42a.util.string.ID;


public abstract class ValDirs {

	private final CodeDirs dirs;
	private final ValueType<?> valueType;

	ValDirs(CodeDirs dirs, ValueType<?> valueType) {
		this.dirs = dirs;
		this.valueType = valueType;
	}

	public final Generator getGenerator() {
		return dirs().getGenerator();
	}

	public final CodeBuilder getBuilder() {
		return dirs().getBuilder();
	}

	public final ValueType<?> getValueType() {
		return this.valueType;
	}

	public final boolean isDebug() {
		return dirs().isDebug();
	}

	public final Allocator getAllocator() {
		return dirs().getAllocator();
	}

	public final Block code() {
		return dirs().code();
	}

	public final Block addBlock(String name) {
		return dirs().addBlock(name);
	}

	public final Block addBlock(ID name) {
		return dirs().addBlock(name);
	}

	public final CodePos falseDir() {
		return dirs().falseDir();
	}

	public ValOp value() {
		return topLevel().value();
	}

	public final CodeDirs dirs() {
		return this.dirs;
	}

	public final DefDirs def() {
		return new ValDefDirs(this, addBlock("result"));
	}

	public final DefDirs def(CodePos returnDir) {
		return new DefDirs(new DefStore(this), returnDir);
	}

	public final ValDirs sub(Block code) {
		return dirs().sub(code).value(this);
	}

	public final ValDirs nested() {
		return new NestedValDirs(dirs().nested(), this);
	}

	public final ValDirs setFalseDir(CodePos falseDir) {
		return dirs().setFalseDir(falseDir).value(this);
	}

	public final ValDirs begin(String id, String message) {
		return begin(id != null ? ID.id(id) : null, message);
	}

	public final ValDirs begin(ID id, String message) {
		return new NestedValDirs(dirs().begin(id, message), this);
	}

	public CodeDirs done() {
		return dirs().done();
	}

	@Override
	public String toString() {
		return dirs().toString(getClass().getSimpleName(), code());
	}

	abstract TopLevelValDirs topLevel();

	static final class TopLevelValDirs extends ValDirs {

		private final ValOp value;

		TopLevelValDirs(
				CodeDirs dirs,
				ValueType<?> valueType,
				ValHolderFactory holderFactory) {
			super(dirs, valueType);
			this.value = stackAllocatedVal(
					"value",
					dirs.code().getAllocator(),
					getBuilder(),
					valueType,
					holderFactory);
		}

		TopLevelValDirs(CodeDirs dirs, ValOp value) {
			super(dirs, value.getValueType());
			this.value = value;
		}

		@Override
		public final ValOp value() {
			return this.value;
		}

		@Override
		final TopLevelValDirs topLevel() {
			return this;
		}

	}

	static final class NestedValDirs extends ValDirs {

		private TopLevelValDirs topLevel;

		NestedValDirs(CodeDirs dirs, ValDirs storage) {
			super(dirs, storage.getValueType());
			this.topLevel = storage.topLevel();
		}

		@Override
		TopLevelValDirs topLevel() {
			return this.topLevel;
		}

	}

	private static final class ValDefDirs extends DefDirs {

		private final Block returnCode;

		ValDefDirs(ValDirs valDirs, Block returnCode) {
			super(new DefStore(valDirs), returnCode.head());
			this.returnCode = returnCode;
		}

		@Override
		public CodeDirs done() {

			final CodeDirs result = super.done();

			if (result.code().exists()) {
				result.code().go(falseDir());
			}
			if (this.returnCode.exists()) {
				this.returnCode.go(result.code().tail());
			}

			return result;
		}

	}

}
