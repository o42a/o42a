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
package org.o42a.core.ir.cmd;

import org.o42a.codegen.code.Code;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.local.MemberLocal;
import org.o42a.core.st.sentence.Local;
import org.o42a.util.string.ID;


public final class LocalOp {

	private static final ID FILL_LOCAL_ID = ID.id("fill_local");

	static LocalOp allocateLocal(CodeDirs dirs, Local local, RefOp ref) {

		final ID id = local.getMemberId().toID();
		final HostTargetOp target = ref.path().target();
		final TargetStoreOp targetStore;
		final MemberLocal member = local.getMember();

		if (member == null) {

			final Code allocation = dirs.getAllocator().allocations();

			targetStore = target.allocateStore(id, allocation);
			targetStore.storeTarget(dirs);
		} else {
			targetStore = target.localStore(
					id,
					ds -> ds.getBuilder()
					.host()
					.local(ds, member.getMemberKey()));

			// Obtain target and store it inside nested allocator
			// to prevent target references outside the scope.
			final ID fillId = FILL_LOCAL_ID.detail(local.getMemberId());
			final CodeDirs fillDirs = dirs.sub(dirs.code().allocator(fillId));

			targetStore.storeTarget(fillDirs);

			fillDirs.done().code().go(dirs.code().tail());
		}

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

	public final TargetStoreOp getTargetStore() {
		return this.targetStore;
	}

	public final ValOp writeValue(ValDirs dirs) {
		return target(dirs.dirs()).value().writeValue(dirs);
	}

	public final void writeCond(CodeDirs dirs) {
		target(dirs).value().writeCond(dirs);
	}

	public final HostOp target(CodeDirs dirs) {
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
