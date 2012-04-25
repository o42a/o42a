/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.ir.def;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;


public class DefDirs {

	private final ValDirs valDirs;
	private final Shared shared;
	private final boolean ownsValDirs;

	public DefDirs(ValDirs valDirs, CodePos returnDir, boolean ownsValDirs) {
		assert valDirs != null :
			"Value directions not specified";
		assert returnDir != null :
			"Return direction not specified";
		this.valDirs = valDirs;
		this.ownsValDirs = ownsValDirs;
		this.shared = new Shared(returnDir);
	}

	private DefDirs(DefDirs prototype, ValDirs valDirs, boolean ownsValDirs) {
		this.valDirs = valDirs;
		this.ownsValDirs = ownsValDirs;
		this.shared = prototype.shared;
	}

	public final Generator getGenerator() {
		return valDirs().getGenerator();
	}

	public final CodeBuilder getBuilder() {
		return valDirs().getBuilder();
	}

	public final boolean isDebug() {
		return dirs().isDebug();
	}

	public final ValueStruct<?, ?> getValueStruct() {
		return valDirs().getValueStruct();
	}

	public final ValueType<?> getValueType() {
		return getValueStruct().getValueType();
	}

	public final CodeDirs dirs() {
		return valDirs().dirs();
	}

	public final ValDirs valDirs() {
		return this.valDirs;
	}

	public final CodePos falseDir() {
		return valDirs().falseDir();
	}

	public final Block code() {
		return valDirs().code();
	}

	public final ValOp value() {
		return valDirs().value();
	}

	public ValOp result() {
		if (this.shared.result != null) {
			return this.shared.result;
		}
		return value();
	}

	public final void returnValue(ValOp value) {
		returnValue(code(), value);
	}

	public void returnValue(Block code, ValOp value) {
		this.shared.store(code, value);
		code.go(returnDir());
	}

	public final CodeId id() {
		return code().id();
	}

	public final CodeId id(String name) {
		return code().id(name);
	}

	public final Block addBlock(String name) {
		return valDirs().addBlock(name);
	}

	public final Block addBlock(CodeId name) {
		return valDirs().addBlock(name);
	}

	public final DefDirs sub(String name) {
		return sub(addBlock(name));
	}

	public final DefDirs sub(CodeId name) {
		return sub(addBlock(name));
	}

	public final DefDirs sub(Block code) {
		return new DefDirs(this, valDirs().sub(code), true);
	}

	public final DefDirs begin(String message) {

		final ValDirs oldDirs = valDirs();
		final ValDirs newDirs = oldDirs.begin(message);

		if (newDirs == oldDirs) {
			return new DefDirs(this, newDirs, false);
		}

		return new DebugDefDirs(this, newDirs);
	}

	public final DefDirs setFalseDir(CodePos falsePos) {

		final ValDirs dirs = valDirs();
		final ValDirs newDirs = dirs.setFalseDir(falsePos);

		return new DefDirs(this, newDirs, dirs != newDirs);
	}

	public void done() {
		if (this.ownsValDirs) {
			valDirs().done();
		}
	}

	@Override
	public String toString() {
		if (this.valDirs == null) {
			return super.toString();
		}
		return dirs().toString("DefDirs", code());
	}

	CodePos returnDir() {
		return this.shared.returnDir;
	}

	private final class Shared {

		private final CodePos returnDir;
		private ValOp result;
		private Code singleResultInset;
		private boolean storeInstantly;

		Shared(CodePos returnDir) {
			this.returnDir = returnDir;
		}

		private void store(Code code, ValOp result) {
			if (this.result == null) {
				this.result = result;
				this.singleResultInset = code.inset("store_def");
				return;
			}
			if (!this.storeInstantly) {
				value().store(this.singleResultInset, this.result);
				this.result = value();
				this.storeInstantly = true;
			}
			value().store(code, result);
		}

	}

	private static final class DebugDefDirs extends DefDirs {

		private final DefDirs enclosing;
		private Block returnCode;

		DebugDefDirs(DefDirs enclosing, ValDirs valDirs) {
			super(enclosing, valDirs, true);
			this.enclosing = enclosing;
		}

		@Override
		public void done() {
			super.done();
			if (this.returnCode != null && this.returnCode.exists()) {
				this.returnCode.end();
				this.enclosing.returnValue(this.returnCode, result());
			}
		}

		@Override
		CodePos returnDir() {
			if (this.returnCode == null) {
				this.returnCode = addBlock("debug_result");
			}
			return this.returnCode.head();
		}

	}

}
