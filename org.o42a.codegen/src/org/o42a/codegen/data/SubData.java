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

import static org.o42a.codegen.data.Struct.structContent;

import java.util.Iterator;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;


public abstract class SubData<S extends StructOp<S>>
		extends Data<S>
		implements Iterable<Data<?>> {

	private final Type<S> instance;
	private final DataChain data = new DataChain();
	private int size;
	private boolean allocationComplete;

	SubData(Generator generator, CodeId id, Type<S> instance) {
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

	public final Int8rec addInt8(String name) {
		return add(new Int8rec(this, id(name), null));
	}

	public final Int8rec addInt8(String name, Content<Int8rec> content) {
		return add(new Int8rec(this, id(name), content));
	}

	public final Int16rec addInt16(String name) {
		return add(new Int16rec(this, id(name), null));
	}

	public final Int16rec addInt16(String name, Content<Int16rec> content) {
		return add(new Int16rec(this, id(name), content));
	}

	public final Int32rec addInt32(String name) {
		return add(new Int32rec(this, id(name), null));
	}

	public final Int32rec addInt32(String name, Content<Int32rec> content) {
		return add(new Int32rec(this, id(name), content));
	}

	public final Int64rec addInt64(String name) {
		return add(new Int64rec(this, id(name), null));
	}

	public final Int64rec addInt64(String name, Content<Int64rec> content) {
		return add(new Int64rec(this, id(name), content));
	}

	public final Fp32rec addFp32(String name) {
		return add(new Fp32rec(this, id(name), null));
	}

	public final Fp32rec addFp32(String name, Content<Fp32rec> content) {
		return add(new Fp32rec(this, id(name), content));
	}

	public final Fp64rec addFp64(String name) {
		return add(new Fp64rec(this, id(name), null));
	}

	public final Fp64rec addFp64(String name, Content<Fp64rec> content) {
		return add(new Fp64rec(this, id(name), content));
	}

	public final <F extends Func<F>> FuncRec<F> addFuncPtr(
			String name,
			Signature<F> signature) {
		return addFuncPtr(name, signature, null);
	}

	public final <F extends Func<F>> FuncRec<F> addFuncPtr(
			String name,
			Signature<F> signature,
			Content<FuncRec<F>> content) {
		return add(new FuncRec<F>(
				this,
				id(name),
				getGenerator().getFunctions().allocate(signature),
				content));
	}

	public final AnyRec addPtr(String name) {
		return add(new AnyRec(this, id(name), null));
	}

	public final AnyRec addPtr(
			String name,
			Content<AnyRec> content) {
		return add(new AnyRec(this, id(name), content));
	}

	public final DataRec addDataPtr(String name) {
		return addDataPtr(name, null);
	}

	public final DataRec addDataPtr(
			String name,
			Content<DataRec> content) {
		return add(new DataRec(this, id(name), content));
	}

	public final <SS extends StructOp<SS>> StructRec<SS> addPtr(
			String name,
			Type<SS> type) {
		return add(new StructRec<SS>(this, id(name), type, null));
	}

	public final <SS extends StructOp<SS>> StructRec<SS> addPtr(
			String name,
			Type<SS> type,
			Content<StructRec<SS>> content) {
		return add(new StructRec<SS>(
				this,
				id(name),
				type,
				content));
	}

	public final RelRec addRelPtr(String name) {
		return add(new RelRec(this, id(name), null));
	}

	public final RelRec addRelPtr(String name, Content<RelRec> content) {
		return add(new RelRec(this, id(name), content));
	}

	public final <SS extends StructOp<SS>, T extends Type<SS>> T addInstance(
			CodeId name,
			T type) {
		return addInstance(name, type, null, null);
	}

	public final <SS extends StructOp<SS>, T extends Type<SS>> T addInstance(
			CodeId name,
			T type,
			Content<T> content) {
		return addInstance(name, type, null, content);
	}

	public final <SS extends StructOp<SS>, T extends Type<SS>> T addInstance(
			CodeId name,
			T type,
			T instance) {
		return addInstance(name, type, instance, null);
	}

	public final <SS extends StructOp<SS>, T extends Type<SS>> T addInstance(
			CodeId name,
			T type,
			T instance,
			Content<T> content) {

		final T typeInstance = type.instantiate(this, name, instance, content);
		final SubData<?> data = typeInstance.getInstanceData();

		add(data);

		return typeInstance;
	}

	public final <SS extends StructOp<SS>, T extends Struct<SS>> T addStruct(
			CodeId name,
			T type,
			T instance) {

		final Content<T> content = structContent();

		return addInstance(name, type, instance, content);
	}

	public final <SS extends Struct<?>> SS addStruct(CodeId name, SS struct) {
		add(struct.setStruct(this, name));
		return struct;
	}

	public final <
			SS extends StructOp<SS>,
			T extends Type<SS>> Allocated<SS, T> allocate(
					CodeId name,
					T type) {

		final T instance = type.instantiate(this, name, null, null);
		final SubData<SS> instanceData =
				add(instance.getInstanceData(), false);

		instanceData.startAllocation(
				getGenerator().getGlobals().dataAllocator());

		return new Allocated<SS, T>(instance, type, instanceData);
	}

	public final <
			SS extends StructOp<SS>,
			T extends Type<SS>> Allocated<SS, T> allocateStruct(
					CodeId name,
					T struct) {

		final SubData<SS> instanceData =
				add(struct.setStruct(this, name), false);

		instanceData.startAllocation(
				getGenerator().getGlobals().dataAllocator());

		return new Allocated<SS, T>(struct, struct, instanceData);
	}

	public final int size() {
		return this.size;
	}

	@Override
	public Iterator<Data<?>> iterator() {
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

	final DataChain data() {
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

	private CodeId id(String name) {
		return getGenerator().id(name);
	}

}
