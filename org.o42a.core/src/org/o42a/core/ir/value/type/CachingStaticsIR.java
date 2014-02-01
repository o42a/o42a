/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.core.ir.value.type;

import java.util.HashMap;

import org.o42a.codegen.data.Global;
import org.o42a.codegen.data.Ptr;
import org.o42a.core.ir.value.ValType;
import org.o42a.util.string.ID;


public abstract class CachingStaticsIR<T> extends StaticsIR<T> {

	private final HashMap<T, Ptr<ValType.Op>> constCache = new HashMap<>();

	public CachingStaticsIR(ValueTypeIR<T> valueTypeIR) {
		super(valueTypeIR);
	}

	@Override
	public Ptr<ValType.Op> valPtr(T value) {

		final Ptr<ValType.Op> cached = this.constCache.get(value);

		if (cached != null) {
			return cached;
		}

		final Global<ValType.Op, ValType> global =
				getGenerator().newGlobal().setConstant().dontExport()
				.newInstance(
						constId(value),
						ValType.VAL_TYPE,
						val(value));
		final Ptr<ValType.Op> result = global.getPointer();

		this.constCache.put(value, result);

		return result;
	}

	protected abstract ID constId(T value);

}
