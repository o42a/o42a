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
package org.o42a.codegen.code;

import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.AnyRecOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.code.op.StructRecOp;
import org.o42a.codegen.data.Type;
import org.o42a.util.string.ID;


public final class Allocations {

	private static final ID ALLOC_SUFFIX = ID.rawId("__alloc__");

	private final Code code;
	private final Allocated<?> allocated;

	Allocations(Code enclosing, Allocated<?> allocated) {
		this.code = enclosing.inset(
				allocated.getId().getLocal().detail(ALLOC_SUFFIX));
		this.allocated = allocated;
	}

	public final Allocated<?> getAllocated() {
		return this.allocated;
	}

	public final Block getBlock() {
		return code().getBlock();
	}

	public final Allocator getAllocator() {
		return code().getAllocator();
	}

	public final AnyRecOp allocatePtr() {
		return allocatePtr(getAllocated().getId());
	}

	public final AnyRecOp allocatePtr(ID id) {
		assert assertIncomplete();
		return writer().allocatePtr(code().opId(id));
	}

	public final <S extends StructOp<S>> StructRecOp<S> allocatePtr(
			Type<S> type) {
		return allocatePtr(getAllocated().getId(), type);
	}

	public final <S extends StructOp<S>> StructRecOp<S> allocatePtr(
			ID id,
			Type<S> type) {
		assert assertIncomplete();

		final StructRecOp<S> result = writer().allocatePtr(
				code().opId(id),
				type.data(code().getGenerator()).getPointer().getAllocation());

		result.allocated(code(), null);

		return result;
	}

	public final <S extends StructOp<S>> S allocate(Type<S> type) {
		return allocate(getAllocated().getId(), type);
	}

	public final <S extends StructOp<S>> S allocate(ID id, Type<S> type) {
		assert assertIncomplete();

		final S result = writer().allocateStruct(
				code().opId(id),
				type.data(code().getGenerator()).getPointer().getAllocation());

		result.allocated(code(), null);

		return result;
	}

	final Code code() {
		return this.code;
	}

	private final boolean assertIncomplete() {
		return code().assertIncomplete();
	}

	private final CodeWriter writer() {
		return code().writer();
	}

}
