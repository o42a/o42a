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
package org.o42a.core.ir.op;

import org.o42a.codegen.Generator;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.Ref;


public class RefOp {

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

	public ValOp writeValue(ValDirs dirs) {
		return target(dirs.dirs()).materialize(dirs.dirs()).writeValue(dirs);
	}

	public HostOp target(CodeDirs dirs) {
		return getRef().getPath().op(dirs, host());
	}

}
