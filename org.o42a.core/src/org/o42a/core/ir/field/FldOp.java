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

import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;

import java.util.function.Function;

import org.o42a.codegen.code.Allocator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.OpMeans;
import org.o42a.core.ir.field.local.LocalIROp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.Obj;
import org.o42a.util.string.ID;


public abstract class FldOp<F extends Fld.Op<F>, T extends Fld.Type<F>>
		extends FldIROp<F, T>
		implements TargetOp {

	public FldOp(ObjOp host, Fld<F, T> fld, OpMeans<F> ptr) {
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
	public final FldStoreOp allocateStore(ID id, Code code) {
		return new FldByOwnerStoreOp<>(id, code, this);
	}

	@Override
	public final FldStoreOp localStore(
			ID id,
			Function<CodeDirs, LocalIROp> getLocal) {
		return new FldByOwnerStoreOp<>(id, getLocal, this);
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

	private static final class FldByOwnerStoreOp<
			F extends Fld.Op<F>,
			T extends Fld.Type<F>>
					extends ByOwnerStoreOp {

		private final FldOp<F, T> fld;

		FldByOwnerStoreOp(ID id, Code code, FldOp<F, T> fld) {
			super(id, code);
			this.fld = fld;
		}

		FldByOwnerStoreOp(
				ID id,
				Function<CodeDirs, LocalIROp> getLocal,
				FldOp<F, T> fld) {
			super(id, getLocal);
			this.fld = fld;
		}

		@Override
		public Obj getOwnerType() {
			return this.fld.fld().getBodyIR().getObjectIR().getObject();
		}

		@Override
		protected ObjectOp owner(CodeDirs dirs, Allocator allocator) {

			final ObjOp owner = this.fld.host();

			if (allocator != null) {
				// Ensure the owner not deallocated until the local exists.
				tempObjHolder(allocator).holdVolatile(dirs.code(), owner);
			}

			return owner;
		}

		@Override
		protected TargetOp op(CodeDirs dirs, ObjOp owner) {
			return this.fld.fld().op(dirs.code(), owner);
		}

	}

}
