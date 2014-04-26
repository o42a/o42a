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
import org.o42a.codegen.code.Allocator;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.value.ValueType;
import org.o42a.util.string.ID;


public class DefDirs {

	private final ValDirs valDirs;
	private final DefStore store;
	private final CodePos returnDir;

	public DefDirs(DefStore store, CodePos returnDir) {
		assert store != null :
			"Value definition store not specified";
		assert returnDir != null :
			"Return direction not specified";
		this.valDirs = store.valDirs();
		this.store = store;
		this.returnDir = returnDir;
	}

	public DefDirs(DefStore store, ValDirs valDirs, CodePos returnDir) {
		this.store = store;
		this.valDirs = valDirs;
		this.returnDir = returnDir;
	}

	private DefDirs(DefDirs prototype, ValDirs valDirs) {
		this.store = prototype.store;
		this.valDirs = valDirs;
		this.returnDir = prototype.returnDir;
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

		final ValOp storedResult = this.store.result();

		return storedResult != null ? storedResult : value();
	}

	public final DefStore store() {
		return this.store;
	}

	public final CodePos returnDir() {
		return this.returnDir;
	}

	public final void returnValue(ValOp value) {
		returnValue(code(), value);
	}

	public void returnValue(Block code, ValOp value) {
		this.store.store(code, value);
		code.go(this.returnDir);
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

	public final DefDirs setReturnDir(CodePos returnDir) {
		return new DefDirs(store(), valDirs().nested(), returnDir);
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

}
