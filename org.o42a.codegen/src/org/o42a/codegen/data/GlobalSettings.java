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

import org.o42a.codegen.code.op.StructOp;
import org.o42a.util.string.ID;


public final class GlobalSettings
		extends AbstractGlobalSettings<GlobalSettings> {

	GlobalSettings(Globals globals) {
		super(globals);
	}

	public final GlobalSettings export() {
		this.flags |= EXPORTED;
		return this;
	}

	public GlobalSettings dontExport() {
		this.flags &= ~EXPORTED;
		return this;
	}

	public final GlobalSettings set(GlobalAttributes attributes) {
		this.flags = attributes.getDataFlags() & GLOBAL_FLAGS;
		return this;
	}

	public final <
			S extends StructOp<S>,
			T extends Type<S>> Allocated<S, Global<S, T>> allocate(
					ID id,
					T type) {

		final Global<S, T> global = new Global<>(this, id, type, null, null);
		final SubData<S> instanceData = global.getInstance().getInstanceData();

		instanceData.startAllocation(globals().dataAllocator());

		return new Allocated.AllocatedGlobal<>(
				global,
				type,
				instanceData,
				true);
	}

	public final <S extends StructOp<S>, T extends Type<S>>
	Allocated<S, Global<S, T>> allocateStruct(T struct) {

		final Global<S, T> global = new Global<>(this, struct);
		final SubData<S> instanceData = struct.setGlobal(global);

		instanceData.startAllocation(globals().dataAllocator());

		return new Allocated.AllocatedGlobal<>(
				global,
				struct,
				instanceData,
				false);
	}

	public final <S extends StructOp<S>, T extends Type<S>> Global<S, T>
	newInstance(ID id, T type) {
		return newInstance(id, type, null);
	}

	public final <S extends StructOp<S>, T extends Type<S>> Global<S, T>
	newInstance(ID id, T type, Content<T> content) {
		return globals().addGlobal(this, id, type, null, content);
	}

	public final <S extends StructOp<S>, ST extends Struct<S>>
	Global<S, ST> struct(ST struct) {
		return globals().addGlobal(this, struct);
	}

	public final <S extends StructOp<S>, T extends Type<S>>
	Global<S, T> instance(ID id, T type, T instance) {
		return instance(id, type, instance, null);
	}

	public final <S extends StructOp<S>, T extends Type<S>>
	Global<S, T> instance(ID id, T type, T instance, Content<T> content) {
		return globals().addGlobal(this, id, type, instance, content);
	}

	public final <S extends StructOp<S>, ST extends Struct<S>>
	Global<S, ST> struct(ST type, ST instance) {

		final Content<ST> content = Struct.structContent();

		return instance(instance.getId(), type, instance, content);
	}

}
