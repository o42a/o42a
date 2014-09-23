/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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

import java.util.function.Function;


public class IndirectTargetStoreOp<S extends TargetStoreOp>
		implements TargetStoreOp {

	private final Function<CodeDirs, S> getStore;
	private S store;

	public IndirectTargetStoreOp(Function<CodeDirs, S> getStore) {
		this.getStore = getStore;
	}

	@Override
	public void storeTarget(CodeDirs dirs) {
		this.store = this.getStore.apply(dirs);
		this.store.storeTarget(dirs);
	}

	@Override
	public TargetOp loadTarget(CodeDirs dirs) {
		return this.store.loadTarget(dirs);
	}

	protected final S store() {
		return this.store;
	}

}
