/*
    Compiler
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
package org.o42a.compiler.ip.type;

import org.o42a.core.object.meta.Nesting;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.value.ValueStructFinder;


final class DefaultTypeConsumer extends TypeConsumer {

	private final Nesting nesting;

	public DefaultTypeConsumer(Nesting nesting) {
		this.nesting = nesting;
	}

	@Override
	public TypeParamConsumer paramConsumer() {
		return new TypeParamConsumer(this.nesting);
	}

	@Override
	public TypeRef consumeType(Ref ref, ValueStructFinder valueStruct) {
		return ref.toTypeRef(valueStruct);
	}

}
