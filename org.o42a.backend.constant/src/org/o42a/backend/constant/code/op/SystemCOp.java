/*
    Constant Handler Compiler Back-end
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.backend.constant.code.op;

import static org.o42a.backend.constant.code.op.SystemStore.allocSystemStore;

import org.o42a.analysis.use.SimpleUsage;
import org.o42a.analysis.use.Usable;
import org.o42a.backend.constant.data.struct.CSystemType;
import org.o42a.codegen.code.op.SystemOp;
import org.o42a.codegen.data.Ptr;
import org.o42a.codegen.data.SystemType;


public class SystemCOp extends AllocPtrCOp<SystemOp> implements SystemOp {

	private final CSystemType underlyingType;
	private final SystemStore store;
	private final Usable<SimpleUsage> explicitUses;

	public SystemCOp(
			OpBE<SystemOp> backend,
			SystemStore store,
			CSystemType underlyingType) {
		this(backend, store, underlyingType, null);
	}

	public SystemCOp(
			OpBE<SystemOp> backend,
			SystemStore store,
			CSystemType underlyingType,
			Ptr<SystemOp> constant) {
		super(backend, store != null ? store.getAllocPlace() : null, constant);
		this.underlyingType = underlyingType;
		this.store = store != null ? store : allocSystemStore(getAllocPlace());
		this.explicitUses = this.store.init(this, allUses());
	}

	public final CSystemType getUnderlyingType() {
		return this.underlyingType;
	}

	@Override
	public final SystemType getSystemType() {
		return this.underlyingType.getOriginal();
	}

	public final SystemStore store() {
		return this.store;
	}

	@Override
	public SystemCOp create(OpBE<SystemOp> backend, Ptr<SystemOp> constant) {
		return new SystemCOp(backend, null, getUnderlyingType(), constant);
	}

	@Override
	protected final Usable<SimpleUsage> explicitUses() {
		return this.explicitUses;
	}

}
