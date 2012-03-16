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

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.ir.field.variable.VariableAssignerFunc.VARIABLE_ASSIGNER;
import static org.o42a.core.ir.object.ObjectIRType.OBJECT_TYPE;
import static org.o42a.core.ir.object.ObjectOp.anonymousObject;
import static org.o42a.core.ir.object.ObjectPrecision.COMPATIBLE;
import static org.o42a.core.ir.object.ObjectPrecision.EXACT;
import static org.o42a.core.ir.op.ObjectRefFunc.OBJECT_REF;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.FuncOp;
import org.o42a.codegen.code.op.StructRecOp;
import org.o42a.codegen.data.FuncRec;
import org.o42a.codegen.data.StructRec;
import org.o42a.codegen.data.SubData;
import org.o42a.core.artifact.link.Link;
import org.o42a.core.ir.field.FldKind;
import org.o42a.core.ir.field.RefFld;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.op.ObjectRefFunc.ObjectRef;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.ref.type.TypeRelation;


public class VarFld extends RefFld<ObjectRefFunc> {

	public static final Type VAR_FLD = new Type();

	private FuncPtr<VariableAssignerFunc> assigner;

	public VarFld(ObjectBodyIR bodyIR, Field<Link> field) {
		super(bodyIR, field);
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
				getField().ir(getGenerator()).getId().detail("assigner"),
				VARIABLE_ASSIGNER,
				new AssignerBuilder()).getPointer();
	}

	@Override
	protected void fill() {
		super.fill();

		final Obj type =
				getField().getArtifact().getTypeRef().typeObject(dummyUser());
		final ObjectTypeIR typeIR = type.ir(getGenerator()).getStaticTypeIR();

		getInstance().bound().setValue(
				typeIR.getInstance().pointer(getGenerator()));
		getInstance().assigner().setConstant(true).setValue(this.assigner);
	}

	@Override
	protected Type getType() {
		return VAR_FLD;
	}

	private FuncPtr<VariableAssignerFunc> reusedAssigner() {
		for (Field<?> overridden : getField().getOverridden()) {

			final TypeRelation relation =
					overridden.getArtifact().getTypeRef().relationTo(
							getField().getArtifact().getTypeRef(),
							false);

			if (relation == TypeRelation.SAME) {
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

	private void buildAssigner(Function<VariableAssignerFunc> assigner) {

		final Block failure = assigner.addBlock("failure");
		final ObjBuilder builder = new ObjBuilder(
				assigner,
				failure.head(),
				getBodyIR(),
				getBodyIR().getAscendant(),
				getBodyIR().getObjectIR().isExact() ? EXACT : COMPATIBLE);
		final CodeDirs dirs =
				builder.falseWhenUnknown(assigner, failure.head());

		final VarFldOp fld = op(assigner, builder.host());
		final TypeRef typeRef = getField().getArtifact().getTypeRef();
		final Obj typeObject = typeRef.typeObject(dummyUser());
		final RefOp boundRef = typeRef.op(dirs, builder.host());
		final ObjectTypeOp bound =
				boundRef.target(dirs).materialize(dirs).objectType(assigner);

		fld.ptr().bound(null, assigner).store(assigner, bound.ptr());

		final ObjectOp valueObject = anonymousObject(
				builder,
				assigner.arg(assigner, VARIABLE_ASSIGNER.value()),
				builder.getContext().getVoid());

		final ObjectOp castObject = valueObject.dynamicCast(
				assigner.id("cast_target"),
				dirs,
				typeObject.ir(getGenerator())
				.getTypeIR().op(builder, assigner),
				typeObject,
				true);

		fld.ptr().object(null, assigner).store(
				assigner,
				castObject.toData(assigner));
		assigner.bool(true).returnValue(assigner);

		if (failure.exists()) {

			final ObjectIR falseIR =
					builder.getContext().getFalse().ir(getGenerator());

			fld.ptr().object(null, failure).store(
					failure,
					falseIR.op(builder, failure).toData(failure));
			fld.ptr().bound(null, failure).store(
					failure,
					falseIR.getTypeIR()
					.getInstance()
					.pointer(getGenerator())
					.op(null, failure));
			failure.bool(false).returnValue(failure);
		}
	}

	public static final class Op extends RefFld.Op<Op, ObjectRefFunc> {

		private Op(StructWriter<Op> writer) {
			super(writer);
		}

		@Override
		public final Type getType() {
			return (Type) super.getType();
		}

		public final StructRecOp<ObjectIRType.Op> bound(CodeId id, Code code) {
			return ptr(id, code, getType().bound());
		}

		public final FuncOp<VariableAssignerFunc> assigner(
				CodeId id,
				Code code) {
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

		private StructRec<ObjectIRType.Op> bound;
		private FuncRec<VariableAssignerFunc> assigner;

		private Type() {
		}

		public final StructRec<ObjectIRType.Op> bound() {
			return this.bound;
		}

		public final FuncRec<VariableAssignerFunc> assigner() {
			return this.assigner;
		}

		@Override
		public void allocate(SubData<Op> data) {
			super.allocate(data);
			this.bound = data.addPtr("bound", OBJECT_TYPE);
			this.assigner = data.addFuncPtr("assigner_f", VARIABLE_ASSIGNER);
		}

		@Override
		public Op op(StructWriter<Op> writer) {
			return new Op(writer);
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.id("VarFld");
		}

		@Override
		protected ObjectRef getSignature() {
			return OBJECT_REF;
		}

		@Override
		protected FuncPtr<ObjectRefFunc> constructorStub() {
			return getGenerator()
					.externalFunction()
					.link("o42a_obj_ref_stub", OBJECT_REF);
		}

	}

	private final class AssignerBuilder
			implements FunctionBuilder<VariableAssignerFunc> {

		@Override
		public void build(Function<VariableAssignerFunc> function) {
			buildAssigner(function);
		}

	}

}
