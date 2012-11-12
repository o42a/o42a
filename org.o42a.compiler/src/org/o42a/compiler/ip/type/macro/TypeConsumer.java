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
package org.o42a.compiler.ip.type.macro;

import org.o42a.core.object.meta.Nesting;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.value.ValueStructFinder;


public abstract class TypeConsumer {

	public static final TypeConsumer NO_TYPE_CONSUMER = NoTypeConsumer.INSTANCE;

	public static TypeConsumer typeConsumer(Nesting nesting) {
		return new DefaultTypeConsumer(nesting);
	}

	public abstract TypeConsumer paramConsumer(int index);

	public abstract TypeRef consumeType(Ref ref, ValueStructFinder valueStruct);

}
