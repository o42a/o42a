/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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

import org.o42a.codegen.code.Code;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.member.DeclarationStatement;
import org.o42a.core.member.field.Field;


public final class LocalFieldOp extends StOp {

	private LclOp op;
	private final Field<?> field;

	public LocalFieldOp(
			LocalBuilder builder,
			DeclarationStatement statement,
			Field<?> field) {
		super(builder, statement);
		this.field = field;
	}

	public final Field<?> getField() {
		return this.field;
	}

	@Override
	public void allocate(LocalBuilder builder, Code code) {

		final LocalFieldIRBase<?> fieldIR = this.field.ir(getGenerator());

		this.op = fieldIR.allocate(builder, code);
	}

	@Override
	public void writeAssignment(Control control, ValOp result) {
		this.op.write(control, result);
	}

	@Override
	public void writeCondition(Control control) {
		throw new UnsupportedOperationException();
	}

}
