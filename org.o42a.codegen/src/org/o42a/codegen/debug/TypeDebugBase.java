/*
    Compiler Code Generator
    Copyright (C) 2011-2014 Ruslan Lopatin

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

import org.o42a.codegen.data.Type;


public abstract class TypeDebugBase {

	public abstract Type<?> getType();

	public final DebugTypeInfo getTypeInfo() {
		if (!getType().isDebuggable()) {
			return null;
		}

		final Type<?> type = getType();
		final Debug debug = type.getGenerator().getDebug();

		return debug.typeInfo(type);
	}

	protected DebugTypeInfo createTypeInfo() {
		return new DefaultTypeInfo(getType());
	}

	protected final DebugTypeInfo externalTypeInfo(int code) {

		final String id = getType().getId().toString();
		final String suffix;

		if (id.endsWith("_t")) {
			suffix = id.substring(0, id.length() - 2);
		} else {
			suffix = id;
		}

		return externalTypeInfo("_O42A_DEBUG_TYPE_" + suffix, code);
	}

	protected final DebugTypeInfo externalTypeInfo(String name, int code) {
		return new ExternalTypeInfo(getType(), name, code);
	}

}
