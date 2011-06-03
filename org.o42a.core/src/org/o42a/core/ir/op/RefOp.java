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

import org.o42a.codegen.Generator;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ref.Ref;


public abstract class RefOp {

	private final HostOp host;
	private final Ref ref;

	public RefOp(HostOp host, Ref ref) {
		this.host = host;
		this.ref = ref;
	}

	public final Generator getGenerator() {
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

	public void writeLogicalValue(CodeDirs dirs) {

		final HostOp target = target(dirs);

		target.materialize(dirs).writeLogicalValue(dirs);
	}

	public void writeValue(CodeDirs dirs, ValOp result) {

		final HostOp target = target(dirs);
		final ValDirs valDirs = dirs.value(dirs.id("ref_value"), result);

		target.materialize(dirs).writeValue(valDirs);

		valDirs.done();
	}

	public abstract HostOp target(CodeDirs dirs);

}
