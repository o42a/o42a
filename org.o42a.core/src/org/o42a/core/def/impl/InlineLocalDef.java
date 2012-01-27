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
package org.o42a.core.def.impl;

import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.local.InlineControl;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.InlineValue;
import org.o42a.core.st.InlineCommand;
import org.o42a.core.value.ValueStruct;


final class InlineLocalDef extends InlineValue {

	private final InlineCommand command;

	InlineLocalDef(
			ValueStruct<?, ?> valueStruct,
			InlineCommand command) {
		super(valueStruct);
		this.command = command;
	}

	@Override
	public void writeCond(CodeDirs dirs, HostOp host) {

		final InlineControl control = control(dirs);

		this.command.writeCond(control);

		control.end();
	}

	@Override
	public ValOp writeValue(ValDirs dirs, HostOp host) {

		final InlineControl control = control(dirs.dirs());
		final ValOp value = dirs.value();

		this.command.writeValue(control, value);

		control.end();

		return value;
	}

	@Override
	public void cancel() {
		this.command.cancel();
	}

	@Override
	public String toString() {
		if (this.command == null) {
			return super.toString();
		}
		return this.command.toString();
	}

	private final InlineControl control(CodeDirs dirs) {
		return new InlineControl(
				dirs.getBuilder(),
				dirs.code(),
				dirs.unknownDir(),
				dirs.falseDir());
	}

}