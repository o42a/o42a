/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.object.state.impl;

import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;

import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.state.KeeperOp;


final class KeeperAccessEval implements Eval {

	private final KeeperAccessDef def;

	KeeperAccessEval(KeeperAccessDef def) {
		this.def = def;
	}

	@Override
	public void write(DefDirs dirs, HostOp host) {

		final ObjectOp object =
				this.def.object().op(host)
				.target(dirs.dirs())
				.materialize(
						dirs.dirs(),
						tempObjHolder(dirs.getAllocator()));
		final KeeperOp keeper =
				object.keeper(dirs.dirs(), this.def.getKeeper());

		dirs.returnValue(keeper.writeValue(dirs.valDirs()));
	}

	@Override
	public String toString() {
		if (this.def == null) {
			return super.toString();
		}
		return this.def.toString();
	}

}
