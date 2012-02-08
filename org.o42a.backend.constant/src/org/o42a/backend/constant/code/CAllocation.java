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

import static org.o42a.backend.constant.data.ConstBackend.underlying;
import static org.o42a.codegen.data.AllocClass.AUTO_ALLOC_CLASS;

import org.o42a.backend.constant.code.op.OpBE;
import org.o42a.backend.constant.code.rec.AnyRecCOp;
import org.o42a.backend.constant.code.rec.StructRecCOp;
import org.o42a.backend.constant.data.ContainerCDAlloc;
import org.o42a.backend.constant.data.struct.CStruct;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.AllocationCode;
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

	CAllocation(
			CCode<?> enclosing,
			AllocationCode code,
			AllocationCode underlying) {
		super(enclosing, code, underlying);
	}

	@Override
	public final AnyRecCOp allocatePtr(CodeId id) {
		return new AnyRecCOp(
				new OpBE<AnyRecOp>(id, this) {
					@Override
					protected AnyRecOp write() {
						return getUnderlying().writer().allocatePtr(getId());
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
						return getUnderlying().writer().allocatePtr(
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
						return getUnderlying().writer().allocateStruct(
								getId(),
								typeAlloc.getUnderlyingPtr().getAllocation());
					}
				},
				AUTO_ALLOC_CLASS,
				type));
	}

	@Override
	public void dispose(CodeWriter writer) {
		getUnderlying().writer().dispose(underlying(writer));
	}

}
