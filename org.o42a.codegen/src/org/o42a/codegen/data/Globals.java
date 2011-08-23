/*
    Compiler Code Generator
    Copyright (C) 2010,2011 Ruslan Lopatin

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

import java.util.LinkedList;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;


public abstract class Globals {

	private final Generator generator;

	private final DataChain globals = new DataChain();
	private int typesAllocating;
	private final LinkedList<AbstractTypeData<?>> scheduled =
			new LinkedList<AbstractTypeData<?>>();

	private Ptr<AnyOp> nullPtr;
	private Ptr<DataOp> nullDataPtr;

	public Globals(Generator generator) {
		this.generator = generator;
	}

	public final Generator getGenerator() {
		return this.generator;
	}

	public final Ptr<AnyOp> nullPtr() {
		if (this.nullPtr != null) {
			return this.nullPtr;
		}
		return this.nullPtr = new Ptr<AnyOp>(
				getGenerator().id("null"),
				getGenerator().getGlobals().dataWriter().nullPtr(),
				true);
	}

	public final Ptr<DataOp> nullDataPtr() {
		if (this.nullDataPtr != null) {
			return this.nullDataPtr;
		}
		return this.nullDataPtr = new Ptr<DataOp>(
				getGenerator().id("null"),
				getGenerator().getGlobals().dataWriter().nullDataPtr(),
				true);
	}

	public final <S extends StructOp<S>> Ptr<S> nullPtr(Type<S> type) {
		return new Ptr<S>(
				type.getId().detail("null"),
				getGenerator().getGlobals().dataWriter().nullPtr(type),
				true);
	}

	public final <
			S extends StructOp<S>,
			T extends Type<S>> Allocated<S, T> allocateType(
					T type) {

		final SubData<S> data = type.createTypeData(getGenerator());

		data.startAllocation(dataAllocator());

		return new Allocated<S, T>(type, type, type.getInstanceData());
	}

	public final GlobalSettings newGlobal() {
		return new GlobalSettings(this);
	}

	public final Ptr<AnyOp> addBinary(
			CodeId id,
			boolean isConstant,
			byte[] data) {
		return addBinary(id, isConstant, data, 0, data.length);
	}

	public Ptr<AnyOp> addBinary(
			CodeId id,
			boolean isConstant,
			byte[] data,
			int start,
			int end) {
		return new Ptr<AnyOp>(
				id,
				dataAllocator().addBinary(id, isConstant, data, start, end),
				isConstant);
	}

	public boolean write() {

		final DataWriter writer = dataWriter();
		Data<?> global = this.globals.getFirst();

		if (global == null) {
			return false;
		}
		do {
			global.write(writer);
			global = global.getNext();
		} while (global != null);

		this.globals.empty();

		return true;
	}

	protected abstract DataAllocator dataAllocator();

	protected abstract DataWriter dataWriter();

	protected abstract void registerType(SubData<?> type);

	<S extends StructOp<S>, T extends Type<S>> Global<S, T> addGlobal(
			GlobalSettings settings,
			CodeId id,
			T type,
			T instance,
			Content<T> content) {

		final Global<S, T> global =
				new Global<S, T>(settings, id, type, instance, content);
		final SubData<S> data = global.getInstance().getInstanceData();

		data.allocateType(false);
		globalAllocated(data);

		return global;
	}

	<S extends StructOp<S>, ST extends Struct<S>> Global<S, ST> addGlobal(
			GlobalSettings settings,
			ST struct) {

		final Global<S, ST> global = new Global<S, ST>(settings, struct);
		final SubData<S> instanceData = struct.setGlobal(global);

		instanceData.allocateType(false);

		return global;
	}

	final void allocatingType(AbstractTypeData<?> typeData) {
		++this.typesAllocating;
	}

	final void allocatedType(
			AbstractTypeData<?> typeData,
			boolean immediately) {
		if (immediately) {
			--this.typesAllocating;
		}
		allocateScheduled();
	}

	void scheduleTypeAllocation(AbstractTypeData<?> typeData) {
		if (!allocateScheduled()) {
			this.scheduled.addLast(typeData);
			return;
		}
		typeData.completeAllocation(false);
	}

	final void globalAllocated(SubData<?> global) {
		this.globals.add(global);
	}

	private boolean allocateScheduled() {
		if (this.typesAllocating != 0) {
			assert this.typesAllocating > 0 :
				"Wrong types allocation count";
			return false;
		}
		for (;;) {

			final AbstractTypeData<?> scheduled = this.scheduled.poll();

			if (scheduled == null) {
				return true;
			}

			scheduled.completeAllocation(false);
		}
	}

}
