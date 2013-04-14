/*
    Compiler Core
    Copyright (C) 2012,2013 Ruslan Lopatin

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

import static org.o42a.core.member.MemberName.fieldName;
import static org.o42a.util.string.Capitalization.CASE_SENSITIVE;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.DataRecOp;
import org.o42a.codegen.data.Content;
import org.o42a.codegen.data.DataRec;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.ir.field.FldKind;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectIRBodyData;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.MemberName;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectType;
import org.o42a.core.object.type.Sample;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.Value;
import org.o42a.core.value.link.KnownLink;
import org.o42a.core.value.link.LinkValueType;
import org.o42a.util.string.ID;
import org.o42a.util.string.Name;


public class VarSte extends Fld<VarSte.Op> implements Content<VarSte.Type> {

	public static final Type VAR_STE = new Type();

	private static final Name VAR_STE_NAME = CASE_SENSITIVE.name("V");
	private static final ID VAR_STE_ID = VAR_STE_NAME.toID();
	private static final MemberName VAR_STE_MEMBER = fieldName(VAR_STE_NAME);

	public static MemberKey varSteKey(CompilerContext context) {
		return VAR_STE_MEMBER.key(variableObject(context).getScope());
	}

	private static Obj variableObject(CompilerContext context) {
		return context.getIntrinsics().getVariable();
	}

	private MemberKey key;
	private Obj definedIn;

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

		final Value<?> value = getObject().value().getValue();

		if (!value.getKnowledge().isInitiallyKnown()) {
			instance.object().setNull();
		} else if (value.getKnowledge().isFalse()) {

			final ObjectIR noneIR =
					getObject().getContext().getNone().ir(getGenerator());

			instance.object().setValue(
					noneIR.getMainBodyIR().pointer(getGenerator()).toData());
		} else {


			final LinkValueType linkType =
					getObject().type().getValueType().toLinkType();
			final KnownLink link =
					linkType.cast(value).getCompilerValue();
			final Obj target = link.getTarget().getWrapped();

			if (target.getConstructionMode().isRuntime()) {
				instance.object().setNull();
			} else {

				final ObjectIR targetIR = target.ir(getGenerator());

				instance.object().setValue(
						targetIR.getMainBodyIR()
						.pointer(getGenerator())
						.toData());
			}
		}

	}

	@Override
	public String toString() {
		if (getBodyIR() == null) {
			return "assigner";
		}
		return getObject() + " assigner";
	}

	@Override
	protected Content<VarSte.Type> content() {
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

	@Override
	protected VarSteOp op(Code code, ObjOp host, Op ptr) {
		return new VarSteOp(this, host, ptr);
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

	}

	public static final class Type extends Fld.Type<Op> {

		private DataRec object;

		Type() {
			super(ID.rawId("o42a_ste_var"));
		}

		public final DataRec object() {
			return this.object;
		}

		@Override
		public Op op(StructWriter<Op> writer) {
			return new Op(writer);
		}

		@Override
		protected void allocate(SubData<Op> data) {
			this.object = data.addDataPtr("object");
		}

		@Override
		protected DebugTypeInfo createTypeInfo() {
			return externalTypeInfo(0x042a0200 | FldKind.VAR_STATE.code());
		}

	}

}
