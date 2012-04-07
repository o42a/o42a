/*
    Compiler Core
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
package org.o42a.core.object.def.impl;

import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.local.InlineControl;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.InlineValue;
import org.o42a.core.st.InlineCmd;
import org.o42a.core.value.ValueStruct;
import org.o42a.util.fn.Cancelable;


final class InlineLocalDef extends InlineValue {

	private final InlineCmd command;

	InlineLocalDef(ValueStruct<?, ?> valueStruct, InlineCmd command) {
		super(null, valueStruct);
		this.command = command;
	}

	@Override
	public void writeCond(CodeDirs dirs, HostOp host) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ValOp writeValue(ValDirs dirs, HostOp host) {

		final InlineControl control = new InlineControl(dirs);

		this.command.write(control);

		control.end();

		return control.finalResult();
	}

	@Override
	public String toString() {
		if (this.command == null) {
			return super.toString();
		}
		return this.command.toString();
	}

	@Override
	protected Cancelable cancelable() {
		return null;
	}

}