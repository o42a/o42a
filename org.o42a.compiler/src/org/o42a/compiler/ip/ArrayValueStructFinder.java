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
package org.o42a.compiler.ip;

import org.o42a.ast.type.ArrayTypeNode;
import org.o42a.core.object.array.ArrayValueStruct;
import org.o42a.core.object.array.ArrayValueType;
import org.o42a.core.ref.Ref;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueStructFinder;
import org.o42a.core.value.ValueType;


final class ArrayValueStructFinder implements ValueStructFinder {

	private final Interpreter interpreter;
	private final ArrayTypeNode node;
	private boolean error;

	ArrayValueStructFinder(Interpreter interpreter, ArrayTypeNode node) {
		this.interpreter = interpreter;
		this.node = node;
	}

	@Override
	public ValueStruct<?, ?> valueStructBy(
			Ref ref,
			ValueStruct<?, ?> defaultStruct) {
		if (this.error) {
			return defaultStruct;
		}

		final ValueType<?> valueType = defaultStruct.getValueType();
		final ArrayValueType arrayType = valueType.toArrayType();

		if (arrayType == null) {
			ref.getLogger().error(
					"unexpected_array_type",
					this.node,
					"Array type can not be specified here");
			this.error = true;
			return defaultStruct;
		}

		final ArrayValueStruct arrayValueStruct =
				this.interpreter.arrayValueStruct(
						this.node,
						ref.distribute(),
						arrayType);

		if (arrayValueStruct == null) {
			this.error = true;
			return defaultStruct;
		}
		if (!defaultStruct.assignableFrom(arrayValueStruct)) {
			ref.getLogger().incompatible(this.node, defaultStruct);
			this.error = true;
			return defaultStruct;
		}

		return arrayValueStruct;
	}

	@Override
	public ValueStruct<?, ?> toValueStruct() {
		return null;
	}

}
