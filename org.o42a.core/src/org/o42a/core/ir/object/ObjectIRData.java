/*
    Compiler Core
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
package org.o42a.core.ir.object;

import static org.o42a.core.ir.field.object.FldCtrOp.FLD_CTR_TYPE;
import static org.o42a.core.ir.object.ObjectIRType.OBJECT_TYPE;
import static org.o42a.core.ir.object.type.ValueTypeDescOp.VALUE_TYPE_DESC_TYPE;
import static org.o42a.core.ir.object.value.ObjectValueFunc.OBJECT_VALUE;
import static org.o42a.core.ir.system.MutexSystemType.MUTEX_SYSTEM_TYPE;
import static org.o42a.core.ir.system.ThreadCondSystemType.THREAD_COND_SYSTEM_TYPE;
import static org.o42a.core.ir.value.ObjectValFunc.OBJECT_VAL;

import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.data.*;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.object.impl.ObjectIRAscendants;
import org.o42a.core.ir.object.impl.ObjectIRSamples;
import org.o42a.core.ir.object.type.ValueTypeDescOp;
import org.o42a.core.ir.object.value.ObjectValueFunc;
import org.o42a.core.ir.op.RelList;
import org.o42a.core.ir.value.ObjectValFunc;
import org.o42a.util.string.ID;


public final class ObjectIRData extends Type<ObjectIRDataOp> {

	public static final short OBJ_FLAG_RT = 0x1;
	public static final short OBJ_FLAG_ABSTRACT = 0x2;
	public static final short OBJ_FLAG_PROTOTYPE = 0x4;
	public static final short OBJ_FLAG_VOID = ~0x7fff;
	public static final short OBJ_FLAG_NONE = 0x4000;

	public static final ObjectIRData OBJECT_DATA_TYPE = new ObjectIRData();

	private static final Type<?>[] TYPE_DEPENDENCIES =
			new Type<?>[] {OBJECT_TYPE};

	private RelRec object;
	private RelRec start;
	private Int16rec flags;
	private FuncRec<ObjectValueFunc> valueFunc;
	private FuncRec<ObjectValFunc> claimFunc;
	private FuncRec<ObjectValFunc> propositionFunc;
	private StructRec<ValueTypeDescOp> valueType;
	private RelList<ObjectIRBody> ascendants;
	private RelList<ObjectIRBody> samples;

	private ObjectIRData() {
		super(ID.rawId("o42a_obj_data_t"));
	}

	@Override
	public final Type<?>[] getTypeDependencies() {
		return TYPE_DEPENDENCIES;
	}

	public final RelRec object() {
		return this.object;
	}

	public final RelRec start() {
		return this.start;
	}

	public final Int16rec flags() {
		return this.flags;
	}

	public final FuncRec<ObjectValueFunc> valueFunc() {
		return this.valueFunc;
	}

	public final FuncRec<ObjectValFunc> claimFunc() {
		return this.claimFunc;
	}

	public final FuncRec<ObjectValFunc> propositionFunc() {
		return this.propositionFunc;
	}

	public final StructRec<ValueTypeDescOp> valueType() {
		return this.valueType;
	}

	public final RelList<ObjectIRBody> ascendants() {
		return this.ascendants;
	}

	public final RelList<ObjectIRBody> samples() {
		return this.samples;
	}

	@Override
	public ObjectIRDataOp op(StructWriter<ObjectIRDataOp> writer) {
		return new ObjectIRDataOp(writer);
	}

	@Override
	protected void allocate(SubData<ObjectIRDataOp> data) {
		this.object = data.addRelPtr("object");
		this.start = data.addRelPtr("start");
		this.flags = data.addInt16("flags");
		data.addInt8("mutex_init").setValue((byte) 0);
		data.addSystem("mutex", MUTEX_SYSTEM_TYPE);
		data.addSystem("thread_cond", THREAD_COND_SYSTEM_TYPE);
		this.valueFunc = data.addFuncPtr("value_f", OBJECT_VALUE);
		this.claimFunc = data.addFuncPtr("claim_f", OBJECT_VAL);
		this.propositionFunc = data.addFuncPtr("proposition_f", OBJECT_VAL);
		this.valueType = data.addPtr("value_type", VALUE_TYPE_DESC_TYPE);
		data.addPtr("fld_ctrs", FLD_CTR_TYPE).setNull();
		this.ascendants = new ObjectIRAscendants().allocate(data, "ascendants");
		this.samples = new ObjectIRSamples().allocate(data, "samples");
	}

	@Override
	protected DebugTypeInfo createTypeInfo() {
		return externalTypeInfo(0x042a0100);
	}

}
