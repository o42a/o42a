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

import org.o42a.codegen.code.op.PtrOp;


public final class Global<O extends PtrOp, T extends Type<O>> {

	private final GlobalSettings settings;
	private final String id;
	private final T instance;

	@SuppressWarnings("unchecked")
	Global(GlobalSettings settings, String id, T type, Content<T> content) {
		this.settings = settings;
		this.id = id;
		this.instance = (T) type.instantiate(this, content);
	}

	@SuppressWarnings("unchecked")
	<S extends Struct<O>> Global(GlobalSettings settings, S struct) {
		this.settings = settings;
		this.id = struct.getId();
		this.instance = (T) struct;
	}

	public final String getId() {
		return this.id;
	}

	public final boolean isExported() {
		return this.settings.isExported();
	}

	public final boolean isConstant() {
		return this.settings.isConstant();
	}

	public final Ptr<O> getPointer() {
		return getInstance().getPointer();
	}

	public final T getInstance() {
		return this.instance;
	}

	@Override
	public String toString() {
		return this.id;
	}

}
