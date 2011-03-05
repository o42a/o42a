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
package org.o42a.codegen.debug;

import org.o42a.codegen.Generator;
import org.o42a.codegen.data.Content;
import org.o42a.codegen.data.Data;


final class DbgGlobal implements Content<DbgGlobalType> {

	private final Data<?> global;

	DbgGlobal(Data<?> global) {
		this.global = global;
	}

	@Override
	public void allocated(DbgGlobalType instance) {
	}

	@Override
	public void fill(DbgGlobalType instance) {

		final Generator generator = instance.getData().getGenerator();
		final Debug debug = generator;

		debug.setName(
				instance.getName(),
				generator.id("DEBUG")
				.sub("GLOBAL_NAME")
				.sub(this.global.getId()),
				this.global.getId().getId());
		instance.getStart().setValue(this.global.getPointer().toAny());
		instance.getContent().fill(generator, null, this.global);
	}

}
