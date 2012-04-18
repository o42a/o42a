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
package org.o42a.core.ref.impl.normalizer;

import static org.o42a.core.value.Value.voidValue;

import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.value.ValueStruct;
import org.o42a.util.fn.Cancelable;


public class VoidInlineValue extends InlineValue {

	public VoidInlineValue() {
		super(null, ValueStruct.VOID);
	}

	@Override
	public void writeCond(CodeDirs dirs, HostOp host) {
	}

	@Override
	public ValOp writeValue(ValDirs dirs, HostOp host) {
		return voidValue().op(dirs.getBuilder(), dirs.code());
	}

	@Override
	public String toString() {
		return "VOID";
	}

	@Override
	protected Cancelable cancelable() {
		return null;
	}

}
