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
import org.o42a.core.ir.IRGenerator;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.st.St;


public abstract class StOp {

	private final LocalBuilder builder;
	private final St statement;

	public StOp(LocalBuilder builder, St statement) {
		this.builder = builder;
		this.statement = statement;
	}

	public final IRGenerator getGenerator() {
		return getBuilder().getGenerator();
	}

	public final LocalBuilder getBuilder() {
		return this.builder;
	}

	public final St getStatement() {
		return this.statement;
	}

	public abstract void allocate(LocalBuilder builder, Code code);

	public abstract void writeAssignment(Control control, ValOp result);

	public abstract void writeCondition(Control control);

}
