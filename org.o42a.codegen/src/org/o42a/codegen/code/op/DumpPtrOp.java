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

import java.util.Objects;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.data.AllocPlace;
import org.o42a.util.string.ID;


public final class DumpPtrOp implements DumpablePtrOp<DumpPtrOp> {

	public static DumpPtrOp dumpPtrOp(DumpablePtrOp<?> ptr) {
		return new DumpPtrOp(ptr);
	}

	private final DumpablePtrOp<?> ptr;

	DumpPtrOp(DumpablePtrOp<?> ptr) {
		this.ptr = ptr;
	}

	@Override
	public ID getId() {
		return this.ptr.getId();
	}

	@Override
	public AllocPlace getAllocPlace() {
		return this.ptr.getAllocPlace();
	}

	@Override
	public BoolOp isNull(ID id, Code code) {
		return this.ptr.isNull(id, code);
	}

	@Override
	public BoolOp eq(ID id, Code code, DumpPtrOp other) {
		return eq(id, code, this.ptr, other.ptr);
	}

	@Override
	public BoolOp ne(ID id, Code code, DumpPtrOp other) {
		return ne(id, code, this, other);
	}

	@Override
	public void returnValue(Block code, boolean dispose) {
		this.ptr.returnValue(code, dispose);
	}

	@Override
	public DumpPtrOp offset(ID id, Code code, IntOp<?> index) {

		final DumpablePtrOp<?> result = this.ptr.offset(id, code, index);

		if (result == this.ptr) {
			return this;
		}

		return new DumpPtrOp(result);
	}

	@Override
	public AnyOp toAny(ID id, Code code) {
		return this.ptr.toAny(id, code);
	}

	@Override
	public DataOp toData(ID id, Code code) {
		return this.ptr.toData(id, code);
	}

	@Override
	public String toString() {
		return Objects.toString(this.ptr);
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
