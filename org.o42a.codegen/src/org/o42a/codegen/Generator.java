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
package org.o42a.codegen;

import static org.o42a.codegen.CodeIdFactory.DEFAULT_CODE_ID_FACTORY;

import org.o42a.codegen.debug.Debug;


public abstract class Generator extends Debug {

	private final String id;

	public Generator(String id) {
		if (id == null) {
			throw new NullPointerException(
					"Generator identifier not specified");
		}
		this.id = id;
	}

	public String getId() {
		return this.id;
	}

	public CodeIdFactory getCodeIdFactory() {
		return DEFAULT_CODE_ID_FACTORY;
	}

	public final CodeId id() {
		return getCodeIdFactory().id();
	}

	public final CodeId topId() {
		return getCodeIdFactory().topId();
	}

	public final CodeId id(String name) {
		return getCodeIdFactory().id(name);
	}

	public final CodeId rawId(String id) {
		return getCodeIdFactory().rawId(id);
	}

}
