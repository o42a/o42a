/*
    Compiler Core
    Copyright (C) 2010-2013 Ruslan Lopatin

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

import static org.o42a.core.ir.object.ObjectOp.anonymousObject;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;

import org.o42a.codegen.code.Allocator;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.AnyRecOp;
import org.o42a.codegen.code.op.StructRecOp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.TargetOp;
import org.o42a.util.string.ID;


public abstract class FldOp<F extends Fld.Op<F>> extends FldIROp {

	public FldOp(ObjOp host, Fld<F> fld) {
		super(host, fld);
	}

	public final boolean isOmitted() {
		return fld().isOmitted();
	}

	@Override
	public ID getId() {
		if (!isOmitted()) {
			return super.getId();
		}
		return fld().getId();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Fld<F> fld() {
		return (Fld<F>) super.fld();
	}

	@Override
	public abstract F ptr();

	@Override
	public FldStoreOp allocateStore(ID id, Code code) {
		if (fld().isOmitted()) {
			return new OmittedFldStoreOp(this);
		}

		final AnyRecOp hostPtr = code.allocatePtr(id.detail(HOST_ID));
		final StructRecOp<F> ptr = code.allocatePtr(id, fld().getType());

		return new RealFldStoreOp<>(
				this,
				code.getAllocator(),
				hostPtr,
				ptr);
	}

	private static final class OmittedFldStoreOp implements FldStoreOp {

		private final FldOp<?> fld;

		OmittedFldStoreOp(FldOp<?> fld) {
			this.fld = fld;
		}

		@Override
		public void storeTarget(CodeDirs dirs) {
		}

		@Override
		public ObjectOp loadObject(CodeDirs dirs) {
			throw new UnsupportedOperationException(this + " is omitted");
		}

		@Override
		public TargetOp loadTarget(CodeDirs dirs) {
			return this.fld;
		}

		@Override
		public String toString() {
			if (this.fld == null) {
				return super.toString();
			}
			return this.fld.toString();
		}

	}

	private static final class RealFldStoreOp<F extends Fld.Op<F>>
			implements FldStoreOp {

		private final FldOp<F> fld;
		private final Allocator allocator;
		private final AnyRecOp hostPtr;
		private final StructRecOp<F> ptr;

		RealFldStoreOp(
				FldOp<F> fld,
				Allocator allocator,
				AnyRecOp hostPtr,
				StructRecOp<F> ptr) {
			this.fld = fld;
			this.allocator = allocator;
			this.hostPtr = hostPtr;
			this.ptr = ptr;
		}

		@Override
		public void storeTarget(CodeDirs dirs) {

			final Block code = dirs.code();
			final ObjOp host = this.fld.host();

			tempObjHolder(this.allocator).holdVolatile(code, host);
			this.hostPtr.store(code, host.toAny(null, code));
			this.ptr.store(code, this.fld.ptr());
		}

		@Override
		public ObjectOp loadObject(CodeDirs dirs) {

			final Block code = dirs.code();

			return anonymousObject(
					dirs.getBuilder(),
					this.hostPtr.load(null, code).toData(null, code),
					this.fld.fld().getDeclaredIn());
		}

		@Override
		public TargetOp loadTarget(CodeDirs dirs) {

			final Block code = dirs.code();
			final F ptr = this.ptr.load(null, code);

			return this.fld.fld().op(code, this.fld.host(), ptr);
		}

		@Override
		public String toString() {
			if (this.ptr == null) {
				return super.toString();
			}
			return this.ptr.toString();
		}

	}

}
