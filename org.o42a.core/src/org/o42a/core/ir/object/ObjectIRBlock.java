/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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

import static org.o42a.core.ir.gc.GCBlockOp.GC_BLOCK_ID;
import static org.o42a.core.ir.gc.GCBlockOp.GC_BLOCK_TYPE;
import static org.o42a.core.ir.object.ObjectIRStruct.OBJECT_ID;

import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Struct;
import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.gc.GCBlock;


public class ObjectIRBlock extends Struct<ObjectIRBlock.Op> {

	private final ObjectIR objectIR;
	private final ObjectIRStruct struct;

	ObjectIRBlock(ObjectIRBodies bodies) {
		super(bodies.getObjectIR().getId());
		this.objectIR = bodies.getObjectIR();
		this.struct = new ObjectIRStruct(bodies);
	}

	public final ObjectIRStruct getStruct() {
		return this.struct;
	}

	@Override
	public Op op(StructWriter<Op> writer) {
		return new Op(writer);
	}

	@Override
	protected void allocate(SubData<Op> data) {
		data.addNewInstance(
				GC_BLOCK_ID,
				GC_BLOCK_TYPE,
				new GCBlock(this.struct, "o42a_obj_gc_desc"));
		data.addInstance(
				OBJECT_ID,
				this.objectIR.getType(),
				this.struct);
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
