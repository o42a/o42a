/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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

import static org.o42a.codegen.code.op.Atomicity.VOLATILE;
import static org.o42a.core.ir.field.variable.VariableAssignerFunc.VARIABLE_ASSIGNER;
import static org.o42a.core.ir.object.ObjectIRType.OBJECT_TYPE;
import static org.o42a.core.member.MemberName.fieldName;
import static org.o42a.util.string.Capitalization.CASE_SENSITIVE;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.DataRecOp;
import org.o42a.codegen.code.op.FuncOp;
import org.o42a.codegen.code.op.StructRecOp;
import org.o42a.codegen.data.*;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.ir.field.FldKind;
import org.o42a.core.ir.object.*;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.MemberName;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectType;
import org.o42a.core.object.type.Sample;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.value.TypeParameters;
import org.o42a.util.string.ID;
import org.o42a.util.string.Name;


public class VarSte extends Fld implements Content<VarSte.Type> {

	public static final Type VAR_STE = new Type();

	static final ID CAST_TARGET_ID = ID.id("cast_target");

	private static final Name VAR_STE_NAME = CASE_SENSITIVE.name("V");
	private static final ID VAR_STE_ID = VAR_STE_NAME.toID();
	private static final MemberName VAR_STE_MEMBER = fieldName(VAR_STE_NAME);
	private static final ID ASSIGNER_SUFFIX = ID.id("assigner");

	public static MemberKey varSteKey(CompilerContext context) {
		return VAR_STE_MEMBER.key(variableObject(context).getScope());
	}

	private static Obj variableObject(CompilerContext context) {
		return context.getIntrinsics().getVariable();
	}

	private MemberKey key;
	private Obj definedIn;
	private FuncPtr<VariableAssignerFunc> assigner;

	@Override
	public MemberKey getKey() {
		if (this.key != null) {
			return this.key;
		}
		return this.key = VAR_STE_MEMBER.key(
				variableObject(getBodyIR().getAscendant().getContext())
				.getScope());
	}

	@Override
	public final ID getId() {
		return VAR_STE_ID;
	}

	@Override
	public FldKind getKind() {
		return FldKind.VAR_STATE;
	}

	@Override
	public final Type getInstance() {
		return (Type) super.getInstance();
	}

	public final FuncPtr<VariableAssignerFunc> getAssigner() {
		if (this.assigner != null) {
			return this.assigner;
		}

		final FuncPtr<VariableAssignerFunc> reusedAssigner = reusedAssigner();

		if (reusedAssigner != null) {
			return this.assigner = reusedAssigner;
		}

		return this.assigner = getGenerator().newFunction().create(
				getObjectIR().getId().detail(ASSIGNER_SUFFIX),
				VARIABLE_ASSIGNER,
				new VarSteAssignerBuilder(this)).getPointer();
	}

	@Override
	public boolean isOverrider() {

		final Obj object = getObject();

		if (object.is(getDeclaredIn())) {
			// Declaration in variable object.
			return false;
		}

		final Obj definedIn = getDefinedIn();

		if (object.is(definedIn)) {
			// Explicit field override.
			return true;
		}

		final ObjectType definedInType = definedIn.type();

		if (definedInType.getAncestor().getType().type().derivedFrom(
				definedInType)) {
			// Overridden in ancestor.
			return false;
		}

		// Overridden in sample.
		return true;
	}

	@Override
	public Obj getDefinedIn() {
		if (this.definedIn != null) {
			return this.definedIn;
		}
		return this.definedIn = definedIn(getObject());
	}

	public final void declare(ObjectIRBodyData data) {
		allocate(data);
	}

	@Override
	public void allocated(Type instance) {
	}

	@Override
	public void fill(Type instance) {

		final ObjectTypeIR typeIR =
				interfaceType().ir(getGenerator()).getStaticTypeIR();

		instance.bound().setValue(typeIR.getInstance().pointer(getGenerator()));
		getInstance().assigner().setConstant(true).setValue(getAssigner());
	}

	@Override
	public VarSteOp op(Code code, ObjOp host) {
		return new VarSteOp(
				this,
				host,
				host.ptr().field(code, getInstance()));
	}

