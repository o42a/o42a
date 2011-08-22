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
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.op.StructOp;


public final class GlobalSettings {

	private final Globals globals;

	private boolean exported;
	private boolean constant;

	GlobalSettings(Globals globals) {
		this.globals = globals;
	}

	public final Generator getGenerator() {
		return this.globals.getGenerator();
	}

	public final boolean isExported() {
		return this.exported;
	}

	public final GlobalSettings export() {
		this.exported = true;
		return this;
	}

	public GlobalSettings dontExport() {
		this.exported = false;
		return this;
	}

	public final boolean isConstant() {
		return this.constant;
	}

	public final GlobalSettings setConstant() {
		this.constant = true;
		return this;
	}

	public final GlobalSettings setVariable() {
		this.constant = false;
		return this;
	}

	public final GlobalSettings set(GlobalSettings settings) {
		this.exported = settings.exported;
		this.constant = settings.constant;
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

		return new Allocated.AllocatedGlobal<S, T>(global, type, instanceData);
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
				instanceData);
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
