/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.Ref;


public final class RefOp {

	private final HostOp host;
	private final Ref ref;

	public RefOp(HostOp host, Ref ref) {
		this.host = host;
		this.ref = ref;
	}

	public final Generator getGenerator() {
		return host().getGenerator();
	}

	public final CodeBuilder getBuilder() {
		return host().getBuilder();
	}

	public final OpPresets getPresets() {
		return host().getPresets();
	}

	public final RefOp setPresets(OpPresets presets) {

		final HostOp oldHost = host();
		final HostOp newHost = oldHost.setPresets(presets);

		if (oldHost == newHost) {
			return this;
		}

		return new RefOp(newHost, getRef());
	}

	public final HostOp host() {
		return this.host;
	}

	public final Ref getRef() {
		return this.ref;
	}

	public final ValOp writeValue(ValDirs dirs) {
		return path().value().writeValue(dirs);
	}

	public final void writeCond(CodeDirs dirs) {
		path().value().writeCond(dirs);
	}

	public final HostOp path() {
		return getRef().getPath().op(host());
	}

	@Override
	public String toString() {
		if (this.ref == null) {
			return super.toString();
		}
		return this.ref.toString();
	}

}
