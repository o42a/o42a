/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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

import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.member.DeclarationStatement;
import org.o42a.core.member.field.Field;


public final class LocalFieldCmd extends Cmd {

	private final Field<?> field;

	public LocalFieldCmd(
			CodeBuilder builder,
			DeclarationStatement statement,
			Field<?> field) {
		super(builder, statement);
		this.field = field;
	}

	public final Field<?> getField() {
		return this.field;
	}

	@Override
	public void write(Control control) {

		final LocalFieldIRBase<?> fieldIR = this.field.ir(getGenerator());
		final LclOp op =
				fieldIR.allocate(control.getBuilder(), control.allocation());

		op.write(control);
	}

}
