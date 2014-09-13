/*
    Compiler Code Generator
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
package org.o42a.codegen.data;

import static org.o42a.codegen.data.AllocClass.CONSTANT_ALLOC_CLASS;
import static org.o42a.codegen.data.AllocClass.STATIC_ALLOC_CLASS;
import static org.o42a.util.fn.Init.init;

import java.util.function.Supplier;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.PtrOp;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.util.fn.Init;
import org.o42a.util.string.ID;


public abstract class Ptr<P extends PtrOp<P>>
		extends AbstractPtr
		implements Supplier<Ptr<P>> {

	private final Init<DataAllocation<P>> allocation =
			init(this::createAllocation);
	private DataAllocation<P> protoAllocation;

	protected Ptr(ID id, boolean ptrToConstant, boolean isNull) {
		super(id, ptrToConstant, isNull);
	}

	@Override
	public final Ptr<P> get() {
		return this;
	}

	public final DataAllocation<P> getAllocation() {
		return this.allocation.get();
	}

	public final DataAllocation<P> getProtoAllocation() {
		return this.protoAllocation;
	}

	public final RelPtr relativeTo(Ptr<?> ptr) {
		return new RelPtr(this, ptr);
	}

	public Ptr<DataOp> toData() {

		final ID id = getId().type("data");

		return new Ptr<DataOp>(id, isPtrToConstant(), isNull()) {
			@Override
			public Ptr<DataOp> toData() {
				return this;
			}
			@Override
			protected DataAllocation<DataOp> createAllocation() {
				return Ptr.this.getAllocation().toData();
			}
		};
	}

	public final P op(ID id, Code code) {

		final CodeBase c = code;

		c.assertIncomplete();

		return getAllocation().op(
				id != null ? code.opId(id) : getId(),
				isPtrToConstant() ? CONSTANT_ALLOC_CLASS : STATIC_ALLOC_CLASS,
				c.writer());
	}

	protected abstract DataAllocation<P> createAllocation();

	@Override
	protected DataAllocation<AnyOp> allocationToAny() {
		return getAllocation().toAny();
	}

	void setAllocation(DataAllocation<P> allocation) {
		assert !this.allocation.isInitialized() :
			"Allocation already present";
		this.allocation.set(allocation);
	}

	final void setProtoAllocation(DataAllocation<P> allocation) {
		assert this.protoAllocation == null :
			"Prototype allocation already present";
		this.protoAllocation = allocation;
	}

	@SuppressWarnings("unchecked")
	final void copyAllocation(Data<?> proto) {
		this.protoAllocation = (DataAllocation<P>) proto.getAllocation();
	}

}
