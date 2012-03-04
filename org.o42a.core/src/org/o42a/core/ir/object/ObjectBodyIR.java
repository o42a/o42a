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

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.ir.object.ObjectIRType.OBJECT_TYPE;
import static org.o42a.core.ir.object.ObjectOp.anonymousObject;
import static org.o42a.core.member.field.FieldUsage.ALL_FIELD_USAGES;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.*;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldAnalysis;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.member.local.Dep;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.util.func.Getter;


public final class ObjectBodyIR extends Struct<ObjectBodyIR.Op> {

	private static final int KIND_MASK = 3;

	private final ObjectIRStruct objectIRStruct;
	private final Obj ascendant;

	private ObjectMethodsIR methodsIR;

	private final ArrayList<Fld> fieldList = new ArrayList<Fld>();
	private final HashMap<MemberKey, Fld> fieldMap =
			new HashMap<MemberKey, Fld>();
	private final HashMap<Dep, DepIR> deps = new HashMap<Dep, DepIR>();

	private RelRec objectType;
	private RelRec ancestorBody;
	private DataRec methods;
	private Int32rec flags;

	ObjectBodyIR(ObjectIRStruct objectIRStruct) {
		this.objectIRStruct = objectIRStruct;
		this.ascendant = objectIRStruct.getObject();
	}

	private ObjectBodyIR(ObjectIR inheritantIR, Obj ascendant) {
		this.objectIRStruct = inheritantIR.getStruct();
		this.ascendant = ascendant;
	}

	public final ObjectIR getObjectIR() {
		return this.objectIRStruct.getObjectIR();
	}

	public final Obj getAscendant() {
		return this.ascendant;
	}

	public final boolean isMain() {
		return this == this.objectIRStruct.mainBodyIR();
	}

	public void setKind(Kind kind) {

		final Getter<Integer> value = this.flags.getValue();

		if (value == null) {
			this.flags.setConstant(true).setValue(kind.ordinal());
			return;
		}

		this.flags.setValue(
				(value.get().intValue() & ~KIND_MASK) | kind.ordinal());
	}

	public Kind getKind() {

		final Getter<Integer> value = this.flags.getValue();

		if (value == null) {
			return null;
		}

		return Kind.values()[value.get().intValue() & KIND_MASK];
	}

	public ObjectMethodsIR getMethodsIR() {
		return this.methodsIR;
	}

	public final RelRec objectType() {
		return this.objectType;
	}

	public final RelRec ancestorBody() {
		return this.ancestorBody;
	}

	public final DataRec methods() {
		return this.methods;
	}

	public final Int32rec flags() {
		return this.flags;
	}

	public final ObjectBodyIR derive(ObjectIR inheritantIR) {
		return new ObjectBodyIR(inheritantIR, getAscendant());
	}

	public final Fld fld(MemberKey memberKey) {

		final Fld fld = findFld(memberKey);

		assert fld != null :
			fieldNotFound(memberKey);

		return fld;
	}

	public final Fld findFld(MemberKey memberKey) {
		return this.fieldMap.get(memberKey);
	}

	public DepIR dep(Dep dep) {

		final DepIR ir = this.deps.get(dep);

		assert ir != null :
			"Dep " + dep + " not found in " + this;

		return ir;
	}

	@Override
	public Op op(StructWriter<Op> writer) {
		return new Op(writer);
	}

	@Override
	protected CodeId buildCodeId(CodeIdFactory factory) {

		final ObjectIR ascendantIR =
				this.ascendant.ir(this.objectIRStruct.getGenerator());

		return ascendantIR.getId().detail("body");
	}

	@Override
	protected void allocate(SubData<Op> data) {
		this.objectType = data.addRelPtr("object_type");
		this.ancestorBody = data.addRelPtr("ancestor_body");
		this.methods = data.addDataPtr("methods");
		this.flags = data.addInt32("flags");
		allocateFields(data);
		allocateDeps(data);
	}

	@Override
	protected void fill() {

		final Generator generator = getGenerator();
		final ObjectIRType objectType =
				getObjectIR().getTypeIR().getObjectType();

		this.objectType.setConstant(true).setValue(
				objectType.data(generator).getPointer().relativeTo(
						data(generator).getPointer()));

		final ObjectBodyIR ancestorBodyIR = getObjectIR().getAncestorBodyIR();

		if (ancestorBodyIR != null) {
			this.ancestorBody.setConstant(true).setValue(
					ancestorBodyIR.data(generator).getPointer().relativeTo(
							data(generator).getPointer()));
		} else {
			this.ancestorBody.setConstant(true).setNull();
		}

		this.methods.setConstant(true).setValue(
				getMethodsIR().data(generator).getPointer().toData());
	}

	final List<Fld> getDeclaredFields() {
		return this.fieldList;
	}

	void allocateMethodsIR(SubData<?> data) {
		if (isMain()) {
			this.methodsIR = new ObjectMethodsIR(this);

			data.addStruct(
					getGenerator().id("methods").detail(
							getAscendant().ir(getGenerator()).getId()),
					this.methodsIR);
			return;
		}
		// reuse meta from original type

		final ObjectIR ascendantIR = getAscendant().ir(getGenerator());

		this.methodsIR = ascendantIR.getMainBodyIR().getMethodsIR();
	}

