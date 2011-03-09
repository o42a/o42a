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

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;


public abstract class Type<O extends StructOp> implements Cloneable {

	@SuppressWarnings("rawtypes")
	static final EmptyContent<?> EMPTY_CONTENT = new EmptyContent();

	private CodeId id;
	private Type<O> original;
	SubData<O> data;
	private Generator allocated;
	private Generator allocating;

	public Type() {
		this.original = this;
	}

	public final CodeId codeId(Generator generator) {
		return codeId(generator.getCodeIdFactory());
	}

	public final CodeId codeId(CodeIdFactory factory) {

		final CodeId id = this.id;

		if (id != null && id.compatibleWith(factory)) {
			return id;
		}

		return this.id = buildCodeId(factory);
	}

	public final Type<O> getOriginal() {
		return this.original;
	}

	public final Ptr<O> pointer(Generator generator) {
		return data(generator, false).getPointer();
	}

	public boolean isPacked() {
		return false;
	}

	public final Data<O> data(Generator generator) {
		return data(generator, true);
	}

	public final int size(Generator generator) {
		ensureTypeAllocated(generator, true);
		return this.data.size();
	}

	public final DataLayout layout(Generator generator) {
		return data(generator, true).getLayout();
	}

	public abstract O op(StructWriter writer);

	public Generator getGenerator() {
		return this.data.getGenerator();
	}

	public Iterable<Data<?>> iterate(Generator generator) {
		ensureTypeAllocated(generator, true);
		return this.data;
	}

	@Override
	public String toString() {
		if (this.id == null) {
			return getClass().getSimpleName();
		}
		return this.id.toString();
	}

	protected abstract CodeId buildCodeId(CodeIdFactory factory);

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

	final boolean startAllocation(Generator generator) {
		if (this.original.allocating == generator) {
			return false;
		}
		this.original.allocating = generator;
		return true;
	}

	final boolean isAllocated(Generator generator) {
		return this.original.allocated == generator;
	}

	final void setAllocated(Generator generator) {
		this.original.allocated = generator;
	}

	final void allocateType(SubData<O> data) {
		refineCodeId(data.getGenerator().getCodeIdFactory());
		allocate(data);
	}

	final DataAllocation<O> allocation() {
		return this.data.getPointer().getAllocation();
	}

	@SuppressWarnings("unchecked")
	final <I extends Type<O>> I instantiate(
			Generator generator,
			CodeId id,
			I instance,
			Content<?> content) {
		ensureTypeAllocated(generator, true);
		if (instance == null) {
			instance = (I) clone();
		} else {
			instance.original = this.original;
		}

		instance.data = new InstanceData<O>(id, instance, content);

		return instance;
	}

	@SuppressWarnings("unchecked")
	final <I extends Type<O>> I instantiate(
			Global<O, ?> global,
			I instance,
			Content<?> content) {
		ensureTypeAllocated(global.getGenerator(), true);
		if (instance == null) {
			instance = (I) clone();
		} else {
			instance.original = this.original;
		}

		instance.data = new GlobalInstanceData<O>(instance, global, content);

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

	private final Data<O> data(Generator generator, boolean fullyAllocated) {
		ensureTypeAllocated(generator, fullyAllocated);
		return this.data;
	}

	private final void ensureTypeAllocated(
			Generator generator,
			boolean fullyAllocated) {
		if (getOriginal() != this) {
			getOriginal().ensureTypeAllocated(generator, fullyAllocated);
			return;
		}
		if (this.data == null) {
			this.data = new TypeData<O>(generator, this);
		}
		this.data.allocateData(generator);
		if (fullyAllocated) {
			assert isAllocated(generator) :
				"Not allocated: " + this;
		}
	}

	private static final class TypeData<O extends StructOp> extends SubData<O> {

		TypeData(Generator generator, Type<O> type) {
			super(type.codeId(generator).removeLocal(), type);
		}

		@Override
		public String toString() {
			return getType().toString();
		}

		@Override
		protected void allocate(Generator generator) {
			if (!getType().startAllocation(generator)) {
				return;
			}

			final DataAllocator allocator = generator.dataAllocator();

			setAllocation(allocator.begin(getType()));
			getType().allocateType(this);
			allocator.end(getType());
			getType().setAllocated(generator);

			final Globals globals = generator;

			globals.addType(this);
		}

		@Override
		protected void write(DataWriter writer) {
			throw new UnsupportedOperationException(
					"Type " + getId() + " itself can not be written out. "
					+ "Write an instance instead.");
		}

	}

	private static class InstanceData<O extends StructOp> extends SubData<O> {

		@SuppressWarnings("rawtypes")
		final Content content;
		private Data<?> next;

		InstanceData(CodeId id, Type<O> type, Content<?> content) {
			super(id, type);
			getPointer().copyAllocation(type.getOriginal().getTypeData());
			this.content = content != null ? content : EMPTY_CONTENT;
			this.next = type.original.data.data().getFirst();
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void allocate(Generator generator) {

			final DataAllocator allocator = generator.dataAllocator();

			setAllocation(allocator.enter(getType().allocation(), this));
			getType().allocateType(this);
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

	private static final class GlobalInstanceData<O extends StructOp>
			extends InstanceData<O> {

		private final Global<O, ?> global;

		GlobalInstanceData(
				Type<O> type,
				Global<O, ?> global,
				Content<?> content) {
			super(global.getId().removeLocal(), type, content);
			getPointer().copyAllocation(type.getOriginal().getTypeData());
			this.global = global;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void allocate(Generator generator) {

			final DataAllocator allocator = generator.dataAllocator();

			setAllocation(allocator.begin(getAllocation(), this.global));
			getType().allocateType(this);
			allocator.end(this.global);
			this.content.allocated(getType());

			final Globals globals = generator;

			globals.addGlobal(this);
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void write(DataWriter writer) {
			writer.begin(getPointer().getAllocation(), this.global);
			this.content.fill(getType());
			writeIncluded(writer);
			writer.end(getPointer().getAllocation(), this.global);

			final Globals globals = getGenerator();

			globals.addType(this);
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