	@Override
	public String toString() {
		if (getBodyIR() == null) {
			return "assigner";
		}
		return getObject() + " assigner";
	}

	@Override
	protected Content<?> content() {
		return this;
	}

	@Override
	protected boolean mayOmit() {
		return false;
	}

	@Override
	protected Type getType() {
		return VAR_STE;
	}

	final TypeRef interfaceRef() {

		final TypeParameters<?> typeParameters =
				getObject().type().getParameters();

		return typeParameters.getValueType()
				.toLinkType()
				.interfaceRef(typeParameters);
	}

	final Obj interfaceType() {
		return interfaceRef().getType();
	}

	final Obj getObject() {
		return getObjectIR().getObject();
	}

	final ObjectIR getObjectIR() {
		return getBodyIR().getObjectIR();
	}

	private static Obj definedIn(Obj object) {
		if (!object.type().getValueType().isLink()) {
			return null;
		}

		Obj definedIn = null;

		for (Sample sample : object.type().getSamples()) {

			final Obj sampleDefinedIn = definedIn(sample.getObject());

			if (sampleDefinedIn == null) {
				continue;
			}
			if (definedIn == null) {
				definedIn = sampleDefinedIn;
				continue;
			}
			if (!definedIn.is(sampleDefinedIn)) {
				return object;
			}
		}

		final Obj ancestorDefinedIn =
				definedIn(object.type().getAncestor().getType());

		if (ancestorDefinedIn == null) {
			return definedIn != null ? definedIn : object;
		}
		if (definedIn == null || definedIn.is(ancestorDefinedIn)) {
			return ancestorDefinedIn;
		}

		return object;
	}

	private FuncPtr<VariableAssignerFunc> reusedAssigner() {

		final Obj definedIn = getDefinedIn();

		if (getObject().is(definedIn)) {
			return null;
		}

		final VarSte definedFld =
				(VarSte) definedIn.ir(getGenerator()).fld(getKey());

		return definedFld.getAssigner();
	}

	public static final class Op extends Fld.Op<Op> {

		Op(StructWriter<Op> writer) {
			super(writer);
		}

		@Override
		public final Type getType() {
			return (Type) super.getType();
		}

		public final DataRecOp object(ID id, Code code) {
			return ptr(id, code, getType().object());
		}

		public final StructRecOp<ObjectIRTypeOp> bound(ID id, Code code) {
			return ptr(id, code, getType().bound());
		}

		public final FuncOp<VariableAssignerFunc> assigner(ID id, Code code) {
			return func(id, code, getType().assigner());
		}

	}

	public static final class Type extends Fld.Type<Op> {

		private DataRec object;
		private StructRec<ObjectIRTypeOp> bound;
		private FuncRec<VariableAssignerFunc> assigner;

		Type() {
			super(ID.rawId("o42a_ste_var"));
		}

		public final DataRec object() {
			return this.object;
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
			this.object = data.addDataPtr("object");
			this.bound = data.addPtr("bound", OBJECT_TYPE);
			this.assigner =
					data.addFuncPtr("assigner_f", VARIABLE_ASSIGNER)
					.setConstant(true);
		}

		@Override
		protected DebugTypeInfo createTypeInfo() {
			return externalTypeInfo(0x042a0200 | FldKind.VAR_STATE.code());
		}

	}

	private static final class VarSteAssignerBuilder
			extends AssignerBuilder<VarSteOp> {

		private final VarSte fld;

		VarSteAssignerBuilder(VarSte fld) {
			this.fld = fld;
		}

		@Override
		protected TypeRef getInterfaceRef() {
			return this.fld.interfaceRef();
		}

		@Override
		protected ObjectIRBody getBodyIR() {
			return this.fld.getBodyIR();
		}

		@Override
		protected VarSteOp op(Code code, ObjOp host) {
			return this.fld.op(code, host);
		}

		@Override
		protected void storeBound(
				Code code,
				VarSteOp fld,
				ObjectIRTypeOp bound) {
			fld.ptr().bound(null, code).store(code, bound, VOLATILE);
		}

		@Override
		protected void storeObject(
				Block code,
				VarSteOp fld,
				ObjectOp object) {
			fld.assignValue(code, object);
		}

	}

}
