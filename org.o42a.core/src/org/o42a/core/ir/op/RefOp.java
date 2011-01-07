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
package org.o42a.core.ir.op;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.IRGenerator;
import org.o42a.core.ref.Ref;


public abstract class RefOp {

	private final HostOp host;
	private final Ref ref;

	public RefOp(HostOp host, Ref ref) {
		this.host = host;
		this.ref = ref;
	}

	public final IRGenerator getGenerator() {
		return host().getGenerator();
	}

	public CodeBuilder getBuilder() {
		return host().getBuilder();
	}

	public final HostOp host() {
		return this.host;
	}

	public final Ref getRef() {
		return this.ref;
	}

	public void writeCondition(Code code, CodePos exit) {

		final HostOp target = target(code, exit);

		target.materialize(code, exit).writeCondition(code, exit);
	}

	public final void writeValue(Code code, ValOp result) {
		writeValue(code, null, result);
	}

	public void writeValue(Code code, CodePos exit, ValOp result) {

		final HostOp target = target(code, exit);

		target.materialize(code, exit).writeValue(code, exit, result);
	}

	public abstract HostOp target(Code code, CodePos exit);

}
