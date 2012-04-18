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
import org.o42a.codegen.code.CodePos;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;


public final class DefDirs {

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

	public final ValueStruct<?, ?> getValueStruct() {
		return valDirs().getValueStruct();
	}

	public final ValueType<?> getValueType() {
		return getValueStruct().getValueType();
	}

	public final boolean isFalseWhenUnknown() {
		return dirs().isFalseWhenUnknown();
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

	public final CodePos unknownDir() {
		return valDirs().unknownDir();
	}

	public final Block code() {
		return valDirs().code();
	}

	public final ValOp value() {
		return valDirs().value();
	}

	public final ValOp result() {
		if (this.shared.result != null) {
			return this.shared.result;
		}
		return value();
	}

	public final void returnValue(ValOp value) {
		returnValue(code(), value);
	}

	public final void returnValue(Block code, ValOp value) {
		this.shared.result = value;
		code.go(this.shared.returnDir);
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

	public final DefDirs falseWhenUnknown() {

		final ValDirs dirs = valDirs();

		if (dirs.falseDir() == dirs.unknownDir()) {
			return this;
		}

		return new DefDirs(this, dirs.falseWhenUnknown());
	}

	public final void done() {
		valDirs().done();
	}

	@Override
	public String toString() {
		if (this.valDirs == null) {
			return super.toString();
		}
		return dirs().toString("DefDirs", code());
	}

	private static final class Shared {

		private final CodePos returnDir;
		private ValOp result;

		Shared(CodePos returnDir) {
			this.returnDir = returnDir;
		}

	}

}
