/*
    Compiler Core
    Copyright (C) 2013,2014 Ruslan Lopatin

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
package org.o42a.core.ir.op;

import org.o42a.core.ir.cmd.CmdState;
import org.o42a.core.ir.cmd.Control;
import org.o42a.core.ir.cmd.InlineCmd;
import org.o42a.core.ir.value.ValOp;
import org.o42a.util.fn.Cancelable;


final class InlineValueCmd extends InlineCmd<Void> {

	private final InlineValue inline;

	InlineValueCmd(InlineValue inline) {
		super(null);
		this.inline = inline;
	}

	@Override
	public void write(Control control, CmdState<Void> state) {

		final ValDirs dirs =
				control.getBuilder().dirs(
						control.code(),
						control.falseDir())
				.value(control.result());
		final ValOp value = this.inline.writeValue(dirs, control.host());

		control.returnValue(dirs.code(), value);

		dirs.done();
		state.done();
	}

	@Override
	public String toString() {
		if (this.inline == null) {
			return super.toString();
		}
		return this.inline.toString();
	}

	@Override
	protected Cancelable cancelable() {
		return null;
	}

}
