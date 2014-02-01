/*
    Constant Handler Compiler Back-end
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.backend.constant.data;

import static org.o42a.backend.constant.data.ConstBackend.cast;

import org.o42a.backend.constant.code.op.OpBE;
import org.o42a.backend.constant.code.op.RelCOp;
import org.o42a.backend.constant.data.rec.RelRecCDAlloc;
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.RelOp;
import org.o42a.codegen.code.op.RelRecOp;
import org.o42a.codegen.data.RelPtr;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.DataWriter;
import org.o42a.codegen.data.backend.RelAllocation;
import org.o42a.util.string.ID;


public final class RelCDAlloc implements RelAllocation {

	private final RelPtr pointer;
	private final CDAlloc<?> relativeTo;
	private final CDAlloc<?> target;
	private RelPtr underlying;

	public RelCDAlloc(
			RelPtr pointer,
			CDAlloc<?> relativeTo,
			CDAlloc<?> target) {
		this.pointer = pointer;
		this.relativeTo = relativeTo;
		this.target = target;
	}

	public final ConstBackend getBackend() {
		return this.target.getBackend();
	}

	public final RelPtr getPointer() {
		return this.pointer;
	}

	public final CDAlloc<?> getRelativeTo() {
		return this.relativeTo;
	}

	public final CDAlloc<?> getTarget() {
		return this.target;
	}

	public RelPtr getUnderlying() {
		if (this.underlying != null) {
			return this.underlying;
		}
		return this.underlying = this.target.getUnderlyingPtr().relativeTo(
				this.relativeTo.getUnderlyingPtr());
	}

	@Override
	public void write(
			DataWriter writer,
			DataAllocation<RelRecOp> destination) {

		final RelRecCDAlloc dest = (RelRecCDAlloc) destination;

		dest.setValue(getPointer());
	}

	@Override
	public RelOp op(ID id, CodeWriter writer) {
		return new RelCOp(
				new OpBE<RelOp>(id, cast(writer)) {
					@Override
					public void prepare() {
					}
					@Override
					protected RelOp write() {
						return getUnderlying().op(
								getId(),
								part().underlying());
					}
				},
				getPointer());
	}

	@Override
	public String toString() {
		if (this.pointer == null) {
			return super.toString();
		}
		return this.pointer.toString();
	}

}
