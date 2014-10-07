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

import java.util.HashMap;
import java.util.LinkedList;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;
import org.o42a.util.collect.Chain;
import org.o42a.util.string.ID;


public abstract class Globals {

	private final Generator generator;
	private final Chain<Data<?>> globals =
			new Chain<>(Data::getNext, Data::setNext);
	private final HashMap<String, Ptr<?>> externals = new HashMap<>();
	private int typesAllocating;
	private final LinkedList<AbstractTypeData<?>> scheduled =
			new LinkedList<>();

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

		final ID id = ID.id("any_null");

		return this.nullPtr = new Ptr<AnyOp>(id, true, true) {
			@Override
			public Ptr<AnyOp> toAny() {
				return this;
			}
			@Override
			protected DataAllocation<AnyOp> createAllocation() {
				return dataWriter().nullPtr(this);
			}
		};
	}

	public final Ptr<DataOp> nullDataPtr() {
		if (this.nullDataPtr != null) {
			return this.nullDataPtr;
		}

		final ID id = ID.id("data_null");

		return this.nullDataPtr = new Ptr<DataOp>(id, true, true) {
			@Override
			public Ptr<DataOp> toData() {
				return this;
			}
			@Override
			protected DataAllocation<DataOp> createAllocation() {
				return dataWriter().nullDataPtr(this);
			}
		};
	}

	public final <S extends StructOp<S>> Ptr<S> nullPtr(final Type<S> type) {
		return new Ptr<S>(type.getId().detail("null"), true, true) {
			@Override
			protected DataAllocation<S> createAllocation() {
				return dataWriter().nullPtr(this, type);
			}
		};
	}

	public final <
			S extends StructOp<S>,
			T extends Type<S>> AllocatedStruct<S, T> allocateType(
					T type) {

		final SubData<S> data = type.createTypeData(getGenerator());

		assert data != null :
			"Failed to allocate type " + type;
		data.startAllocation(dataAllocator());

		return new AllocatedStruct<>(type, type, type.getInstanceData());
	}

	public final GlobalSettings newGlobal() {
		return new GlobalSettings(this);
	}

	public final ExternalGlobalSettings externalGlobal() {
		return new ExternalGlobalSettings(this);
	}

	public final Ptr<AnyOp> addBinary(
			ID id,
			boolean isConstant,
			byte[] data) {
		return addBinary(id, isConstant, data, 0, data.length);
	}

	public final Ptr<AnyOp> addBinary(
			final ID id,
			final boolean isConstant,
			final byte[] data,
			final int start,
			final int end) {
		return new Ptr<AnyOp>(id, isConstant, false) {
			@Override
			public Ptr<AnyOp> toAny() {
				return this;
			}
			@Override
			protected DataAllocation<AnyOp> createAllocation() {
				return dataAllocator().addBinary(this, data, start, end);
			}
		};
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

		this.globals.clear();

		return true;
	}

	protected abstract DataAllocator dataAllocator();

	protected abstract DataWriter dataWriter();

	protected abstract void registerType(SubData<?> type);

	<S extends StructOp<S>, T extends Type<S>> Global<S, T> addGlobal(
			GlobalSettings settings,
			ID id,
			T type,
			T instance,
			Content<? extends T> content) {

		final Global<S, T> global =
				new Global<>(settings, id, type, instance, content);
		final SubData<S> data = global.getInstance().getInstanceData();

		data.allocateType(false);
		globalAllocated(data);

		return global;
	}

	<S extends StructOp<S>, ST extends Struct<S>> Global<S, ST> addGlobal(
			GlobalSettings settings,
			ST struct) {

		final Global<S, ST> global = new Global<>(settings, struct);
		final SubData<S> instanceData = struct.setGlobal(global);

		instanceData.allocateType(false);

		return global;
	}

	final void allocatingType() {
		++this.typesAllocating;
	}

	final void allocatedType(boolean immediately) {
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

	final <S extends StructOp<S>> Ptr<S> externalGlobal(
			final String name,
			final Type<S> type,
			final ExternalGlobalSettings settings) {

		@SuppressWarnings("unchecked")
		final Ptr<S> found = (Ptr<S>) this.externals.get(name);

		if (found != null) {
			return found;
		}

		final Ptr<S> extern = new Ptr<S>(
				ID.rawId(name),
				settings.isConstant(),
				false) {
			@Override
			protected DataAllocation<S> createAllocation() {
				return dataAllocator().externStruct(
						this,
						type.data(getGenerator()).getAllocation(),
						settings);
			}
		};

		this.externals.put(name, extern);

		return extern;
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
