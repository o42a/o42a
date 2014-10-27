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

import static org.o42a.core.ir.field.RefVmtRecord.CONSTRUCT_ID;
import static org.o42a.core.ir.field.object.ObjectConstructorFn.OBJECT_CONSTRUCTOR;
import static org.o42a.core.ir.value.ObjectValueFn.OBJECT_VALUE;
import static org.o42a.util.fn.Init.init;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.DataRecOp;
import org.o42a.codegen.code.op.FuncOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.*;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.field.FldKind;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.value.ObjectValueFn;
import org.o42a.core.object.Obj;
import org.o42a.util.fn.Init;
import org.o42a.util.string.ID;


public final class ObjFldConf {

	public static final Type OBJ_FLD_CONF_TYPE = new Type();

	public static final ID CONF_ID = ID.id("conf");

	private final ObjFld fld;
	private final ID id;
	private final Init<Ptr<Op>> ptr = init(this::allocate);
	private final Init<FuncPtr<ObjectConstructorFn>> constructor =
			init(this::findConstructor);

	ObjFldConf(ObjFld fld) {
		this.fld = fld;
		this.id = fld.getId().detail(CONF_ID);
	}

	public final ID getId() {
		return this.id;
	}

	public final ObjFld fld() {
		return this.fld;
	}

	public final Ptr<Op> ptr() {
		return this.ptr.get();
	}

	public final FuncPtr<ObjectConstructorFn> constructor() {
		return this.constructor.get();
	}

	private Ptr<Op> allocate() {
		return fld()
				.getGenerator()
				.newGlobal()
				.newInstance(getId(), OBJ_FLD_CONF_TYPE, this::fill)
				.getPointer();
	}

	private void fill(Type instance) {

		final Generator generator = instance.getGenerator();
		final Obj target = fld().getTarget();
		final ObjectIR targetIR = target.ir(generator);

		if (target.value().getDefinitions().areInherited()) {
			instance.valueFn().setConstant(true).setNull();
		} else {
			instance.valueFn()
			.setConstant(true)
			.setValue(targetIR.getObjectValueIR().ptr());
		}

		instance.vmt()
		.setConstant(true)
		.setValue(targetIR.getVmtIR().pointer(generator).toData());
	}

	private FuncPtr<ObjectConstructorFn> findConstructor() {
		// Create the constructor function only once
		// (in field declaration).
		if (fld().getBodyIR().bodies().isTypeBodies()
				|| fld().getField().isOverride()) {

			final ObjFld decl =
					(ObjFld) fld()
					.get(
							fld()
							.getKey()
							.getOrigin()
							.toObject()
							.ir(fld().getGenerator())
							.bodies());

			return decl.conf().constructor();
		}

		return createConstructor();
	}

	private FuncPtr<ObjectConstructorFn> createConstructor() {
		return fld().getGenerator().newFunction().create(
				fld().getId().detail(CONSTRUCT_ID),
				OBJECT_CONSTRUCTOR,
				new ObjFldConstructorBuilder(fld()))
				.getPointer();
	}

	public static final class Op extends StructOp<Op> {

		private Op(StructWriter<Op> writer) {
			super(writer);
		}

		@Override
		public final Type getType() {
			return (Type) super.getType();
		}

		public final FuncOp<ObjectValueFn> valueFn(ID id, Code code) {
			return func(id, code, getType().valueFn());
		}

		public final DataRecOp vmt(ID id, Code code) {
			return ptr(id, code, getType().vmt());
		}

	}

	public static final class Type
			extends org.o42a.codegen.data.Type<Op> {

		private FuncRec<ObjectValueFn> valueFn;
		private DataRec vmt;

		private Type() {
			super(ID.rawId("o42a_fld_obj_conf_t"));
		}

		public final FuncRec<ObjectValueFn> valueFn() {
			return this.valueFn;
		}

		public final DataRec vmt() {
			return this.vmt;
		}

		@Override
		public Op op(StructWriter<Op> writer) {
			return new Op(writer);
		}

		@Override
		protected void allocate(SubData<Op> data) {
			this.valueFn = data.addFuncPtr("value_f", OBJECT_VALUE);
			this.vmt = data.addDataPtr("vmt");
		}

		@Override
		protected DebugTypeInfo createTypeInfo() {
			return externalTypeInfo(0x042a0280 | FldKind.OBJ.code());
		}

	}

}
