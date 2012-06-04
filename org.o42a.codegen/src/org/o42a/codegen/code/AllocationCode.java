/*
    Compiler Code Generator
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.backend.AllocationWriter;
import org.o42a.codegen.code.op.AnyRecOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.code.op.StructRecOp;
import org.o42a.codegen.data.Type;


public final class AllocationCode extends Inset {

	public static final Disposal NO_DISPOSAL = new NoDisposal();

	private final AllocationWriter writer;
	private Disposal lastDisposal = NO_DISPOSAL;
	private Disposal disposal = NO_DISPOSAL;

	AllocationCode(Allocator allocator) {
		super(allocator, allocator.id().detail("alloc"));
		this.writer = allocator.writer().init(this);
	}

	public final void addDisposal(Disposal disposal) {
		assert disposal != null :
			"Disposal not specified";
		if (this.disposal == NO_DISPOSAL) {
			this.disposal = disposal;
		} else {
			this.disposal = new CombinedDisposal(this.disposal, disposal);
		}
	}

	public final void addLastDisposal(Disposal disposal) {
		assert disposal != null :
			"Disposal not specified";
		if (this.lastDisposal == NO_DISPOSAL) {
			this.lastDisposal = disposal;
		} else {
			this.lastDisposal =
					new CombinedDisposal(disposal, this.lastDisposal);
		}
	}

	public final AnyRecOp allocatePtr(CodeId id) {
		assert assertIncomplete();
		return writer().allocatePtr(opId(id));
	}

	public final AnyRecOp allocateNull(CodeId id) {

		final AnyRecOp result = allocatePtr(id);

		result.store(this, nullPtr());

		return result;
	}

	public <S extends StructOp<S>> S allocate(CodeId id, Type<S> type) {
		assert assertIncomplete();

		final S result = writer().allocateStruct(
				opId(id),
				type.data(getGenerator()).getPointer().getAllocation());

		result.allocated(this, null);

		return result;
	}

	public <S extends StructOp<S>> StructRecOp<S> allocatePtr(
			CodeId id,
			Type<S> type) {
		assert assertIncomplete();

		final StructRecOp<S> result = writer().allocatePtr(
				opId(id),
				type.data(getGenerator()).getPointer().getAllocation());

		result.allocated(this, null);

		return result;
	}

	@Override
	public final AllocationWriter writer() {
		return this.writer;
	}

	final void dispose(Block code) {
		this.disposal.dispose(code);
		this.lastDisposal.dispose(code);
		writer().dispose(code.writer());
	}

	private static final class NoDisposal implements Disposal {

		@Override
		public void dispose(Code code) {
		}

		@Override
		public String toString() {
			return "_";
		}

	}

	private static final class CombinedDisposal implements Disposal {

		private final Disposal first;
		private final Disposal second;

		CombinedDisposal(Disposal first, Disposal second) {
			this.first = first;
			this.second = second;
		}

		@Override
		public void dispose(Code code) {
			this.first.dispose(code);
			this.second.dispose(code);
		}

		@Override
		public String toString() {
			if (this.second == null) {
				return super.toString();
			}
			return this.first + ", " + this.second;
		}

	}

}
