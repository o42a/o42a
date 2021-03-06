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

import static org.o42a.core.ir.object.ObjectDataIR.OBJECT_DATA_ID;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.FuncOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.code.op.StructRecOp;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.object.desc.ObjectIRDescOp;
import org.o42a.core.ir.object.vmt.VmtIRChain;
import org.o42a.core.ir.value.ObjectValueFn;
import org.o42a.core.ir.value.ValType;
import org.o42a.util.string.ID;


public final class ObjectIRDataOp extends StructOp<ObjectIRDataOp> {

	ObjectIRDataOp(StructWriter<ObjectIRDataOp> writer) {
		super(writer);
	}

	@Override
	public final ObjectIRData getType() {
		return (ObjectIRData) super.getType();
	}

	public final ObjectDataOp op(CodeBuilder builder) {
		return new ObjectDataOp(builder, this);
	}

	public final FuncOp<ObjectValueFn> valueFunc(Code code) {
		return func(null, code, getType().valueFunc());
	}

	public final ValType.Op value(Code code) {
		return struct(null, code, getType().value());
	}

	public final StructRecOp<VmtIRChain.Op> vmtc(Code code) {
		return ptr(null, code, getType().vmtc());
	}

	public final StructRecOp<ObjectIRDescOp> desc(Code code) {
		return ptr(null, code, getType().desc());
	}

	@Override
	public String toString() {
		return getType() + " data";
	}

	@Override
	protected ID fieldId(Code code, ID local) {
		return OBJECT_DATA_ID.setLocal(local);
	}

}
