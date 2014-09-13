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
import org.o42a.codegen.code.op.AnyRecOp;
import org.o42a.codegen.code.op.StructRecOp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.TargetOp;
import org.o42a.util.string.ID;


public abstract class FldOp<F extends Fld.Op<F>, T extends Fld.Type<F>>
		extends FldIROp<F, T> {

	public FldOp(ObjOp host, Fld<F, T> fld) {
		super(host, fld);
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
	public abstract F ptr();

	@Override
	public FldStoreOp allocateStore(ID id, Code code) {
		if (fld().isOmitted()) {
			return new OmittedFldStoreOp(this);
		}

		final Allocated<FldPtrs<F>> ptrs =
				code.allocate(id, new AllocatableFldPtrs<>(this));

		return new RealFldStoreOp<>(this, code.getAllocator(), ptrs);
	}

	private static final class OmittedFldStoreOp implements FldStoreOp {

		private final FldOp<?, ?> fld;

		OmittedFldStoreOp(FldOp<?, ?> fld) {
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

	private static final class FldPtrs<F extends Fld.Op<F>> {

		private final AnyRecOp host;
		private final StructRecOp<F> ptr;

		FldPtrs(AnyRecOp host, StructRecOp<F> ptr) {
			this.host = host;
			this.ptr = ptr;
		}

	}

	private static final class AllocatableFldPtrs<
			F extends Fld.Op<F>,
			T extends Fld.Type<F>>
					implements Allocatable<FldPtrs<F>> {

		private final FldOp<F, T> fld;

		AllocatableFldPtrs(FldOp<F, T> fld) {
			this.fld = fld;
		}

		@Override
		public AllocationMode getAllocationMode() {
			return ALLOCATOR_ALLOCATION;
		}

		@Override
		public FldPtrs<F> allocate(
				Allocations code,
				Allocated<FldPtrs<F>> allocated) {
			return new FldPtrs<>(
					code.allocatePtr(HOST_ID),
					this.fld.isStateless()
					? null : code.allocatePtr(this.fld.fld().getType()));
		}

		@Override
		public void init(Code code, FldPtrs<F> allocated) {
		}

		@Override
		public void dispose(Code code, Allocated<FldPtrs<F>> allocated) {
		}

	}

	private static final class RealFldStoreOp<
			F extends Fld.Op<F>,
			T extends Fld.Type<F>>
					implements FldStoreOp {

		private final FldOp<F, T> fld;
		private final Allocator allocator;
		private final Allocated<FldPtrs<F>> ptrs;

		RealFldStoreOp(
				FldOp<F, T> fld,
				Allocator allocator,
				Allocated<FldPtrs<F>> ptrs) {
			this.fld = fld;
			this.allocator = allocator;
			this.ptrs = ptrs;
		}

		@Override
		public void storeTarget(CodeDirs dirs) {

			final Block code = dirs.code();
			final ObjOp host = this.fld.host();

			tempObjHolder(this.allocator).holdVolatile(code, host);

			final FldPtrs<F> ptrs = this.ptrs.get(code);

			ptrs.host.store(code, host.toAny(null, code));
			if (!this.fld.isStateless()) {
				ptrs.ptr.store(code, this.fld.ptr());
			}
		}

		@Override
		public ObjectOp loadObject(CodeDirs dirs) {

			final Block code = dirs.code();

			return anonymousObject(
					dirs,
					this.ptrs.get(code)
					.host
					.load(null, code)
					.toData(null, code),
					this.fld.fld().getDeclaredIn());
		}

		@Override
		public TargetOp loadTarget(CodeDirs dirs) {

			final Block code = dirs.code();
			final F ptr;

			if (this.fld.isStateless()) {
				ptr = null;
			} else {
				ptr = this.ptrs.get(code).ptr.load(null, code);
			}

			return this.fld.fld().op(code, this.fld.host(), ptr);
		}

		@Override
		public String toString() {
			if (this.ptrs == null) {
				return super.toString();
			}
			return this.ptrs.toString();
		}

	}

}
