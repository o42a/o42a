/*
    Compiler Core
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
package org.o42a.core.value.impl;

import org.o42a.core.artifact.object.Obj;
import org.o42a.core.source.Intrinsics;
import org.o42a.core.value.SingleValueStruct;
import org.o42a.core.value.SingleValueType;
import org.o42a.core.value.ValueStruct;


public final class IntegerValueType extends SingleValueType<Long> {

	public static final IntegerValueType INSTANCE = new IntegerValueType();

	private IntegerValueType() {
		super("integer");
	}

	@Override
	public SingleValueStruct<Long> struct() {
		return ValueStruct.INTEGER;
	}

	@Override
	public Obj wrapper(Intrinsics intrinsics) {
		return intrinsics.getInteger();
	}

}
