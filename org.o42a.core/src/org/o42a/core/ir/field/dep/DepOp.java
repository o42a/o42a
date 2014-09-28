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
package org.o42a.core.ir.field.dep;

import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;

import java.util.function.Function;

import org.o42a.codegen.code.Allocator;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.DumpablePtrOp;
import org.o42a.core.ir.field.ByOwnerStoreOp;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.field.local.LocalIROp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.object.Obj;
import org.o42a.core.object.state.Dep;
import org.o42a.util.string.ID;


public class DepOp extends DefiniteIROp implements TargetOp, HostValueOp {

	public static final ID DEP_ID = ID.id("dep");

	private final ObjectOp host;
	private final DepIR depIR;
	private final DumpablePtrOp<?> ptr;
	private final DepIR.Op op;
	private final RefIROp ref;

	DepOp(Code code, ObjectOp host, DepIR depIR) {
		super(host.getBuilder());
		this.host = host;
		this.depIR = depIR;
		if (depIR.isOmitted()) {
			this.ptr = host.ptr(code);
			this.op = null;
		} else {
			this.ptr = this.op =
					host.ptr(code).field(code, depIR.getTypeInstance());
		}
		this.ref = depIR.refIR().op(code, this);
	}

	@Override
	public DumpablePtrOp<?> ptr() {
		return this.ptr;
	}

	public final DepIR.Op op() {
		assert this.op != null :
			this + " is omitted";
		return this.op;
	}

	@Override
	public ID getId() {
		return ptr().getId();
	}

	public final Dep getDep() {
		return depIR().getDep();
	}

	public final ObjectOp host() {
		return this.host;
	}

	public final DepIR depIR() {
		return this.depIR;
	}

	@Override
	public final HostValueOp value() {
		return this;
	}

	@Override
	public void writeCond(CodeDirs dirs) {
		loadDep(dirs).value().writeCond(dirs);
	}

	@Override
	public ValOp writeValue(ValDirs dirs) {
		return loadDep(dirs.dirs()).value().writeValue(dirs);
	}

	@Override
	public FldOp<?, ?> field(CodeDirs dirs, MemberKey memberKey) {
		return loadDep(dirs).field(dirs, memberKey);
	}

	@Override
	public ObjectOp materialize(CodeDirs dirs, ObjHolder holder) {
		return loadDep(dirs).materialize(dirs, holder);
	}

	@Override
	public ObjectOp dereference(CodeDirs dirs, ObjHolder holder) {
		return loadDep(dirs).dereference(dirs, holder);
	}

	@Override
	public TargetStoreOp allocateStore(ID id, Code code) {
		return new DepByOwnerStoreOp(id, code, this);
	}

	@Override
	public TargetStoreOp localStore(
			ID id,
			Function<CodeDirs, LocalIROp> getLocal) {
		return new DepByOwnerStoreOp(id, getLocal, this);
	}

	@Override
	public void assign(CodeDirs dirs, HostOp value) {
		throw new UnsupportedOperationException();
	}

	public void fill(CodeDirs dirs, HostOp host) {
		if (depIR().isOmitted()) {
			return;
		}

		final Block code = dirs.code();

		this.ref.storeTarget(dirs, host);
		code.debug("Depends on " + getDep().ref());
		code.dump(getDep() + " := ", this);
	}

	@Override
	public String toString() {
		return "DepOp[" + getDep() + '@' + host() + ']';
	}

	private TargetOp loadDep(CodeDirs dirs) {
		return this.ref.loadTarget(dirs);
	}

	private static final class DepByOwnerStoreOp extends ByOwnerStoreOp {

		private final DepOp dep;

		DepByOwnerStoreOp(ID id, Code code, DepOp dep) {
			super(id, code);
			this.dep = dep;
		}

		DepByOwnerStoreOp(
				ID id,
				Function<CodeDirs, LocalIROp> getLocal,
				DepOp dep) {
			super(id, getLocal);
			this.dep = dep;
		}

		@Override
		public Obj getOwnerType() {
			return this.dep.depIR().getBodyIR().getObjectIR().getObject();
		}

		@Override
		protected ObjectOp owner(CodeDirs dirs, Allocator allocator) {

			final ObjectOp owner = this.dep.host();

			if (allocator != null) {
				// Ensure the owner not deallocated until the local exists.
				tempObjHolder(allocator).holdVolatile(dirs.code(), owner);
			}

			return owner;
		}

		@Override
		protected TargetOp op(CodeDirs dirs, ObjOp owner) {
			return this.dep.depIR().op(dirs.code(), owner);
		}

	}

}