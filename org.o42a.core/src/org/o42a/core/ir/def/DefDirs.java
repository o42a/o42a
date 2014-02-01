/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.*;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.value.ValueType;
import org.o42a.util.string.ID;


public class DefDirs {

	private final ValDirs valDirs;
	private final Shared shared;

	public DefDirs(ValDirs valDirs, CodePos returnDir) {
		assert valDirs != null :
			"Value directions not specified";
		assert returnDir != null :
			"Return direction not specified";
		this.valDirs = valDirs;
		this.shared = new Shared(returnDir);
	}

	private DefDirs(DefDirs prototype, ValDirs valDirs) {
		this.valDirs = valDirs;
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

	public final Allocator getAllocator() {
		return valDirs().getAllocator();
	}

	public final ValueType<?> getValueType() {
		return valDirs().getValueType();
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
		code.go(this.shared.returnDir);
	}

	public final Block addBlock(String name) {
		return valDirs().addBlock(name);
	}

	public final Block addBlock(ID name) {
		return valDirs().addBlock(name);
	}

	public final DefDirs sub(Block code) {
		return new DefDirs(this, valDirs().sub(code));
	}

	public final DefDirs begin(ID id, String message) {
		return new DefDirs(this, valDirs().begin(id, message));
	}

	public final DefDirs setFalseDir(CodePos falsePos) {
		return new DefDirs(this, valDirs().setFalseDir(falsePos));
	}

	public CodeDirs done() {
		return valDirs().done();
	}

	@Override
	public String toString() {
		if (this.valDirs == null) {
			return super.toString();
		}
		return dirs().toString("DefDirs", code());
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
			assert getValueType().is(result.getValueType()) :
				"Wrong value type: " + result.getValueType()
				+ ", but " + getValueType() + " expected";
			if (this.result == null
					&& valueAccessibleBy(code)
					&& !holdableValue(result)) {
				this.result = result;
				this.singleResultInset = code.inset("store_def");
				return;
			}
			if (!this.storeInstantly) {
				if (this.singleResultInset != null) {
					value().store(this.singleResultInset, this.result);
					this.singleResultInset = null;
				}
				this.storeInstantly = true;
				this.result = value();
			}
			value().store(code, result);
		}


		private boolean valueAccessibleBy(Code code) {
			if (!valDirs().value().isStackAllocated()) {
				return true;
			}
			return code.getAllocator() == valDirs().code().getAllocator();
		}

		private boolean holdableValue(ValOp result) {

			final ValOp value = valDirs().value();

			if (value == result) {
				return false;
			}

			return value.holder().holdable(result);
		}

	}

}
