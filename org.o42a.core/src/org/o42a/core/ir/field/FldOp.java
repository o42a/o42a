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
package org.o42a.core.ir.field;

import static org.o42a.codegen.code.AllocationMode.ALLOCATOR_ALLOCATION;
import static org.o42a.core.ir.object.ObjectOp.anonymousObject;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.op.DataRecOp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ValOp;
import org.o42a.util.string.ID;


public abstract class FldOp<F extends Fld.Op<F>, T extends Fld.Type<F>>
		extends FldIROp<F, T>
		implements TargetOp {

	private static final AllocatableOwnerPtr ALLOCATABLE_OWNER_PTR =
			new AllocatableOwnerPtr();

	public FldOp(ObjOp host, Fld<F, T> fld, F ptr) {
		super(host, fld, ptr);
	}

	public final boolean isOmitted() {
		return fld().isOmitted();
	}

	public final boolean isStateless() {
		return fld().isStateless();
	}

	@Override
	public ID getId() {
		if (!isOmitted()) {
			return super.getId();
		}
		return fld().getId();
	}

	@Override
	public Fld<F, T> fld() {
		return (Fld<F, T>) super.fld();
	}

	@Override
	public final HostTargetOp target() {
		return this;
	}

	@Override
	public final TargetOp op(CodeDirs dirs) {
		return this;
	}

	@Override
	public FldStoreOp allocateStore(ID id, Code code) {

		final Allocated<DataRecOp> ownerPtr =
				code.allocate(id, ALLOCATABLE_OWNER_PTR);

		return new FldByOwnerStoreOp<>(this, code.getAllocator(), ownerPtr);
	}

	protected final HostValueOp objectFldValueOp() {
		return new ObjectFldValueOp(this);
	}

	private static final class ObjectFldValueOp implements HostValueOp {

		private final FldOp<?, ?> fld;

		ObjectFldValueOp(FldOp<?, ?> fld) {
			this.fld = fld;
		}

		@Override
		public void writeCond(CodeDirs dirs) {
			object(dirs).value().writeCond(dirs);
		}

		@Override
		public ValOp writeValue(ValDirs dirs) {
			return object(dirs.dirs()).value().writeValue(dirs);
		}

		@Override
		public void assign(CodeDirs dirs, HostOp value) {
			object(dirs).value().assign(dirs, value);
		}

		@Override
		public String toString() {
			if (this.fld == null) {
				return super.toString();
			}
			return this.fld.toString();
		}

		private final ObjectOp object(CodeDirs dirs) {
			return this.fld.materialize(
					dirs,
					tempObjHolder(dirs.getAllocator()));
		}

	}

	private static final class AllocatableOwnerPtr
			implements Allocatable<DataRecOp> {

		@Override
		public AllocationMode getAllocationMode() {
			return ALLOCATOR_ALLOCATION;
		}

		@Override
		public DataRecOp allocate(
				Allocations code,
				Allocated<DataRecOp> allocated) {
			return code.allocateDataPtr(HOST_ID);
		}

		@Override
		public void init(Code code, DataRecOp allocated) {
		}

		@Override
		public void dispose(Code code, Allocated<DataRecOp> allocated) {
		}

	}

	private static final class FldByOwnerStoreOp<
			F extends Fld.Op<F>,
			T extends Fld.Type<F>>
					implements FldStoreOp {

		private final FldOp<F, T> fld;
		private final Allocator allocator;
		private final Allocated<DataRecOp> ownerPtr;

		FldByOwnerStoreOp(
				FldOp<F, T> fld,
				Allocator allocator,
				Allocated<DataRecOp> ownerPtr) {
			this.fld = fld;
			this.allocator = allocator;
			this.ownerPtr = ownerPtr;
		}

		@Override
		public void storeTarget(CodeDirs dirs) {

			final Block code = dirs.code();
			final ObjOp host = this.fld.host();

			tempObjHolder(this.allocator).holdVolatile(code, host);

			this.ownerPtr.get(code).store(code, host.toData(null, code));
		}

		@Override
		public ObjectOp loadOwner(CodeDirs dirs) {

			final Block code = dirs.code();

			return anonymousObject(
					dirs,
					this.ownerPtr.get(code).load(null, code),
					this.fld.fld().getBodyIR().getObjectIR().getObject());
		}

		@Override
		public TargetOp loadTarget(CodeDirs dirs) {

			final ObjOp host = loadOwner(dirs).castToWellKnown(null, dirs);

			return this.fld.fld().op(dirs.code(), host);
		}

		@Override
		public String toString() {
			if (this.ownerPtr == null) {
				return super.toString();
			}
			return this.ownerPtr.toString();
		}

	}

}
