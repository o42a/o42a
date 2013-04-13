/*
    Compiler Code Generator
    Copyright (C) 2010-2013 Ruslan Lopatin

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
import org.o42a.codegen.code.op.StructOp;
import org.o42a.util.string.ID;


public final class Global<S extends StructOp<S>, T extends Type<S>>
		implements GlobalAttributes {

	private final GlobalSettings settings;
	private final ID id;
	private final T instance;

	Global(
			GlobalSettings settings,
			ID id,
			T type,
			T instance,
			Content<? extends T> content) {
		this.settings = settings;
		this.id = id;
		this.instance = type.instantiate(this, instance, content);
	}

	Global(GlobalSettings settings, T struct) {
		this.settings = settings;
		this.id = struct.getId();
		this.instance = struct;
	}

	public final Generator getGenerator() {
		return this.settings.getGenerator();
	}

	public final ID getId() {
		return this.id;
	}

	@Override
	public final boolean isExported() {
		return this.settings.isExported();
	}

	@Override
	public final boolean isConstant() {
		return this.settings.isConstant();
	}

	@Override
	public int getDataFlags() {
		return this.settings.getDataFlags();
	}

	public final Ptr<S> getPointer() {
		return getInstance().pointer(getGenerator());
	}

	public final T getInstance() {
		return this.instance;
	}

	@Override
	public String toString() {
		if (this.id == null) {
			return super.toString();
		}
		return this.id.toString();
	}

}
