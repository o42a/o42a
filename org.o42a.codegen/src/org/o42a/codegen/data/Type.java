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
import static org.o42a.codegen.debug.DebugHeader.DEBUG_HEADER_TYPE;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.TypeInstanceData.GlobalInstanceData;
import org.o42a.codegen.data.TypeInstanceData.InstanceData;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;
import org.o42a.codegen.debug.DebugHeader;
import org.o42a.codegen.debug.TypeDebugBase;


public abstract class Type<O extends StructOp>
		extends TypeDebugBase
		implements Cloneable {

	@SuppressWarnings("rawtypes")
	static final EmptyContent<?> EMPTY_CONTENT = new EmptyContent();

	private CodeId id;
	Type<O> type;
	SubData<O> data;
	private Generator allocated;
	private Generator allocating;

	public Type() {
		this.type = this;
	}

	public final Generator getGenerator() {
		return this.data.getGenerator();
	}

	public boolean isPacked() {
		return false;
	}

	public boolean isReentrant() {
		return !isDebugInfo();
	}

	public boolean isDebugInfo() {
		return false;
	}

	public boolean isDebuggable() {
		return !isDebugInfo();
	}

	public final Type<O> getType() {
		return this.type;
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

	public final Ptr<O> pointer(Generator generator) {
		return data(generator, false).getPointer();
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

	public Iterable<Data<?>> iterate(Generator generator) {
		ensureTypeAllocated(generator, true);
		return this.data;
	}

	@Override
	public String toString() {
		if (this.id == null) {
			return codeId(DEFAULT_CODE_ID_FACTORY).toString();
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
		if (this.type.allocating == generator) {
			return false;
		}
		this.type.allocating = generator;
		return true;
	}

	final boolean isAllocated(Generator generator) {
		return this.type.allocated == generator;
	}

	final void setAllocated(Generator generator) {
		this.type.allocated = generator;
	}

	final void allocateInstance(SubData<O> data) {

		final Generator generator = data.getGenerator();

		codeId(generator);// Refine code id.

		if (generator.isDebug() && isDebuggable()) {
			data.addInstance(
					generator.id("__o42a_dbg_header__"),
					DEBUG_HEADER_TYPE,
					new DebugHeader(this));
		}

		allocate(data);
	}

	final DataAllocation<O> getAllocation() {
		return this.data.getPointer().getAllocation();
	}

	@SuppressWarnings("unchecked")
	final <I extends Type<O>> I instantiate(
			SubData<?> enclosing,
			CodeId id,
			I instance,
			Content<?> content) {
		ensureTypeAllocated(enclosing.getGenerator(), true);
		if (instance == null) {
			instance = (I) clone();
		} else {
			instance.type = this.type;
		}

		instance.data = new InstanceData<O>(enclosing, id, instance, content);

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
			instance.type = this.type;
		}

		instance.data = new GlobalInstanceData<O>(global, instance, content);

		return instance;
	}

	final SubData<O> getTypeData() {
		return this.data;
	}

	private final Data<O> data(Generator generator, boolean fullyAllocated) {
		ensureTypeAllocated(generator, fullyAllocated);
		return this.data;
	}

	private final void ensureTypeAllocated(
			Generator generator,
			boolean fullyAllocated) {
		if (getType() != this) {
			getType().ensureTypeAllocated(generator, fullyAllocated);
			return;
		}
		if (this.data == null) {
			this.data = new TypeData<O>(generator, this);
		}
		this.data.allocateType(fullyAllocated);
		if (fullyAllocated) {
			assert isAllocated(generator) :
				"Not allocated: " + this;
		}
	}

	private static final class TypeData<O extends StructOp>
			extends AbstractTypeData<O> {

		TypeData(Generator generator, Type<O> type) {
			super(generator, type.codeId(generator).removeLocal(), type);
		}

		@Override
		public Global<?, ?> getGlobal() {
			return null;
		}

		@Override
		public Type<?> getEnclosing() {
			return null;
		}

		@Override
		protected DataAllocation<O> beginTypeAllocation(
				DataAllocator allocator) {
			return allocator.begin(getInstance());
		}

		@Override
		protected void endTypeAllocation(DataAllocator allocator) {
			allocator.end(getInstance());
		}

		@Override
		protected void write(DataWriter writer) {
			throw new UnsupportedOperationException(
					"Type " + getId() + " itself can not be written out. "
					+ "Write an instance instead.");
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
