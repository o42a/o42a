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
package org.o42a.core.value.impl;

import org.o42a.core.object.Obj;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.Intrinsics;
import org.o42a.core.value.SingleValueStruct;
import org.o42a.core.value.SingleValueType;
import org.o42a.core.value.ValueStruct;


public class NoneValueType extends SingleValueType<Void> {

	public static final NoneValueType INSTANCE = new NoneValueType();

	private NoneValueType() {
		super("none");
	}

	@Override
	public SingleValueStruct<Void> struct() {
		return ValueStruct.NONE;
	}

	@Override
	public Obj typeObject(Intrinsics intrinsics) {
		throw new UnsupportedOperationException("Type NONE does not exist");
	}

	@Override
	public Path path(Intrinsics intrinsics) {
		throw new UnsupportedOperationException("Type NONE does not exist");
	}

}
