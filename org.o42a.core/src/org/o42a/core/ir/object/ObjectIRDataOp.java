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
import static org.o42a.core.ir.object.op.ObjectDataFunc.OBJECT_DATA;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.object.op.ObjectDataFunc;
import org.o42a.core.ir.object.type.ObjectIRDescOp;
import org.o42a.core.ir.object.value.ObjectCondFunc;
import org.o42a.core.ir.object.value.ObjectValueFunc;
import org.o42a.core.ir.op.RelList;
import org.o42a.core.ir.value.ObjectDefFunc;
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

	public final ObjectDataOp op(
			CodeBuilder builder,
			ObjectPrecision precision) {
		return new ObjectDataOp(builder, this, precision);
	}

	public final StructRecOp<VmtIRChain.Op> vmtc(Code code) {
		return ptr(null, code, getType().vmtc());
	}

	public final FuncOp<ObjectValueFunc> valueFunc(Code code) {
		return func(null, code, getType().valueFunc());
	}

	public final FuncOp<ObjectCondFunc> condFunc(Code code) {
		return func(null, code, getType().condFunc());
	}

	public final FuncOp<ObjectDefFunc> defsFunc(Code code) {
		return func(null, code, getType().defFunc());
	}

	public final ValType.Op value(Code code) {
		return struct(null, code, getType().value());
	}

	public final AnyRecOp resumeFrom(Code code) {
		return ptr(null, code, getType().resumeFrom());
	}

	public final StructRecOp<ObjectIRDescOp> desc(Code code) {
		return ptr(null, code, getType().desc());
	}

	public final DataRecOp loadDeps(Code code) {

		final RelList.Op deps =
				struct(null, code, getType().deps().getInstance());
		final Int32op offset =
				deps.list(code).load(null, code).toInt32(null, code);

		return deps.toAny(null, code)
				.offset(null, code, offset)
				.toDataRec(null, code);
	}

	public final void lock(Code code) {

		final FuncPtr<ObjectDataFunc> fn =
				code.getGenerator().externalFunction().link(
						"o42a_obj_lock",
						OBJECT_DATA);

		fn.op(null, code).call(code, this);
	}

	public final void unlock(Code code) {

		final FuncPtr<ObjectDataFunc> fn =
				code.getGenerator().externalFunction().link(
						"o42a_obj_unlock",
						OBJECT_DATA);

		fn.op(null, code).call(code, this);
	}

	public final void wait(Code code) {

		final FuncPtr<ObjectDataFunc> fn =
				code.getGenerator().externalFunction().link(
						"o42a_obj_wait",
						OBJECT_DATA);

		fn.op(null, code).call(code, this);
	}

	public final void signal(Code code) {

		final FuncPtr<ObjectDataFunc> fn =
				code.getGenerator().externalFunction().link(
						"o42a_obj_signal",
						OBJECT_DATA);

		fn.op(null, code).call(code, this);
	}

	public final void broadcast(Code code) {

		final FuncPtr<ObjectDataFunc> fn =
				code.getGenerator().externalFunction().link(
						"o42a_obj_broadcast",
						OBJECT_DATA);

		fn.op(null, code).call(code, this);
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
