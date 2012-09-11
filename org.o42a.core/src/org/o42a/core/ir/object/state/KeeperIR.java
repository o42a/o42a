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
package org.o42a.core.ir.object.state;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.data.Data;
import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.field.FldIR;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIRBody;
import org.o42a.core.object.Obj;
import org.o42a.core.object.state.Keeper;
import org.o42a.util.string.ID;


public abstract class KeeperIR<
		O extends KeeperIROp<O>,
		T extends KeeperIRType<O>>
				implements FldIR {

	private final ObjectIRBody bodyIR;
	private final Keeper keeper;
	private T instance;

	public KeeperIR(ObjectIRBody bodyIR, Keeper keeper) {
		this.bodyIR = bodyIR;
		this.keeper = keeper;
	}

	public final Generator getGenerator() {
		return getBodyIR().getGenerator();
	}

	@Override
	public final ObjectIRBody getBodyIR() {
		return this.bodyIR;
	}

	public final Keeper getKeeper() {
		return this.keeper;
	}

	@Override
	public final ID getId() {
		return getKeeper().toID();
	}

	@Override
	public final Obj getDeclaredIn() {
		return getKeeper().getDeclaredIn();
	}

	public final T getInstance() {
		return this.instance;
	}

	@Override
	public final Data<?> data(Generator generator) {
		return getInstance().data(generator);
	}

	public final void allocate(SubData<?> data) {
		this.instance = allocateKeeper(data);
	}

	public final KeeperOp op(Code code, ObjOp host) {
		assert getInstance() != null :
			this + " is not allocated yet";
		return new KeeperOp(
				host,
				this,
				host.ptr().keeper(code, getInstance()));
	}

	@Override
	public String toString() {
		if (this.keeper == null) {
			return super.toString();
		}
		return this.keeper.toString();
	}

	protected abstract T allocateKeeper(SubData<?> data);

}
