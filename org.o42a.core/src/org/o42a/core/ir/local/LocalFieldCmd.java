/*
    Compiler Core
    Copyright (C) 2010-2013 Ruslan Lopatin

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
package org.o42a.core.ir.local;

import org.o42a.core.member.field.Field;


public final class LocalFieldCmd implements Cmd {

	private final Field field;

	public LocalFieldCmd(Field field) {
		this.field = field;
	}

	public final Field getField() {
		return this.field;
	}

	@Override
	public void write(Control control) {

		final LocalFieldIRBase fieldIR = this.field.ir(control.getGenerator());
		final LclOp op =
				fieldIR.allocate(control.getBuilder(), control.allocation());

		op.write(control);
	}

	@Override
	public String toString() {
		if (this.field == null) {
			return super.toString();
		}
		return "LocalField[" + this.field.getKey() + ']';
	}

}
