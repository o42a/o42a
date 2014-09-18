/*
    Compiler Core
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
package org.o42a.core.ir.object;

import static org.o42a.core.ir.field.object.FldCtrOp.FLD_CTR_TYPE;
import static org.o42a.core.ir.object.type.ObjectIRDesc.OBJECT_DESC_TYPE;
import static org.o42a.core.ir.system.MutexSystemType.MUTEX_SYSTEM_TYPE;
import static org.o42a.core.ir.system.ThreadCondSystemType.THREAD_COND_SYSTEM_TYPE;
import static org.o42a.core.ir.value.ObjectValueFn.OBJECT_VALUE;
import static org.o42a.core.ir.value.ValType.VAL_TYPE;

import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.data.*;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.object.VmtIRChain.Op;
import org.o42a.core.ir.object.type.ObjectIRDescOp;
import org.o42a.core.ir.value.ObjectValueFn;
import org.o42a.core.ir.value.ValType;
import org.o42a.util.string.ID;


public final class ObjectIRData extends Type<ObjectIRDataOp> {

	public static final ObjectIRData OBJECT_DATA_TYPE = new ObjectIRData();

	private static final ID VALUE_ID = ID.id("value");
	private static final Type<?>[] TYPE_DEPENDENCIES =
			new Type<?>[] {OBJECT_DESC_TYPE};

	private StructRec<VmtIRChain.Op> vmtc;
	private FuncRec<ObjectValueFn> valueFunc;
	private FuncRec<ObjectValueFn> defFunc;
	private ValType value;
	private StructRec<ObjectIRDescOp> desc;

	private ObjectIRData() {
		super(ID.rawId("o42a_obj_data_t"));
	}

	@Override
	public final Type<?>[] getTypeDependencies() {
		return TYPE_DEPENDENCIES;
	}

	public final StructRec<Op> vmtc() {
		return this.vmtc;
	}

	public final FuncRec<ObjectValueFn> valueFunc() {
		return this.valueFunc;
	}

	public final FuncRec<ObjectValueFn> defFunc() {
		return this.defFunc;
	}

	public final ValType value() {
		return this.value;
	}

	public final StructRec<ObjectIRDescOp> desc() {
		return this.desc;
	}

	@Override
	public ObjectIRDataOp op(StructWriter<ObjectIRDataOp> writer) {
		return new ObjectIRDataOp(writer);
	}

	@Override
	protected void allocate(SubData<ObjectIRDataOp> data) {
		data.addSystem("mutex", MUTEX_SYSTEM_TYPE);
		data.addSystem("thread_cond", THREAD_COND_SYSTEM_TYPE);
		this.vmtc = data.addPtr("vmtc", VmtIRChain.VMT_IR_CHAIN_TYPE);
		this.valueFunc = data.addFuncPtr("value_f", OBJECT_VALUE);
		this.defFunc = data.addFuncPtr("def_f", OBJECT_VALUE);
		this.value = data.addNewInstance(VALUE_ID, VAL_TYPE);
		this.desc = data.addPtr("desc", OBJECT_DESC_TYPE);
		data.addPtr("fld_ctrs", FLD_CTR_TYPE).setNull();
	}

	@Override
	protected DebugTypeInfo createTypeInfo() {
		return externalTypeInfo(0x042a0100);
	}

}
