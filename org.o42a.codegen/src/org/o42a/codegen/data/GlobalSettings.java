/*
    Compiler Code Generator
    Copyright (C) 2010-2012 Ruslan Lopatin

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
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.op.StructOp;


public final class GlobalSettings implements GlobalAttributes {

	private final Globals globals;

	private int flags;

	GlobalSettings(Globals globals) {
		this.globals = globals;
	}

	public final Generator getGenerator() {
		return this.globals.getGenerator();
	}

	@Override
	public final boolean isExported() {
		return (this.flags & EXPORTED) != 0;
	}

	public final GlobalSettings export() {
		this.flags |= EXPORTED;
		return this;
	}

	public GlobalSettings dontExport() {
		this.flags &= ~EXPORTED;
		return this;
	}

	@Override
	public final boolean isConstant() {
		return (this.flags & CONSTANT) != 0;
	}

	public final GlobalSettings setConstant() {
		this.flags |= CONSTANT;
		return this;
	}

	public final GlobalSettings setVariable() {
		this.flags &= ~CONSTANT;
		return this;
	}

	public final GlobalSettings setConstant(boolean constant) {
		if (constant) {
			return setConstant();
		}
		return setVariable();
	}

	@Override
	public final int getDataFlags() {
		return this.flags;
	}

	public final GlobalSettings set(GlobalAttributes attributes) {
		this.flags = attributes.getDataFlags() & GLOBAL_FLAGS;
		return this;
	}

	public final <
			S extends StructOp<S>,
			T extends Type<S>> Allocated<S, Global<S, T>> allocate(
					CodeId id,
					T type) {

		final Global<S, T> global =
				new Global<S, T>(this, id, type, null, null);
		final SubData<S> instanceData = global.getInstance().getInstanceData();

		instanceData.startAllocation(this.globals.dataAllocator());

		return new Allocated.AllocatedGlobal<S, T>(
				global,
				type,
				instanceData,
				true);
	}

	public final <
			S extends StructOp<S>,
			T extends Type<S>> Allocated<S, Global<S, T>> allocateStruct(
					T struct) {

		final Global<S, T> global = new Global<S, T>(this, struct);
		final SubData<S> instanceData = struct.setGlobal(global);

		instanceData.startAllocation(this.globals.dataAllocator());

		return new Allocated.AllocatedGlobal<S, T>(
				global,
				struct,
				instanceData,
				false);
	}

	public final
	<S extends StructOp<S>, T extends Type<S>> Global<S, T> newInstance(
			CodeId id,
			T type) {
		return newInstance(id, type, null);
	}

	public final
	<S extends StructOp<S>, T extends Type<S>> Global<S, T> newInstance(
			CodeId id,
			T type,
			Content<T> content) {
		return this.globals.addGlobal(this, id, type, null, content);
	}

	public final
	<S extends StructOp<S>, ST extends Struct<S>> Global<S, ST> struct(
			ST struct) {
		return this.globals.addGlobal(this, struct);
	}

	public final
	<S extends StructOp<S>, T extends Type<S>> Global<S, T> instance(
			CodeId id,
			T type,
			T instance) {
		return instance(id, type, instance, null);
	}

	public final
	<S extends StructOp<S>, T extends Type<S>> Global<S, T> instance(
			CodeId id,
			T type,
			T instance,
			Content<T> content) {
		return this.globals.addGlobal(this, id, type, instance, content);
	}

	public final
	<S extends StructOp<S>, ST extends Struct<S>> Global<S, ST> struct(
			ST type,
			ST instance) {

		final Content<ST> content = Struct.structContent();

		return instance(
				instance.codeId(getGenerator()),
				type,
				instance,
				content);
	}

}
