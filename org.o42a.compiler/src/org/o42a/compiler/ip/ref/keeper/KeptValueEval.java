/*
    Compiler
    Copyright (C) 2012,2013 Ruslan Lopatin

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
package org.o42a.compiler.ip.ref.keeper;

import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;

import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.state.KeeperOp;
import org.o42a.core.ir.op.HostOp;
import org.o42a.core.object.state.Keeper;


final class KeptValueEval implements Eval {

	private final Keeper keeper;

	KeptValueEval(Keeper keeper) {
		this.keeper = keeper;
	}

	@Override
	public void write(DefDirs dirs, HostOp host) {

		final ObjectOp object = host.materialize(
				dirs.dirs(),
				tempObjHolder(dirs.getAllocator()));
		final KeeperOp keeper =
				object.keeper(dirs.dirs(), this.keeper);

		dirs.returnValue(keeper.writeValue(dirs.valDirs()));
	}

	@Override
	public String toString() {
		if (this.keeper == null) {
			return super.toString();
		}
		return this.keeper.getValue().toString();
	}

}
