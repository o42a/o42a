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

import static org.o42a.core.ir.object.ObjectIRLock.OBJECT_IR_LOCK;
import static org.o42a.core.ir.object.desc.ObjectIRDesc.OBJECT_DESC_TYPE;
import static org.o42a.core.ir.value.ObjectValueFn.OBJECT_VALUE;
import static org.o42a.core.ir.value.ValType.VAL_TYPE;

import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.data.*;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.object.desc.ObjectIRDescOp;
import org.o42a.core.ir.object.vmt.VmtIRChain;
import org.o42a.core.ir.object.vmt.VmtIRChain.Op;
import org.o42a.core.ir.value.ObjectValueFn;
import org.o42a.core.ir.value.ValType;
import org.o42a.util.string.ID;


public final class ObjectIRData extends Type<ObjectIRDataOp> {

	public static final ObjectIRData OBJECT_DATA_TYPE = new ObjectIRData();

	private static final ID VALUE_ID = ID.id("value");
	private static final Type<?>[] TYPE_DEPENDENCIES =
			new Type<?>[] {OBJECT_DESC_TYPE};

	private FuncRec<ObjectValueFn> valueFunc;
	private ValType value;
	private StructRec<VmtIRChain.Op> vmtc;
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
		this.valueFunc = data.addFuncPtr("value_f", OBJECT_VALUE);
		this.value = data.addNewInstance(VALUE_ID, VAL_TYPE);
		this.vmtc = data.addPtr("vmtc", VmtIRChain.VMT_IR_CHAIN_TYPE);
		this.desc = data.addPtr("desc", OBJECT_DESC_TYPE);
		data.addNewInstance(ID.id("lock"), OBJECT_IR_LOCK);
	}

	@Override
	protected DebugTypeInfo createTypeInfo() {
		return externalTypeInfo(0x042a0100);
	}

}
