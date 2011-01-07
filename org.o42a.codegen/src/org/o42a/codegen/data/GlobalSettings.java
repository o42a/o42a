/*
    Compiler Code Generator
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.codegen.data;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.op.PtrOp;


public final class GlobalSettings {

	private final Globals globals;

	private boolean exported;
	private boolean constant;

	GlobalSettings(Globals globals) {
		this.globals = globals;
	}

	public final Generator getGenerator() {
		return (Generator) this.globals;
	}

	public final boolean isExported() {
		return this.exported;
	}

	public final GlobalSettings export() {
		this.exported = true;
		return this;
	}

	public GlobalSettings dontExport() {
		this.exported = false;
		return this;
	}

	public final boolean isConstant() {
		return this.constant;
	}

	public final GlobalSettings setConstant() {
		this.constant = true;
		return this;
	}

	public final GlobalSettings setVariable() {
		this.constant = false;
		return this;
	}

	public final <O extends PtrOp, T extends Type<O>> Global<O, T> create(
			String id,
			T type) {
		return this.globals.addGlobal(this, id, type, null);
	}

	public final <O extends PtrOp, T extends Type<O>> Global<O, T> create(
			String id,
			T type,
			Content<T> content) {
		return this.globals.addGlobal(this, id, type, content);
	}

	public final <O extends PtrOp, S extends Struct<O>> Global<O, S> create(
			S struct) {
		return this.globals.addGlobal(this, struct);
	}

}
