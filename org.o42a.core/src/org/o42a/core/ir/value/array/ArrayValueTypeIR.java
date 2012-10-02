/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.core.ir.value.array;

import static org.o42a.core.ir.IRNames.DATA_ID;

import org.o42a.codegen.Generator;
import org.o42a.core.value.array.ArrayValueType;
import org.o42a.util.string.ID;


public class ArrayValueTypeIR implements ArrayIRGenerator {

	private static final ID ROW_DATA_ID = DATA_ID.sub("ROW");
	private static final ID ARRAY_DATA_ID = DATA_ID.sub("ARRAY");

	private final Generator generator;
	private final ArrayValueType valueType;
	private int idSeq;

	public ArrayValueTypeIR(Generator generator, ArrayValueType valueType) {
		this.generator = generator;
		this.valueType = valueType;
	}

	@Override
	public final Generator getGenerator() {
		return this.generator;
	}

	public final ArrayValueType getValueType() {
		return this.valueType;
	}

	@Override
	public ID nextId() {

		final ID prefix;

		if (!getValueType().isVariable()) {
			prefix = ROW_DATA_ID;
		} else {
			prefix = ARRAY_DATA_ID;
		}

		return prefix.anonymous(++this.idSeq);
	}

	@Override
	public String toString() {
		if (this.valueType == null) {
			return super.toString();
		}
		return this.valueType + " IR";
	}

}
