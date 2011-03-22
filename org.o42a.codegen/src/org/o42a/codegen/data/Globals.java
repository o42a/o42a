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
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;


public abstract class Globals {

	private final Generator generator;

	private final DataChain globals = new DataChain();
	private int typesAllocating;
	private final LinkedList<AbstractTypeData<?>> scheduled =
		new LinkedList<AbstractTypeData<?>>();

	public Globals(Generator generator) {
		this.generator = generator;
	}

	public final Generator getGenerator() {
		return this.generator;
	}

	public final GlobalSettings newGlobal() {
		return new GlobalSettings(this);
	}

	public final Ptr<AnyOp> addBinary(CodeId id, byte[] data) {
		return addBinary(id, data, 0, data.length);
	}

	public Ptr<AnyOp> addBinary(CodeId id, byte[] data, int start, int end) {
		return new Ptr<AnyOp>(dataAllocator().addBinary(id, data, start, end));
	}

	public void write() {

		final DataWriter writer = dataWriter();
		Data<?> global = this.globals.getFirst();

		while (global != null) {
			global.write(writer);
			global = global.getNext();
		}

		this.globals.empty();
	}

	protected abstract DataAllocator dataAllocator();

	protected abstract DataWriter dataWriter();

	protected abstract void registerType(SubData<?> type);

	protected abstract void addType(SubData<?> type);

	protected abstract void addGlobal(SubData<?> global);

	<O extends StructOp, T extends Type<O>> Global<O, T> addGlobal(
			GlobalSettings settings,
			CodeId id,
			T type,
			T instance,
			Content<T> content) {

		final Global<O, T> global =
			new Global<O, T>(settings, id, type, instance, content);
		final SubData<O> data = global.getInstance().getTypeData();

		data.allocateType(false);

		return global;
	}

	<O extends StructOp, S extends Struct<O>> Global<O, S> addGlobal(
			GlobalSettings settings,
			S struct) {

		final Global<O, S> global = new Global<O, S>(settings, struct);

		struct.setGlobal(global);

		final SubData<O> data = global.getInstance().getTypeData();

		data.allocateType(false);

		return global;
	}

	final void allocatingType(AbstractTypeData<?> typeData) {
		++this.typesAllocating;
	}

	final void allocatedType(
			AbstractTypeData<?> typeData,
			boolean immediately) {
		addType(typeData);
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
		typeData.end(false);
	}

	final void globalCreated(SubData<?> global) {
		this.globals.add(global);
		addGlobal(global);
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

			scheduled.end(false);
		}
	}

}
