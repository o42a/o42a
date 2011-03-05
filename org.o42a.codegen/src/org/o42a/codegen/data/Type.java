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

import static org.o42a.codegen.CodeIdFactory.DEFAULT_CODE_ID_FACTORY;

import java.util.Iterator;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.PtrOp;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;


public abstract class Type<O extends PtrOp>
		implements Cloneable, Iterable<Data<?>> {

	@SuppressWarnings("rawtypes")
	static final EmptyContent<?> EMPTY_CONTENT = new EmptyContent();

	private CodeId id;
	private Type<O> original;
	SubData<O> data;

	public Type() {
		this.original = this;
	}

	public final CodeId getId() {
		if (this.id == null) {
			this.id = buildCodeId(DEFAULT_CODE_ID_FACTORY);
		}
		return this.id;
	}

	public final Type<O> getOriginal() {
		return this.original;
	}

	public final Ptr<O> getPointer() {
		return getData().getPointer();
	}

	public boolean isPacked() {
		return false;
	}

	public final Data<O> getData() {
		allocate();
		return this.data;
	}

	public final int size() {
		allocate();
		return this.data.size();
	}

	public final DataLayout getLayout() {
		return getData().getLayout();
	}

	public abstract O op(StructWriter writer);

	@Override
	public Iterator<Data<?>> iterator() {
		allocate();
		return this.data.iterator();
	}

	@Override
	public String toString() {
		if (this.id == null) {
			return getClass().getSimpleName();
		}
		return this.id.toString();
	}

	protected abstract CodeId buildCodeId(CodeIdFactory factory);

	protected void allocate() {
	}

	protected abstract void allocate(SubData<O> data);

	@SuppressWarnings("unchecked")
	@Override
	protected Type<O> clone() {
		try {
			return (Type<O>) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	final void allocateType(SubData<O> data) {
		refineCodeId(data.getGenerator().getCodeIdFactory());
		allocate(data);
	}

	final DataAllocation<O> allocation() {
		return this.data.getPointer().getAllocation();
	}

	final void setType() {
		this.data = new TypeData<O>(this);
	}

	final Type<O> instantiate(CodeId id, Content<?> content) {

		final Type<O> instance = clone();

		instance.data = new InstanceData<O>(id, instance, content);

		return instance;
	}

	final Type<O> instantiate(Global<O, ?> global, Content<?> content) {

		final Type<O> instance = clone();

		instance.data = new GlobalData<O>(instance, global, content);

		return instance;
	}

	final SubData<O> getTypeData() {
		return this.data;
	}

	private void refineCodeId(CodeIdFactory factory) {
		if (this.id.compatibleWith(factory)) {
			return;
		}
		this.id = buildCodeId(factory);
	}

	private static final class TypeData<O extends PtrOp> extends SubData<O> {

		TypeData(Type<O> type) {
			super(type.getId().removeLocal(), type);
		}

		@Override
		public String toString() {
			return getType().toString();
		}

		@Override
		protected void allocate(Generator generator) {
			setGenerator(generator);

			final DataAllocator allocator = generator.dataAllocator();

			setAllocation(allocator.begin(getType()));
			getType().allocate(this);
			allocator.end(getType());
		}

		@Override
		protected void write(DataWriter writer) {
			throw new IllegalStateException("Can not write type data" + this);
		}

	}

	private static class InstanceData<O extends PtrOp> extends SubData<O> {

		@SuppressWarnings("rawtypes")
		final Content content;
		private Data<?> next;

		InstanceData(CodeId id, Type<O> type, Content<?> content) {
			super(id, type);
			this.content = content != null ? content : EMPTY_CONTENT;
			this.next = type.original.data.data().getFirst();
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void allocate(Generator generator) {
			setGenerator(generator);

			final DataAllocator allocator = generator.dataAllocator();

			setAllocation(allocator.enter(getType().allocation(), this));
			getType().allocate(this);
			allocator.exit(this);
			this.content.allocated(getType());
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void write(DataWriter writer) {
			writer.enter(getPointer().getAllocation(), this);
			this.content.fill(getType());
			writeIncluded(writer);
			writer.exit(getPointer().getAllocation(), this);
		}

		@Override
		protected <D extends Data<?>> D add(D data) {
			assert this.next != null :
				"An attempt to add more fields to instance,"
				+ " than type contains: " + data + " (" + (size() + 1) + ")";
			assert data.getClass() == this.next.getClass() :
				"Wrong field " + data + " at position " + size()
				+ ", while " + this.next + " expected";

			data.getPointer().copyAllocation(this.next);
			this.next = this.next.getNext();

			return super.add(data);
		}

	}

	private static final class GlobalData<O extends PtrOp>
			extends InstanceData<O> {

		private final Global<O, ?> global;

		GlobalData(Type<O> type, Global<O, ?> global, Content<?> content) {
			super(global.getId().removeLocal(), type, content);
			this.global = global;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void allocate(Generator generator) {
			setGenerator(generator);

			final DataAllocator allocator = generator.dataAllocator();

			setAllocation(allocator.begin(getAllocation(), this.global));
			getType().allocate(this);
			allocator.end(this.global);
			this.content.allocated(getType());
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void write(DataWriter writer) {
			writer.begin(getPointer().getAllocation(), this.global);
			this.content.fill(getType());
			writeIncluded(writer);
			writer.end(getPointer().getAllocation(), this.global);
		}

	}

	private static final class EmptyContent<T> implements Content<T> {

		@Override
		public void allocated(T instance) {
		}

		@Override
		public void fill(T struct) {
		}

	}

}
