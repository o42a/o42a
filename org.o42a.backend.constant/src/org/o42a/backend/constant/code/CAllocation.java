/*
    Constant Handler Compiler Back-end
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
package org.o42a.backend.constant.code;

import static org.o42a.backend.constant.data.ConstBackend.cast;
import static org.o42a.codegen.data.AllocClass.AUTO_ALLOC_CLASS;

import org.o42a.backend.constant.code.op.InstrBE;
import org.o42a.backend.constant.code.op.OpBE;
import org.o42a.backend.constant.code.rec.AnyRecCOp;
import org.o42a.backend.constant.code.rec.StructRecCOp;
import org.o42a.backend.constant.data.ContainerCDAlloc;
import org.o42a.backend.constant.data.struct.CStruct;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.AllocationCode;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.AllocationWriter;
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.AnyRecOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.code.op.StructRecOp;
import org.o42a.codegen.data.Type;
import org.o42a.codegen.data.backend.DataAllocation;


public final class CAllocation
		extends CInset<AllocationCode>
		implements AllocationWriter {

	CAllocation(CCode<?> enclosing, AllocationCode code) {
		super(enclosing, code);
	}

	@Override
	public final AnyRecCOp allocatePtr(CodeId id) {
		return new AnyRecCOp(
				new OpBE<AnyRecOp>(id, this) {
					@Override
					protected AnyRecOp write() {

						@SuppressWarnings("unchecked")
						final CCodePart<AllocationCode> part =
								(CCodePart<AllocationCode>) part();

						return part.underlying().writer().allocatePtr(
								getId());
					}
				},
				AUTO_ALLOC_CLASS);
	}

	@Override
	public final <S extends StructOp<S>> StructRecCOp<S> allocatePtr(
			CodeId id,
			DataAllocation<S> typeAllocation) {

		final ContainerCDAlloc<S> typeAlloc =
				(ContainerCDAlloc<S>) typeAllocation;

		return new StructRecCOp<S>(
				new OpBE<StructRecOp<S>>(id, this) {
					@Override
					protected StructRecOp<S> write() {

						@SuppressWarnings("unchecked")
						final CCodePart<AllocationCode> part =
								(CCodePart<AllocationCode>) part();

						return part.underlying().writer().allocatePtr(
								getId(),
								typeAlloc.getUnderlyingPtr().getAllocation());
					}
				},
				AUTO_ALLOC_CLASS,
				typeAlloc.getType());
	}

	@Override
	public final <S extends StructOp<S>> S allocateStruct(
			CodeId id,
			DataAllocation<S> typeAllocation) {

		final ContainerCDAlloc<S> typeAlloc =
				(ContainerCDAlloc<S>) typeAllocation;
		final Type<S> type = typeAlloc.getType();

		return type.op(new CStruct<S>(
				new OpBE<S>(id, this) {
					@Override
					protected S write() {

						@SuppressWarnings("unchecked")
						final CCodePart<AllocationCode> part =
								(CCodePart<AllocationCode>) part();

						return part.underlying().writer().allocateStruct(
								getId(),
								typeAlloc.getUnderlyingPtr().getAllocation());
					}
				},
				AUTO_ALLOC_CLASS,
				type));
	}

	@Override
	public void dispose(final CodeWriter writer) {

		final CInsetPart<AllocationCode> allocPart = nextPart();

		new InstrBE(cast(writer)) {
			@Override
			protected void emit() {
				if (!allocPart.hasOps()) {
					return;
				}
				allocPart.underlying().writer().dispose(
						part().underlying().writer());
			}
		};
	}

	@Override
	protected AllocationCode createUnderlying(Code enclosingUnderlying) {
		if (code().isDisposable()) {
			return enclosingUnderlying.allocate(getId().getLocal());
		}
		return enclosingUnderlying.undisposable(getId().getLocal());
	}

}
