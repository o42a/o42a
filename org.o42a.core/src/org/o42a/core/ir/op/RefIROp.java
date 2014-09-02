/*
    Compiler Core
    Copyright (C) 2013,2014 Ruslan Lopatin

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

import org.o42a.codegen.code.op.DataPtrOp;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.PathIR;


public final class RefIROp extends PathIR implements RefTargetOp {

	private final RefIR ir;
	private final RefTargetOp targetOp;

	public RefIROp(RefIR ir, RefTargetOp targetOp) {
		this.ir = ir;
		this.targetOp = targetOp;
	}

	@Override
	public final DataPtrOp<?> ptr() {
		return this.targetOp.ptr();
	}

	@Override
	public final void storeTarget(CodeDirs dirs, HostOp start) {

		final BoundPath path = this.ir.ref().getPath();
		final HostOp lastStart = pathOp(path, start, path.length() - 1);

		this.targetOp.storeTarget(dirs, lastStart);
	}

	@Override
	public void copyTarget(CodeDirs dirs, TargetStoreOp store) {
		this.targetOp.copyTarget(dirs, store);
	}

	@Override
	public final TargetOp loadTarget(CodeDirs dirs) {
		return this.targetOp.loadTarget(dirs);
	}

	@Override
	public String toString() {
		if (this.targetOp == null) {
			return super.toString();
		}
		return this.targetOp.toString();
	}

}
