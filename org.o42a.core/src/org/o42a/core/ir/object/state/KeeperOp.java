/*
    Compiler Core
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
package org.o42a.core.ir.object.state;

import org.o42a.core.ir.field.FldIROp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.object.state.Keeper;
import org.o42a.util.string.ID;


public final class KeeperOp<O extends KeeperIROp<O>>
		extends FldIROp
		implements HostValueOp {

	public static final ID KEEPER_ID = ID.id("keeper");

	private final KeeperIROp<O> ptr;

	KeeperOp(ObjOp host, KeeperIR<O, ?> keeperIR, KeeperIROp<O> ptr) {
		super(host, keeperIR);
		this.ptr = ptr;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final KeeperIR<O, ?> fld() {
		return (KeeperIR<O, ?>) super.fld();
	}

	public final Keeper getKeeper() {
		return keeperIR().getKeeper();
	}

	@SuppressWarnings("unchecked")
	public final KeeperIR<O, ?> keeperIR() {
		return (KeeperIR<O, ?>) super.fld();
	}

	@Override
	public final KeeperIROp<O> ptr() {
		return this.ptr;
	}

	@Override
	public final HostValueOp value() {
		return this;
	}

	@Override
	public final void writeCond(CodeDirs dirs) {
		ptr().writeCond(this, dirs);
	}

	@Override
	public final ValOp writeValue(ValDirs dirs) {
		return ptr().writeValue(this, dirs);
	}

	@Override
	public final void assign(CodeDirs dirs, HostOp value) {
		throw new UnsupportedOperationException("Can not write to keeper");
	}

	@Override
	public final TargetOp field(CodeDirs dirs, MemberKey memberKey) {
		throw new UnsupportedOperationException("Keepers have no fields");
	}

	@Override
	public final ObjectOp materialize(CodeDirs dirs, ObjHolder holder) {
		throw new UnsupportedOperationException(
				"Keepers can not be materialized");
	}

	@Override
	public final TargetOp dereference(CodeDirs dirs, ObjHolder holder) {
		return ptr().dereference(this, dirs, holder);
	}

	@Override
	public String toString() {
		return "KeeperOp[" + getKeeper() + '@' + host() + ']';
	}

}
