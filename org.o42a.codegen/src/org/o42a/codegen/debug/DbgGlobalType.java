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

import static org.o42a.codegen.debug.DbgFieldType.DBG_FIELD_TYPE;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.AnyPtrRec;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.data.Type;


final class DbgGlobalType extends Type<DbgGlobalType.Op> {

	public static final DbgGlobalType DBG_GLOBAL_TYPE = new DbgGlobalType();

	private AnyPtrRec name;
	private AnyPtrRec start;
	private DbgFieldType content;

	private DbgGlobalType() {
	}

	@Override
	public boolean isDebugInfo() {
		return true;
	}

	public final AnyPtrRec name() {
		return this.name;
	}

	public final AnyPtrRec start() {
		return this.start;
	}

	public final DbgFieldType content() {
		return this.content;
	}

	@Override
	public Op op(StructWriter writer) {
		return new Op(writer);
	}

	@Override
	protected CodeId buildCodeId(CodeIdFactory factory) {
		return factory.id("DEBUG").sub("Global");
	}

	@Override
	protected void allocate(SubData<Op> data) {

		final Generator generator = data.getGenerator();

		this.name = data.addPtr("name");
		this.start = data.addPtr("start");
		this.content = data.addInstance(
				generator.id("content"),
				DBG_FIELD_TYPE);
	}

	public static final class Op extends StructOp {

		private Op(StructWriter writer) {
			super(writer);
		}

	}

}
