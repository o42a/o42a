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
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.debug.DebugHeader;
import org.o42a.codegen.debug.TypeDebugBase;


public abstract class Type<S extends StructOp<S>>
		extends TypeDebugBase
		implements Cloneable {

	@SuppressWarnings("rawtypes")
	private static final EmptyContent<?> EMPTY_CONTENT = new EmptyContent();

	@SuppressWarnings("unchecked")
	public static <T> Content<T> emptyContent() {
		return (Content<T>) EMPTY_CONTENT;
	}

	private CodeId id;
	Type<S> type;
	SubData<S> data;
	private Generator allocated;
	private Generator allocating;

	public Type() {
		this.type = this;
	}

	public final Generator getGenerator() {
		return this.data.getGenerator();
	}

	public final CodeId getId() {
		if (this.id != null) {
			return this.id;
		}
		return codeId(DEFAULT_CODE_ID_FACTORY);
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

	public final Type<S> getType() {
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

	public final Ptr<S> pointer(Generator generator) {
		return data(generator, false).getPointer();
	}

	public final Data<S> data(Generator generator) {
		return data(generator, true);
	}

	public final int size(Generator generator) {
		ensureTypeAllocated(generator, true);
		return this.data.size();
	}

	public final DataLayout layout(Generator generator) {
		return data(generator, true).getLayout();
	}

	public abstract S op(StructWriter<S> writer);

	public Iterable<Data<?>> iterate(Generator generator) {
		ensureTypeAllocated(generator, true);
		return this.data;
	}

	@Override
	public String toString() {
		return getId().toString();
	}

	protected abstract CodeId buildCodeId(CodeIdFactory factory);

	protected abstract void allocate(SubData<S> data);

	@SuppressWarnings("unchecked")
	@Override
	protected Type<S> clone() {
		try {
			return (Type<S>) super.clone();
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

	final void allocateInstance(SubData<S> data) {

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

	final SubData<S> setGlobal(Global<S, ?> global) {
		return this.data = new GlobalStructData<S>(global, this);
	}

	final DataAllocation<S> getAllocation() {
		return this.data.getPointer().getAllocation();
	}

	@SuppressWarnings("unchecked")
	final <I extends Type<S>> I instantiate(
			SubData<?> enclosing,
			CodeId id,
			I instance,
			Content<I> content) {
		ensureTypeAllocated(enclosing.getGenerator(), true);

		final I typeInstance;

		if (instance == null) {
			typeInstance = (I) clone();
		} else {
			typeInstance = instance;
			typeInstance.type = this.type;
		}

		typeInstance.data =
				new TypeInstanceData<S>(enclosing, id, typeInstance, content);

		return typeInstance;
	}

	@SuppressWarnings("unchecked")
	final <I extends Type<S>> I instantiate(
			Global<S, ?> global,
			I instance,
			Content<I> content) {
		ensureTypeAllocated(global.getGenerator(), true);

		final I typeInstance;

		if (instance == null) {
			typeInstance = (I) clone();
		} else {
			typeInstance = instance;
			typeInstance.type = this.type;
		}

		typeInstance.data =
				new GlobalInstanceData<S>(global, typeInstance, content);

		return typeInstance;
	}

	final SubData<S> setStruct(SubData<?> enclosing, CodeId name) {
		return this.data = new StructData<S>(enclosing, this, name);
	}

	final SubData<S> getInstanceData() {
		return this.data;
	}

	final SubData<S> createTypeData(Generator generator) {
		if (this.data == null) {
			return this.data = new TypeData<S>(generator, this);
		}
		if (this.data.getGenerator() != generator) {
			assert this.data instanceof TypeData :
				"Wrong data type of " + codeId(generator) + ": "
				+ this.data.getClass().getName();
			return this.data = new TypeData<S>(generator, this);
		}
		return null;
	}

	private final Data<S> data(Generator generator, boolean fullyAllocated) {
		ensureTypeAllocated(generator, fullyAllocated);
		return this.data;
	}

	private final void ensureTypeAllocated(Generator generator, boolean fully) {
		if (getType() != this) {
			getType().ensureTypeAllocated(generator, fully);
			return;
		}
		createTypeData(generator);
		this.data.allocateType(fully);
		if (fully) {
			assert isAllocated(generator) :
				"Not allocated: " + codeId(generator);
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
