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
package org.o42a.core.ref.impl.cond;

import org.o42a.core.ir.cmd.Control;
import org.o42a.core.ir.cmd.InlineCmd;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.util.fn.Cancelable;


final class InlineRefConditionCmd extends InlineCmd {

	private final InlineValue value;

	InlineRefConditionCmd(InlineValue value) {
		super(null);
		this.value = value;
	}

	@Override
	public void write(Control control) {
		this.value.writeCond(control.dirs(), control.host());
	}

	@Override
	public String toString() {
		if (this.value == null) {
			return super.toString();
		}
		return "(++" + this.value + ")";
	}

	@Override
	protected Cancelable cancelable() {
		return null;
	}

}
