/*
    Compiler Code Generator
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
package org.o42a.codegen.code.op;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.data.AllocPlace;
import org.o42a.util.string.ID;


public final class DumpPtrOp implements DumpablePtrOp<DumpPtrOp> {

	public static DumpPtrOp dumpPtrOp(OpMeans<? extends DumpablePtrOp<?>> ptr) {
		return new DumpPtrOp(ptr);
	}

	private final OpMeans<? extends DumpablePtrOp<?>> ptr;

	DumpPtrOp(OpMeans<? extends DumpablePtrOp<?>> ptr) {
		this.ptr = ptr;
	}

	@Override
	public ID getId() {
		return this.ptr.getId();
	}

	public final DumpablePtrOp<?> ptr() {
		return this.ptr.op();
	}

	@Override
	public AllocPlace getAllocPlace() {
		return ptr().getAllocPlace();
	}

	@Override
	public BoolOp isNull(ID id, Code code) {
		return ptr().isNull(id, code);
	}

	@Override
	public BoolOp eq(ID id, Code code, DumpPtrOp other) {
		return eq(id, code, ptr(), other.ptr());
	}

	@Override
	public BoolOp ne(ID id, Code code, DumpPtrOp other) {
		return ne(id, code, this, other);
	}

	@Override
	public void returnValue(Block code, boolean dispose) {
		ptr().returnValue(code, dispose);
	}

	@Override
	public DumpPtrOp offset(ID id, Code code, IntOp<?> index) {

		final DumpablePtrOp<?> ptr = ptr();
		final DumpablePtrOp<?> result = ptr.offset(id, code, index);

		if (result == ptr) {
			return this;
		}

		return new DumpPtrOp(result);
	}

	@Override
	public AnyOp toAny(ID id, Code code) {
		return ptr().toAny(id, code);
	}

	@Override
	public DataOp toData(ID id, Code code) {
		return ptr().toData(id, code);
	}

	@Override
	public String toString() {
		if (this.ptr == null) {
			return super.toString();
		}
		return getId().toString();
	}

	@SuppressWarnings("unchecked")
	private static <P extends DumpablePtrOp<P>> BoolOp eq(
			ID id,
			Code code,
			DumpablePtrOp<?> p1,
			DumpablePtrOp<?> p2) {
		return ((DumpablePtrOp<P>) p1).eq(id, code, (P) p2);
	}

	@SuppressWarnings("unchecked")
	private static <P extends DumpablePtrOp<P>> BoolOp ne(
			ID id,
			Code code,
			DumpablePtrOp<?> p1,
			DumpablePtrOp<?> p2) {
		return ((DumpablePtrOp<P>) p1).ne(id, code, (P) p2);
	}

}
