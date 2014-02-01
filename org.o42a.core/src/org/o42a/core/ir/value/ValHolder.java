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
package org.o42a.core.ir.value;

import org.o42a.codegen.code.Code;
import org.o42a.core.ir.value.type.NoValHolder;


public abstract class ValHolder {

	public static final ValHolder NO_VAL_HOLDER = NoValHolder.INSTANCE;

	private final ValOp value;

	public ValHolder(ValOp value) {
		this.value = value;
	}

	public final void set(Code code) {
		if (holdable(this.value)) {
			setValue(code, this.value);
		}
	}

	public final void hold(Code code) {
		if (holdable(this.value)) {
			holdValue(code, this.value);
		}
	}

	public abstract boolean holdable(ValOp value);

	@Override
	public String toString() {
		if (this.value == null) {
			return super.toString();
		}
		return getClass().getSimpleName() + '[' + this.value + ']';
	}

	protected abstract void setValue(Code code, ValOp value);

	protected abstract void holdValue(Code code, ValOp value);

}
