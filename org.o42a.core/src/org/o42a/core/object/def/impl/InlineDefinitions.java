/*
    Compiler Core
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
package org.o42a.core.object.def.impl;

import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;

import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.value.ValueType;
import org.o42a.util.fn.Cancelable;


public class InlineDefinitions extends InlineValue {

	private final ValueType<?> valueType;
	private final InlineEval defs;

	public InlineDefinitions(ValueType<?> valueType, InlineEval defs) {
		super(null);
		this.valueType = valueType;
		this.defs = defs;
	}

	@Override
	public void writeCond(CodeDirs dirs, HostOp host) {

		final DefDirs defDirs =
				dirs.nested().value(this.valueType, TEMP_VAL_HOLDER).def();

		this.defs.write(defDirs, host);
		defDirs.done();
	}

	@Override
	public ValOp writeValue(ValDirs dirs, HostOp host) {

		final DefDirs defDirs = dirs.nested().def();

		this.defs.write(defDirs, host);
		defDirs.done();

		return defDirs.result();
	}

	@Override
	public String toString() {
		if (this.defs == null) {
			return super.toString();
		}

		final StringBuilder out = new StringBuilder();

		out.append('(');
		out.append(this.defs);
		out.append(')');

		return out.toString();
	}

	@Override
	protected Cancelable cancelable() {
		return null;
	}

}
