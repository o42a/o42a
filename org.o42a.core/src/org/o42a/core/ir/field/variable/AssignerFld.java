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
import static org.o42a.core.member.MemberId.fieldName;
import static org.o42a.util.string.Capitalization.CASE_SENSITIVE;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.backend.StructWriter;
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
import org.o42a.core.object.link.LinkValueStruct;
import org.o42a.core.object.type.Sample;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerContext;


public class AssignerFld extends Fld implements Content<AssignerFld.Type> {

	public static final Type ASSIGNER_FLD = new Type();
	private static final MemberName ASSIGNER_MEMBER =
			fieldName(CASE_SENSITIVE.canonicalName("AS"));

	public static MemberKey assignerKey(CompilerContext context) {
		return ASSIGNER_MEMBER.key(variableObject(context).getScope());
	}

	private static Obj variableObject(CompilerContext context) {
		return context.getIntrinsics().getVariable();
	}

	private MemberKey key;
	private CodeId id;
	private Obj definedIn;
	private FuncPtr<VariableAssignerFunc> assigner;

	public AssignerFld(ObjectBodyIR bodyIR) {
		super(bodyIR);
	}

	@Override
	public MemberKey getKey() {
		if (this.key != null) {
			return this.key;
		}
		return this.key = ASSIGNER_MEMBER.key(getDeclaredIn().getScope());
	}

	@Override
	public CodeId getId() {
		if (this.id != null) {
			return this.id;
		}
		return this.id = getGenerator().id("AS");
	}

	@Override
	public FldKind getKind() {
		return FldKind.ASSIGNER;
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
				getObjectIR().getId().detail("assigner"),
				VARIABLE_ASSIGNER,
				new AssignerBuilder(this)).getPointer();
	}

	@Override
	public boolean isOverrider() {

		final Obj object = getObject();

		if (object == getDeclaredIn()) {
			// Declaration in variable object.
			return false;
		}

		final Obj definedIn = getDefinedIn();

		if (getObject() == definedIn) {
			// Explicit field override.
			return true;
		}

		final ObjectType definedInType = definedIn.type();

		if (definedInType.getAncestor().type().derivedFrom(
				definedInType)) {
			// Overridden in ancestor.
			return false;
		}

		// Overridden in sample.
		return true;
	}

	@Override
	public Obj getDeclaredIn() {
		return variableObject(getBodyIR().getAscendant().getContext());
	}

	@Override
	public Obj getDefinedIn() {
		if (this.definedIn != null) {
			return this.definedIn;
		}
		return this.definedIn = definedIn(getObject());
	}

	public final void declare(SubData<?> data) {
		allocate(data);
	}

	@Override
	public void allocated(Type instance) {
	}

	@Override
	public void fill(Type instance) {

		final Obj type = linkStruct().getTypeRef().typeObject();
		final ObjectTypeIR typeIR = type.ir(getGenerator()).getStaticTypeIR();

		instance.bound().setValue(typeIR.getInstance().pointer(getGenerator()));
		getInstance().assigner().setConstant(true).setValue(getAssigner());
	}

	@Override
	public AssignerFldOp op(Code code, ObjOp host) {
		return new AssignerFldOp(
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
		return ASSIGNER_FLD;
	}

	final LinkValueStruct linkStruct() {
		return getObject().value().getValueStruct().toLinkStruct();
	}

	final Obj getObject() {
		return getObjectIR().getObject();
	}

	final ObjectIR getObjectIR() {
		return getBodyIR().getObjectIR();
	}

	private static Obj definedIn(Obj object) {

		final LinkValueStruct linkStruct =
				object.value().getValueStruct().toLinkStruct();

		if (linkStruct == null) {
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
			if (definedIn != sampleDefinedIn) {
				return object;
			}
		}

		final Obj ancestorDefinedIn =
				definedIn(object.type().getAncestor().typeObject());

		if (ancestorDefinedIn == null) {
			return definedIn != null ? definedIn : object;
		}
		if (definedIn == null || definedIn == ancestorDefinedIn) {
			return ancestorDefinedIn;
		}

		return object;
	}

	private FuncPtr<VariableAssignerFunc> reusedAssigner() {

		final Obj definedIn = getDefinedIn();

		if (definedIn == getObject()) {
			return null;
		}

		final AssignerFld definedFld =
				(AssignerFld) definedIn.ir(getGenerator()).fld(getKey());

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

		public final StructRecOp<ObjectIRType.Op> bound(CodeId id, Code code) {
			return ptr(id, code, getType().bound());
		}

		public final FuncOp<VariableAssignerFunc> assigner(
				CodeId id,
				Code code) {
			return func(id, code, getType().assigner());
		}

	}

	public static final class Type extends Fld.Type<Op> {

		private StructRec<ObjectIRType.Op> bound;
		private FuncRec<VariableAssignerFunc> assigner;

		Type() {
		}

		public final StructRec<ObjectIRType.Op> bound() {
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
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.rawId("o42a_fld_assigner");
		}

		@Override
		protected void allocate(SubData<Op> data) {
			this.bound = data.addPtr("bound", OBJECT_TYPE);
			this.assigner =
					data.addFuncPtr("assigner_f", VARIABLE_ASSIGNER)
					.setConstant(true);
		}

		@Override
		protected DebugTypeInfo createTypeInfo() {
			return externalTypeInfo(0x042a0200 | FldKind.ASSIGNER.code());
		}

	}

	private static final class AssignerBuilder
			extends AbstractAssignerBuilder<AssignerFldOp> {

		private final AssignerFld fld;

		AssignerBuilder(AssignerFld fld) {
			this.fld = fld;
		}

		@Override
		protected TypeRef getTypeRef() {
			return this.fld.linkStruct().getTypeRef();
		}

		@Override
		protected ObjectBodyIR getBodyIR() {
			return this.fld.getBodyIR();
		}

		@Override
		protected AssignerFldOp op(Code code, ObjOp host) {
			return this.fld.op(code, host);
		}

		@Override
		protected void storeBound(
				Code code,
				AssignerFldOp fld,
				ObjectIRType.Op bound) {
			fld.ptr().bound(null, code).store(code, bound, VOLATILE);
		}

		@Override
		protected void storeObject(
				Block code,
				AssignerFldOp fld,
				ObjectOp object) {
			fld.assignValue(code, object);
		}

	}

}
