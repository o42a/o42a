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
import org.o42a.codegen.code.AllocationCode;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.op.AnyRecOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.code.op.StructRecOp;
import org.o42a.codegen.data.Type;


public class AllocationDirs {

	private final CodeDirs enclosing;
	private final AllocationCode code;
	private Block falseExit;
	private CodeDirs dirs;

	AllocationDirs(CodeDirs enclosing, AllocationCode code) {
		this.enclosing = enclosing;
		this.code = code;
		this.code.addExit(enclosing.code());
	}

	public final AllocationCode code() {
		return this.code;
	}

	public final CodeId id() {
		return code().id();
	}

	public final CodeId id(String name) {
		return code().id(name);
	}

	public final AnyRecOp allocatePtr(CodeId id) {
		return this.code.allocatePtr(id);
	}

	public final AnyRecOp allocateNull(CodeId id) {
		return this.code.allocateNull(id);
	}

	public final <S extends StructOp<S>> S allocate(CodeId id, Type<S> type) {
		return this.code.allocate(id, type);
	}

	public final <S extends StructOp<S>> StructRecOp<S> allocatePtr(
			CodeId id,
			Type<S> type) {
		return this.code.allocatePtr(id, type);
	}

	public final CodeDirs dirs() {
		if (this.dirs != null) {
			return this.dirs;
		}

		this.falseExit = this.code.addExitBlock("false");

		return this.dirs = new CodeDirs(
				this.enclosing.getBuilder(),
				this.enclosing.code(),
				this.falseExit.head());
	}

	public void done() {
		this.code.done();
		if (this.dirs == null) {
			return;
		}
		if (this.falseExit.exists()) {
			this.falseExit.go(this.enclosing.falseDir());
		}
	}

	@Override
	public String toString() {
		return this.enclosing.toString("AllocationDirs", this.code);
	}

}
