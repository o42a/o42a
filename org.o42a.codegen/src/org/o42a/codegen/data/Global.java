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

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.op.StructOp;


public final class Global<S extends StructOp<S>, T extends Type<S>> {

	private final GlobalSettings settings;
	private final CodeId id;
	private final T instance;

	Global(
			GlobalSettings settings,
			CodeId id,
			T type,
			T instance,
			Content<T> content) {
		this.settings = settings;
		this.id = id;
		this.instance = type.instantiate(this, instance, content);
	}

	Global(GlobalSettings settings, T struct) {
		this.settings = settings;
		this.id = struct.codeId(settings.getGenerator());
		this.instance = struct;
	}

	public final Generator getGenerator() {
		return this.settings.getGenerator();
	}

	public final CodeId getId() {
		return this.id;
	}

	public final boolean isExported() {
		return this.settings.isExported();
	}

	public final boolean isConstant() {
		return this.settings.isConstant();
	}

	public final Ptr<S> getPointer() {
		return getInstance().pointer(getGenerator());
	}

	public final T getInstance() {
		return this.instance;
	}

	public final GlobalSettings update(GlobalSettings settings) {
		return settings.set(this.settings);
	}

	@Override
	public String toString() {
		return this.id.toString();
	}

}
