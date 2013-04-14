/*
    Compiler Core
    Copyright (C) 2013 Ruslan Lopatin

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
package org.o42a.core.ir.cmd;

import org.o42a.codegen.code.Code;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.st.sentence.Local;
import org.o42a.util.string.ID;


public final class LocalOp {

	static LocalOp allocateLocal(CodeDirs dirs, Local local, RefOp ref) {

		final ID id = local.getMemberId().toID();
		final Code allocation = dirs.getAllocator().allocation();
		final HostTargetOp target = ref.path().target();
		final TargetStoreOp targetStore =
				target.allocateStore(id, allocation);

		targetStore.storeTarget(dirs);

		return new LocalOp(local, targetStore);
	}

	private final Local local;
	private final TargetStoreOp targetStore;

	private LocalOp(Local local, TargetStoreOp targetStore) {
		this.local = local;
		this.targetStore = targetStore;
	}

	public final Local getLocal() {
		return this.local;
	}

	public final ValOp writeValue(ValDirs dirs) {
		return target(dirs.dirs()).value().writeValue(dirs);
	}

	public final void writeCond(CodeDirs dirs) {
		target(dirs).value().writeCond(dirs);
	}

	public final TargetOp target(CodeDirs dirs) {
		return this.targetStore.loadTarget(dirs);
	}

	@Override
	public String toString() {
		if (this.local == null) {
			return super.toString();
		}
		return this.local.toString();
	}

}
