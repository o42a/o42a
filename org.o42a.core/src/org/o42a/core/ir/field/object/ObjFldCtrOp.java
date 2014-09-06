/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.core.ir.field.object;

import static org.o42a.codegen.code.AllocationMode.ALLOCATOR_ALLOCATION;
import static org.o42a.core.ir.object.VmtIRChain.VMT_IR_CHAIN_TYPE;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.DataRecOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.code.op.StructRecOp;
import org.o42a.codegen.data.DataRec;
import org.o42a.codegen.data.StructRec;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.object.VmtIRChain;
import org.o42a.util.string.ID;


public class ObjFldCtrOp extends StructOp<ObjFldCtrOp> {

	public static final Type OBJ_FLD_CTR_TYPE = new Type();
	public static final Allocatable<ObjFldCtrOp> ALLOCATABLE_OBJ_FLD_CTR =
			new AllocatableObjFldCtr();

	public static final ID OBJ_FLD_CTR_ID = ID.id("obj_fld_ctr");

	private ObjFldCtrOp(StructWriter<ObjFldCtrOp> writer) {
		super(writer);
	}

	@Override
	public final Type getType() {
		return (Type) super.getType();
	}

	public final DataRecOp owner(Code code) {
		return ptr(null, code, getType().owner());
	}

	public final StructRecOp<VmtIRChain.Op> vmtc(Code code) {
		return ptr(null, code, getType().vmtc());
	}

	public final DataRecOp ancestor(Code code) {
		return ptr(null, code, getType().ancestor());
	}

	public static final class Type
			extends org.o42a.codegen.data.Type<ObjFldCtrOp> {

		private DataRec owner;
		private StructRec<VmtIRChain.Op> vmtc;
		private DataRec ancestor;

		Type() {
			super(ID.rawId("o42a_fld_obj_ctr_t"));
		}

		public final DataRec owner() {
			return this.owner;
		}

		public final StructRec<VmtIRChain.Op> vmtc() {
			return this.vmtc;
		}

		public final DataRec ancestor() {
			return this.ancestor;
		}

		@Override
		public ObjFldCtrOp op(StructWriter<ObjFldCtrOp> writer) {
			return new ObjFldCtrOp(writer);
		}

		@Override
		protected void allocate(SubData<ObjFldCtrOp> data) {
			this.owner = data.addDataPtr("owner");
			this.vmtc = data.addPtr("vmtc", VMT_IR_CHAIN_TYPE);
			this.ancestor = data.addDataPtr("ancestor");
		}

		@Override
		protected DebugTypeInfo createTypeInfo() {
			return externalTypeInfo(0x042a02f0);
		}

	}

	private static final class AllocatableObjFldCtr
			implements Allocatable<ObjFldCtrOp> {

		@Override
		public AllocationMode getAllocationMode() {
			return ALLOCATOR_ALLOCATION;
		}

		@Override
		public int getDisposePriority() {
			return NORMAL_DISPOSE_PRIORITY;
		}

		@Override
		public ObjFldCtrOp allocate(
				Allocations code,
				Allocated<ObjFldCtrOp> allocated) {
			return code.allocate(OBJ_FLD_CTR_ID, OBJ_FLD_CTR_TYPE);
		}

		@Override
		public void init(Code code, ObjFldCtrOp allocated) {
			allocated.ancestor(code).store(code, code.nullDataPtr());
		}

		@Override
		public void dispose(Code code, Allocated<ObjFldCtrOp> allocated) {
		}

	}

}