	private final void allocateFields(SubData<Op> data) {

		final Obj ascendant = getAscendant();
		final Generator generator = getGenerator();
		final Obj object = getObjectIR().getObject();

		for (Member declared : ascendant.getMembers()) {

			final MemberField declaredField = declared.toField();

			if (declaredField == null) {
				continue;
			}
			if (declared.isOverride()) {
				continue;
			}

			final Field<?> field =
					object.member(declaredField.getKey())
					.toField()
					.field(dummyUser());

			if (!generateField(declaredField)) {
				continue;
			}

			final FieldIRBase<?> fieldIR = field.ir(generator);
			final Fld fld = fieldIR.allocate(data, this);

			if (fld != null) {
				this.fieldList.add(fld);
				this.fieldMap.put(field.getKey(), fld);
			}
		}
	}

	private boolean generateField(MemberField declaredField) {

		final Generator generator = getGenerator();
		final FieldAnalysis declarationAnalysis = declaredField.getAnalysis();

		if (!declarationAnalysis.isUsed(
				generator.getAnalyzer(),
				ALL_FIELD_USAGES)) {
			// Field is never used. Skip generation.
			return false;
		}

		return true;
	}

	private final void allocateDeps(SubData<Op> data) {

		final Obj ascendant = getAscendant();

		for (Dep dep : ascendant.getDeps()) {
			if (dep.isDisabled()) {
				continue;
			}

			final DepIR depIR = new DepIR(getGenerator(), dep);

			depIR.allocate(data);
			this.deps.put(dep, depIR);
		}
	}

	private String fieldNotFound(MemberKey memberKey) {

		final StringBuilder out = new StringBuilder();

		out.append("Field ").append(memberKey);
		out.append(" not found in ").append(this);

		final Member member = getAscendant().member(memberKey);

		if (member == null) {
			return out.append(": no such member").toString();
		}

		final MemberField field = member.toField();

		if (field == null) {
			return out.append(": not a field").toString();
		}

		final FieldAnalysis analysis = field.getAnalysis();

		out.append(": ").append(
				analysis.reasonNotFound(getGenerator().getAnalyzer()));

		return out.toString();
	}

	public static final class Op extends StructOp<Op> {

		private Op(StructWriter<Op> writer) {
			super(writer);
		}

		@Override
		public final ObjectBodyIR getType() {
			return (ObjectBodyIR) super.getType();
		}

		public final Obj getAscendant() {
			return getType().getAscendant();
		}

		public final RelRecOp objectType(Code code) {
			return relPtr(null, code, getType().objectType());
		}

		public final RelRecOp ancestorBody(Code code) {
			return relPtr(null, code, getType().ancestorBody());
		}

		public final DataRecOp methods(Code code) {
			return ptr(null, code, getType().methods());
		}

		public final Int32recOp flags(Code code) {
			return int32(null, code, getType().flags());
		}

		public final <O extends Fld.Op<O>> O field(
				Code code,
				Fld.Type<O> instance) {
			return struct(null, code, instance);
		}

		public final DepIR.Op dep(Code code, DepIR.Type instance) {
			return struct(null, code, instance);
		}

		public final ObjOp op(
				CodeBuilder builder,
				Obj ascendant,
				ObjectPrecision precision) {
			return op(builder, null, ascendant, precision);
		}

		public final ObjectIRType.Op loadObjectType(Code code) {
			return objectType(code)
					.load(null, code)
					.offset(
							code.id("object_type").type(code.id("any")),
							code,
							this)
					.to(code.id("object_type"), code, OBJECT_TYPE);
		}

		public final ObjectOp loadAncestor(CodeBuilder builder, Code code) {

			final TypeRef ancestorRef = getAscendant().type().getAncestor();
			final Obj ancestor;

			if (ancestorRef == null) {
				ancestor = builder.getContext().getVoid();
			} else {
				ancestor = ancestorRef.typeObject(dummyUser());
			}

			final AnyOp ancestorBodyPtr =
					ancestorBody(code)
					.load(null, code)
					.offset(null, code, this);

			return anonymousObject(
					builder,
					ancestorBodyPtr.toData(null, code),
					ancestor);
		}

		public final ObjectMethodsIR.Op loadMethods(Code code) {

			final DataOp methodsPtr = methods(code).load(null, code);

			return methodsPtr.to(null, code, getType().getMethodsIR());
		}

		@Override
		public String toString() {
			return "*" + getType().codeId(getType().getGenerator());
		}

		@Override
		protected CodeId fieldId(Code code, CodeId local) {
			return code.id("body").setLocal(local);
		}

		final ObjOp op(
				CodeBuilder builder,
				ObjectIR objectIR,
				Obj ascendant,
				ObjectPrecision precision) {
			return new ObjOp(
					builder,
					objectIR != null ? objectIR : getType().getObjectIR(),
					this,
					ascendant,
					precision);
		}

		final ObjOp op(ObjectIR objectIR, ObjectTypeOp data, Obj ascendant) {
			return new ObjOp(
					objectIR != null ? objectIR : getType().getObjectIR(),
					this,
					ascendant,
					data);
		}

		final ObjOp op(CodeBuilder builder, ObjectIR objectIR) {
			return new ObjOp(builder, objectIR, this);
		}

		FldOp declaredField(Code code, ObjOp host, MemberKey memberKey) {

			final Fld declared = getType().fld(memberKey);

			assert declared != null :
				memberKey + " is not declared in " + this;

			return declared.op(code, host);
		}

	}

	public enum Kind {

		INHERITED,
		EXPLICIT,
		PROPAGATED,
		MAIN

	}

}
