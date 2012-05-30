/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.ir.object;

import static org.o42a.core.ir.system.GCBlockSystemType.GC_BLOCK_SYSTEM_TYPE;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Struct;
import org.o42a.codegen.data.SubData;
import org.o42a.core.Scope;


public class ObjectIRBlock extends Struct<ObjectIRBlock.Op> {

	private final ObjectIRStruct struct;

	public ObjectIRBlock(ObjectIR objectIR) {
		this.struct = new ObjectIRStruct(objectIR);
	}

	public final ObjectIRStruct getStruct() {
		return this.struct;
	}

	@Override
	public Op op(StructWriter<Op> writer) {
		return new Op(writer);
	}

	@Override
	protected CodeId buildCodeId(CodeIdFactory factory) {

		final Scope scope = this.struct.getObject().getScope();

		return scope.ir(this.struct.getObjectIR().getGenerator()).getId();
	}

	@Override
	protected void allocate(SubData<Op> data) {
		data.addSystem("gc_block", GC_BLOCK_SYSTEM_TYPE);
		data.addStruct(data.getGenerator().id("object"), this.struct);
	}

	@Override
	protected void fill() {
	}

	public static final class Op extends StructOp<Op> {

		private Op(StructWriter<Op> writer) {
			super(writer);
		}

	}

}
