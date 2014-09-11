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

import static org.o42a.codegen.data.Struct.structContent;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Fn;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;
import org.o42a.util.Chain;
import org.o42a.util.collect.ReadonlyIterable;
import org.o42a.util.collect.ReadonlyIterator;
import org.o42a.util.string.ID;


public abstract class SubData<S extends StructOp<S>>
		extends Data<S>
		implements ReadonlyIterable<Data<?>> {

	private final Type<S> instance;
	private final Chain<Data<?>> data =
			new Chain<>(Data::getNext, Data::setNext);
	private int size;
	private boolean allocationComplete;

	SubData(Generator generator, ID id, Type<S> instance) {
		super(generator, id);
		this.instance = instance;
	}

	@Override
	public DataType getDataType() {
		return DataType.STRUCT;
	}

	@Override
	public final Type<S> getInstance() {
		return this.instance;
	}

	public abstract boolean isTypeData();

	public final Int8rec addInt8(String name) {
		return addInt8(ID.id(name));
	}

	public final Int8rec addInt8(ID name) {
		return add(new Int8rec(this, name));
	}

	public final Int16rec addInt16(String name) {
		return addInt16(ID.id(name));
	}

	public final Int16rec addInt16(ID name) {
		return add(new Int16rec(this, name));
	}

	public final Int32rec addInt32(String name) {
		return addInt32(ID.id(name));
	}

	public final Int32rec addInt32(ID name) {
		return add(new Int32rec(this, name));
	}

	public final Int64rec addInt64(String name) {
		return addInt64(ID.id(name));
	}

	public final Int64rec addInt64(ID name) {
		return add(new Int64rec(this, name));
	}

	public final Fp32rec addFp32(String name) {
		return addFp32(ID.id(name));
	}

	public final Fp32rec addFp32(ID name) {
		return add(new Fp32rec(this, name));
	}

	public final Fp64rec addFp64(String name) {
		return addFp64(ID.id(name));
	}

	public final Fp64rec addFp64(ID name) {
		return add(new Fp64rec(this, name));
	}

	public final SystemData addSystem(String name, SystemType systemType) {
		return addSystem(ID.id(name), systemType);
	}

	public final SystemData addSystem(ID name, SystemType systemType) {
		systemType.allocate(getGenerator());
		return add(new SystemData(this, name, systemType));
	}

	public final <F extends Fn<F>>
	FuncRec<F> addFuncPtr(String name, Signature<F> signature) {
		return addFuncPtr(ID.id(name), signature);
	}

	public final <F extends Fn<F>>
	FuncRec<F> addFuncPtr(ID name, Signature<F> signature) {
		return add(new FuncRec<>(
				this,
				name,
				getGenerator().getFunctions().allocate(signature)));
	}

	public final AnyRec addPtr(String name) {
		return addPtr(ID.id(name));
	}

	public final AnyRec addPtr(ID name) {
		return add(new AnyRec(this, name));
	}

	public final DataRec addDataPtr(String name) {
		return addDataPtr(ID.id(name));
	}

	public final DataRec addDataPtr(ID name) {
		return add(new DataRec(this, name));
	}

	public final <SS extends StructOp<SS>>
	StructRec<SS> addPtr(String name, Type<SS> type) {
		return addPtr(ID.id(name), type);
	}

	public final <SS extends StructOp<SS>>
	StructRec<SS> addPtr(ID name, Type<SS> type) {
		return add(new StructRec<>(this, name, type));
	}

	public final RelRec addRelPtr(String name) {
		return addRelPtr(ID.id(name));
	}

	public final RelRec addRelPtr(ID name) {
		return add(new RelRec(this, name));
	}

	public final <SS extends StructOp<SS>, T extends Type<SS>>
	T addInstance(ID name, T type) {
		return addInstance(name, type, null, null);
	}

	public final <SS extends StructOp<SS>, T extends Type<SS>>
	T addInstance(ID name, T type, Content<? extends T> content) {
		return addInstance(name, type, null, content);
	}

	public final <SS extends StructOp<SS>, T extends Type<SS>>
	T addInstance(ID name, T type, T instance) {
		return addInstance(name, type, instance, null);
	}

	public final <SS extends StructOp<SS>, T extends Type<SS>>
	T addInstance(ID name, T type, T instance, Content<? extends T> content) {

		final T typeInstance = type.instantiate(this, name, instance, content);
		final SubData<?> data = typeInstance.getInstanceData();

		add(data);

		return typeInstance;
	}

	public final <SS extends StructOp<SS>, T extends Struct<SS>>
	T addStruct(ID name, T type, T instance) {

		final Content<T> content = structContent();

		return addInstance(name, type, instance, content);
	}

	public final <SS extends Struct<?>> SS addStruct(ID name, SS struct) {
		add(struct.setStruct(this, name));
		return struct;
	}

	public final <
			SS extends StructOp<SS>,
			T extends Type<SS>> Allocated<SS, T> allocate(
					ID name,
					T type) {

		final T instance = type.instantiate(this, name, null, null);
		final SubData<SS> instanceData =
				add(instance.getInstanceData(), false);

		instanceData.startAllocation(
				getGenerator().getGlobals().dataAllocator());

		return new Allocated<>(instance, type, instanceData);
	}

	public final <SS extends StructOp<SS>, T extends Type<SS>>
	Allocated<SS, T> allocateStruct(ID name, T struct) {

		final SubData<SS> instanceData =
				add(struct.setStruct(this, name), false);

		instanceData.startAllocation(
				getGenerator().getGlobals().dataAllocator());

		return new Allocated<>(struct, struct, instanceData);
	}

	public final int size() {
		return this.size;
	}

	@Override
	public ReadonlyIterator<Data<?>> iterator() {
		return this.data.iterator();
	}

	@Override
	protected final void allocate(DataAllocator allocator) {
		if (startAllocation(allocator)) {
			allocateContents();
			this.allocationComplete = true;
			endAllocation(allocator);
		}
	}

	protected abstract void allocateType(boolean fully);

	protected abstract boolean startAllocation(DataAllocator allocator);

	protected abstract void allocateContents();

	protected abstract void endAllocation(DataAllocator allocator);

	protected <D extends Data<?>> D add(D data, boolean allocate) {
		if (allocate) {
			data.allocateData();
		}
		this.size++;
		return this.data.add(data);
	}

	final <D extends Data<?>> D add(D data) {
		return add(data, true);
	}

	final Chain<Data<?>> data() {
		return this.data;
	}

	final void endAllocation() {
		assert !this.allocationComplete :
			"Allocation already complete: " + this;
		this.allocationComplete = true;
		endAllocation(getGenerator().getGlobals().dataAllocator());
	}

	final void writeIncluded(DataWriter writer) {

		Data<?> data = this.data.getFirst();

		while (data != null) {
			data.write(writer);
			data = data.getNext();
		}
	}

}
