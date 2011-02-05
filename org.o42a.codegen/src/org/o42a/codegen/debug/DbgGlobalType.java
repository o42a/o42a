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
package org.o42a.codegen.debug;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.AnyPtrRec;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.data.Type;


final class DbgGlobalType extends Type<DbgGlobalType.Op> {

	private final Generator generator;

	private AnyPtrRec name;
	private AnyPtrRec start;
	private DbgFieldType content;

	DbgGlobalType(Generator generator) {
		super(generator.id("DEBUG").sub("Global"));
		this.generator = generator;
	}

	public final AnyPtrRec getName() {
		return this.name;
	}

	public final AnyPtrRec getStart() {
		return this.start;
	}

	public final DbgFieldType getContent() {
		return this.content;
	}

	@Override
	public Op op(StructWriter writer) {
		return new Op(writer);
	}

	@Override
	protected void allocate(SubData<Op> data) {
		this.name = data.addPtr("name");
		this.start = data.addPtr("start");
		this.content = data.addInstance(
				generator().id("content"),
				debug().dbgFieldType());
	}

	final Debug debug() {
		return this.generator;
	}

	final Generator generator() {
		return this.generator;
	}

	public static final class Op extends StructOp {

		private Op(StructWriter writer) {
			super(writer);
		}

		@Override
		public Op create(StructWriter writer) {
			return new Op(writer);
		}

	}

}
