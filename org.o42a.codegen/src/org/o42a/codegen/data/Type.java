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

import static org.o42a.codegen.debug.DebugHeader.DEBUG_HEADER_TYPE;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.debug.DebugHeader;
import org.o42a.codegen.debug.TypeDebugBase;
import org.o42a.util.DataLayout;
import org.o42a.util.string.ID;


public abstract class Type<S extends StructOp<S>>
		extends TypeDebugBase
		implements Cloneable {

	private static final Type<?>[] NO_DEPENDENCIES = new Type<?>[0];

	private final ID id;
	Type<S> type;
	SubData<S> data;
	private Generator allocated;
	private Generator allocating;

	public Type(ID id) {
		this.id = id;
		this.type = this;
	}

	public final Generator getGenerator() {
		return this.data.getGenerator();
	}

	public final ID getId() {
		return this.id;
	}

	/**
	 * Whether this type is packed.
	 *
	 * @return <code>true</code> if a {{@link TypeAlignment#PACKED_TYPE packed}
	 * data alignment {@link #requiredAlignment() requested},
	 * or <code>false</code> otherwise.
	 */
	public final boolean isPacked() {
		return requiredAlignment().isPacked();
	}

	/**
	 * The required type data alignment.
	 *
	 * <p>This is a minimum data alignment. The real one can be bigger, unless
	 * a {@link TypeAlignment#PACKED_TYPE packed} alignment returned.</p>
	 *
	 * @return {@link TypeAlignment#TYPE_ALIGN_1 1 byte alignment} by default.
	 */
	public TypeAlignment requiredAlignment() {
		return TypeAlignment.TYPE_ALIGN_1;
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

	@Override
	public final Type<S> getType() {
		return this.type;
	}

	public Type<?>[] getTypeDependencies() {
		return NO_DEPENDENCIES;
	}

	public final Ptr<S> pointer(Generator generator) {
		return data(generator, false).getPointer();
	}

	public final SubData<S> data(Generator generator) {
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

	protected abstract void allocate(SubData<S> data);

	@Override
	protected Type<S> clone() {
		try {

			@SuppressWarnings("unchecked")
			final Type<S> clone = (Type<S>) super.clone();

			clone.allocated = null;
			clone.allocating = null;
			clone.data = null;

			return clone;
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

		if (generator.isDebug() && isDebuggable()) {
			data.addInstance(
					ID.id("__o42a_dbg_header__"),
					DEBUG_HEADER_TYPE,
					new DebugHeader(this));
		}

		allocate(data);
	}

	final SubData<S> setGlobal(Global<S, ?> global) {
		return this.data = new GlobalStructData<>(global, this);
	}

	final DataAllocation<S> getAllocation() {
		return this.data.getPointer().getAllocation();
	}

	@SuppressWarnings("unchecked")
	final <I extends Type<S>> I instantiate(
			SubData<?> enclosing,
			ID id,
			I instance,
			Content<? extends I> content) {
		ensureTypeAllocated(enclosing.getGenerator(), true);

		final I typeInstance;

		if (instance == null) {
			typeInstance = (I) clone();
		} else {
			typeInstance = instance;
			typeInstance.type = this.type;
		}

		typeInstance.data =
				new TypeInstanceData<>(enclosing, id, typeInstance, content);

		return typeInstance;
	}

	@SuppressWarnings("unchecked")
	final <I extends Type<S>> I instantiate(
			Global<S, ?> global,
			I instance,
			Content<? extends I> content) {
		ensureTypeAllocated(global.getGenerator(), true);

		final I typeInstance;

		if (instance == null) {
			typeInstance = (I) clone();
		} else {
			typeInstance = instance;
			typeInstance.type = this.type;
		}

		typeInstance.data =
				new GlobalInstanceData<>(global, typeInstance, content);

		return typeInstance;
	}

	final SubData<S> setStruct(SubData<?> enclosing, ID name) {
		return this.data = new StructData<>(enclosing, this, name);
	}

	final SubData<S> getInstanceData() {
		return this.data;
	}

	final SubData<S> createTypeData(Generator generator) {
		if (this.data == null) {
			return initTypeData(generator);
		}
		if (this.data.getGenerator() != generator) {
			assert this.data instanceof TypeData :
				"Wrong data type of " + getId() + ": "
				+ this.data.getClass().getName();
			initTypeData(generator);
		}
		return null;
	}

	private final SubData<S> initTypeData(Generator generator) {
		this.data = new TypeData<>(generator, this);
		allocateTypeDependencies(generator);
		return this.data;
	}

	private final void allocateTypeDependencies(Generator generator) {
		for (Type<?> type : getTypeDependencies()) {
			type.pointer(generator);
		}
	}

	private final SubData<S> data(Generator generator, boolean fullyAllocated) {
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
				"Not allocated: " + getId();
		}
	}

}
