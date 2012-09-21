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
package org.o42a.core.ir.field.variable;

import static org.o42a.codegen.code.op.Atomicity.ACQUIRE_RELEASE;
import static org.o42a.codegen.code.op.Atomicity.ATOMIC;
import static org.o42a.codegen.code.op.Atomicity.VOLATILE;
import static org.o42a.core.ir.field.object.FldCtrOp.FLD_CTR_TYPE;
import static org.o42a.core.ir.field.variable.VariableAssignerFunc.VARIABLE_ASSIGNER;
import static org.o42a.core.ir.object.ObjectIRType.OBJECT_TYPE;
import static org.o42a.core.ir.object.op.ObjectRefFunc.OBJECT_REF;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.FuncRec;
import org.o42a.codegen.data.StructRec;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.field.FldKind;
import org.o42a.core.ir.field.RefFld;
import org.o42a.core.ir.field.RefFldOp;
import org.o42a.core.ir.field.link.AbstractLinkFld;
import org.o42a.core.ir.field.object.FldCtrOp;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.object.op.ObjectRefFunc;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.ref.type.TypeRelation;
import org.o42a.util.string.ID;


public class VarFld extends AbstractLinkFld {

	public static final Type VAR_FLD = new Type();

	private FuncPtr<VariableAssignerFunc> assigner;

	public VarFld(Field field, Obj target) {
		super(field, target);
	}

	@Override
	public final FldKind getKind() {
		return FldKind.VAR;
	}

	@Override
	public Type getInstance() {
		return (Type) super.getInstance();
	}

	@Override
	public VarFldOp op(Code code, ObjOp host) {
		return new VarFldOp(
				this,
				host,
				isOmitted() ? null : host.ptr().field(code, getInstance()));
	}

	@Override
	protected Type getType() {
		return VAR_FLD;
	}

	@Override
	protected boolean mayOmit() {
		return false;
	}

	@Override
	protected void allocateMethods() {
		super.allocateMethods();

		final FuncPtr<VariableAssignerFunc> reusedAssigner = reusedAssigner();

		if (reusedAssigner != null) {
			this.assigner = reusedAssigner;
			return;
		}

		this.assigner = getGenerator().newFunction().create(
				getField().getId().detail("assigner"),
				VARIABLE_ASSIGNER,
				new VarFldAssignerBuilder(this)).getPointer();
	}

	@Override
	protected void fill() {
		super.fill();

		final Obj type = getTypeRef().getType();
		final ObjectTypeIR typeIR = type.ir(getGenerator()).getStaticTypeIR();

		getInstance().bound().setValue(
				typeIR.getInstance().pointer(getGenerator()));
		getInstance().assigner().setConstant(true).setValue(this.assigner);
	}

	@Override
	protected void buildConstructor(ObjBuilder builder, CodeDirs dirs) {

		final Block code = dirs.code();
		final RefFldOp<?, ObjectRefFunc> fld = op(code, builder.host());
		final FldCtrOp ctr =
				code.getAllocator()
				.allocation()
				.allocate(FLD_CTR_ID, FLD_CTR_TYPE);

		final Block constructed = code.addBlock("constructed");

		ctr.start(code, fld).goUnless(code, constructed.head());

		fld.ptr()
		.object(null, constructed)
		.load(null, constructed, ATOMIC)
		.toData(null, constructed)
		.returnValue(constructed);

		final DataOp res = construct(builder, dirs).toData(null, code);
		final DataRecOp objectRec =
				op(code, builder.host()).ptr().object(null, code);

		objectRec.store(code, res, ACQUIRE_RELEASE);
		ctr.finish(code, fld);

		res.returnValue(code);
	}

	private FuncPtr<VariableAssignerFunc> reusedAssigner() {
		for (Field overridden : getField().getOverridden()) {

			final TypeRelation relation =
					typeRef(overridden)
					.upgradeScope(getField())
					.relationTo(getTypeRef());

			if (relation.isSame()) {
				// Variable has the same interface type as one
				// of the overridden fields. Reuse assigner.
				final Obj overriddenOwner =
						overridden.getEnclosingScope().toObject();
				final ObjectIR overriddenOwnerIR =
						overriddenOwner.ir(getGenerator())
						.getBodyType()
						.getObjectIR();
				final VarFld overriddenFld =
						(VarFld) overriddenOwnerIR.fld(getField().getKey());

				return overriddenFld.assigner;
			}
		}

		return null;
	}

	private static TypeRef typeRef(Field field) {
		return field.toObject()
				.value()
				.getValueStruct()
				.toLinkStruct()
				.getTypeRef();
	}

	private final TypeRef getTypeRef() {
		return typeRef(getField());
	}

	public static final class Op extends RefFld.Op<Op, ObjectRefFunc> {

		private Op(StructWriter<Op> writer) {
			super(writer);
		}

		@Override
		public final Type getType() {
			return (Type) super.getType();
		}

		public final StructRecOp<ObjectIRTypeOp> bound(ID id, Code code) {
			return ptr(id, code, getType().bound());
		}

		public final FuncOp<VariableAssignerFunc> assigner(ID id, Code code) {
			return func(id, code, getType().assigner());
		}

		@Override
		protected DataOp construct(
				Code code,
				ObjOp host,
				ObjectRefFunc constructor) {
			return constructor.call(code, host);
		}

	}

	public static final class Type extends RefFld.Type<Op, ObjectRefFunc> {

		private StructRec<ObjectIRTypeOp> bound;
		private FuncRec<VariableAssignerFunc> assigner;

		private Type() {
			super(ID.rawId("o42a_fld_var"));
		}

		@Override
		public boolean isStateless() {
			return false;
		}

		public final StructRec<ObjectIRTypeOp> bound() {
			return this.bound;
		}

		public final FuncRec<VariableAssignerFunc> assigner() {
			return this.assigner;
		}

		@Override
		public Op op(StructWriter<Op> writer) {
			return new Op(writer);
		}

		@Override
		protected void allocate(SubData<Op> data) {
			super.allocate(data);
			this.bound = data.addPtr("bound", OBJECT_TYPE);
			this.assigner = data.addFuncPtr("assigner_f", VARIABLE_ASSIGNER);
		}

		@Override
		protected DebugTypeInfo createTypeInfo() {
			return externalTypeInfo(0x042a0200 | FldKind.VAR.code());
		}

		@Override
		protected ObjectRefFunc.Signature getSignature() {
			return OBJECT_REF;
		}

		@Override
		protected FuncPtr<ObjectRefFunc> constructorStub() {
			return getGenerator()
					.externalFunction()
					.link("o42a_obj_ref_stub", OBJECT_REF);
		}

	}

	private static final class VarFldAssignerBuilder
			extends AssignerBuilder<VarFldOp> {

		private final VarFld fld;

		VarFldAssignerBuilder(VarFld fld) {
			this.fld = fld;
		}

		@Override
		protected TypeRef getTypeRef() {
			return this.fld.getTypeRef();
		}

		@Override
		protected ObjectIRBody getBodyIR() {
			return this.fld.getBodyIR();
		}

		@Override
		protected VarFldOp op(Code code, ObjOp host) {
			return this.fld.op(code, host);
		}

		@Override
		protected void storeBound(
				Code code,
				VarFldOp fld,
				ObjectIRTypeOp bound) {
			fld.ptr().bound(null, code).store(code, bound, VOLATILE);
		}

		@Override
		protected void storeObject(Block code, VarFldOp fld, ObjectOp object) {
			fld.ptr().object(null, code)
			.store(code, object.toData(null, code), VOLATILE);
		}

	}

}
