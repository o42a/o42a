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

import org.o42a.codegen.Generator;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.st.Statement;


public abstract class StOp {

	public static StOp noStOp(CodeBuilder builder, Statement statement) {
		return new NoStOp(builder, statement);
	}

	private final CodeBuilder builder;
	private final Statement statement;

	public StOp(CodeBuilder builder, Statement statement) {
		this.builder = builder;
		this.statement = statement;
	}

	public final Generator getGenerator() {
		return getBuilder().getGenerator();
	}

	public final CodeBuilder getBuilder() {
		return this.builder;
	}

	public final Statement getStatement() {
		return this.statement;
	}

	public abstract void writeLogicalValue(Control control);

	public abstract void writeValue(Control control, ValOp result);

}
