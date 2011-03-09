/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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

import static org.o42a.core.ir.object.ObjectDataType.OBJECT_DATA_TYPE;
import static org.o42a.core.ir.object.ObjectOp.anonymousObject;

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
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.local.Dep;
import org.o42a.core.ref.type.TypeRef;


public final class ObjectBodyIR extends Struct<ObjectBodyIR.Op> {

	private static final int KIND_MASK = 3;

	private final ObjectIRStruct objectIRStruct;
	private final Obj ascendant;

	private ObjectMethodsIR methodsIR;

	private final ArrayList<Fld> fieldList = new ArrayList<Fld>();
	private final HashMap<MemberKey, Fld> fieldMap =
		new HashMap<MemberKey, Fld>();
	private final HashMap<Dep, DepIR> deps = new HashMap<Dep, DepIR>();

	private RelPtrRec objectType;
	private RelPtrRec ancestorBody;
	private AnyPtrRec methods;
	private Int32rec flags;

	ObjectBodyIR(ObjectIRStruct objectIRStruct) {
		this.objectIRStruct = objectIRStruct;
		this.ascendant = objectIRStruct.getObject();
	}

	private ObjectBodyIR(ObjectIR inheritantIR, Obj ascendant) {
		this.objectIRStruct = inheritantIR.getStruct();
		this.ascendant = ascendant;
	}

	@Override
	public final Generator getGenerator() {
		return this.objectIRStruct.getGenerator();
	}

	public final ObjectIR getObjectIR() {
		return this.objectIRStruct.getObjectIR();
	}

	public final Obj getAscendant() {
		return this.ascendant;
	}

	public final boolean isMain() {
		return this == this.objectIRStruct.getMainBodyIR();
	}

	public void setKind(Kind kind) {

		final Integer value = this.flags.getValue();

		if (value == null) {
			this.flags.setValue(kind.ordinal());
			return;
		}

		this.flags.setValue((value.intValue() & ~KIND_MASK) | kind.ordinal());
	}

	public Kind getKind() {

		final Integer value = this.flags.getValue();

		if (value == null) {
			return null;
		}

		return Kind.values()[value.intValue() & KIND_MASK];
	}

	public ObjectMethodsIR getMethodsIR() {
		return this.methodsIR;
	}

	public final ObjectBodyIR derive(ObjectIR inheritantIR) {
		return new ObjectBodyIR(inheritantIR, getAscendant());
	}

	public Fld fld(MemberKey memberKey) {

		final Fld fld = this.fieldMap.get(memberKey);

		assert fld != null :
			"Field " + memberKey + " not found in " + this;

		return fld;
	}

	public DepIR dep(Dep dep) {

		final DepIR ir = this.deps.get(dep);

		assert ir != null :
			"Dep " + dep + " not found in " + this;

		return ir;
	}

	@Override
	public Op op(StructWriter writer) {
		return new Op(writer);
	}

	@Override
	public String toString() {
		return print(" body IR");
	}

	@Override
	protected CodeId buildCodeId(CodeIdFactory factory) {
		if (this.objectIRStruct.getObject() == this.ascendant) {
			return this.objectIRStruct.codeId(factory).setLocal(
					factory.id().detail("main_body"));
		}

		final ObjectIR ascendantIR =
			this.ascendant.ir(this.objectIRStruct.getGenerator());

		return this.objectIRStruct.codeId(factory).setLocal(
				factory.id().detail("body").detail(
						ascendantIR.getStruct().codeId(factory)));
	}

	@Override
	protected void allocate(SubData<Op> data) {
		this.objectType = data.addRelPtr("object_type");
		this.ancestorBody = data.addRelPtr("ancestor_body");
		this.methods = data.addPtr("methods");
		this.flags = data.addInt32("flags");
		allocateFields(data);
		allocateDeps(data);
	}

	@Override
	protected void fill() {

		final Generator generator = getGenerator();
		final ObjectType objectType = getObjectIR().getTypeIR().getObjectType();

		this.objectType.setValue(
				objectType.data(generator).getPointer().relativeTo(
						data(generator).getPointer()));

		final ObjectBodyIR ancestorBodyIR = getObjectIR().getAncestorBodyIR();

		if (ancestorBodyIR != null) {
			this.ancestorBody.setValue(
					ancestorBodyIR.data(generator).getPointer().relativeTo(
							data(generator).getPointer()));
		} else {
			this.ancestorBody.setNull();
		}

		this.methods.setValue(
				getMethodsIR().data(generator).getPointer().toAny());
	}

	String print(String suffix) {
		if (isMain()) {
			return this.objectIRStruct.getObject() + suffix;
		}

		return ("(" + this.ascendant + ") "
				+ this.objectIRStruct.getObject() + suffix);
	}

	final List<Fld> getDeclaredFields() {
		return this.fieldList;
	}

	void allocateMetaIR(SubData<?> data) {
		if (isMain()) {
			this.methodsIR = new ObjectMethodsIR(this);
			data.addStruct(
					this.methodsIR.codeId(data.getGenerator()).getLocal(),
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
			if (declared.toField() == null) {
				continue;
			}

			final Field<?> field = object.member(declared.getKey()).toField();
			final FieldIRBase<?> fieldIR = field.ir(generator);
			final Fld fld = fieldIR.allocate(data, this);

			if (fld != null) {
				this.fieldList.add(fld);
				this.fieldMap.put(field.getKey(), fld);
			}
		}
	}

	private final void allocateDeps(SubData<Op> data) {

		final Obj ascendant = getAscendant();

		for (Dep dep : ascendant.getDeps()) {

			final DepIR depIR = new DepIR(getGenerator(), dep);

			depIR.allocate(data);
			this.deps.put(dep, depIR);
		}
	}

	public static final class Op extends StructOp {

		private Op(StructWriter writer) {
			super(writer);
		}

		@Override
		public final ObjectBodyIR getType() {
			return (ObjectBodyIR) super.getType();
		}

		public final Obj getAscendant() {
			return getType().getAscendant();
		}

		public final ObjOp op(
				CodeBuilder builder,
				Obj ascendant,
				ObjectPrecision precision) {
			return new ObjOp(builder, this, ascendant, precision);
		}

		public final ObjectDataType.Op data(Code code) {

			final DataOp<RelOp> bodyHeader = toRel(code);
			final AnyOp dataPtr = bodyHeader.load(code).offset(code, this);

			return dataPtr.to(code, OBJECT_DATA_TYPE);
		}

		public final DataOp<RelOp> ancestorBody(Code code) {
			return writer().relPtr(code, getType().ancestorBody);
		}

		public final ObjectOp ancestor(CodeBuilder builder, Code code) {

			final TypeRef ancestorRef = getAscendant().getAncestor();
			final Obj ancestor;

			if (ancestorRef == null) {
				ancestor = null;
			} else {
				ancestor = ancestorRef.getType();
			}

			final RelOp ancestorBody =
				writer().relPtr(code, getType().ancestorBody).load(code);
			final AnyOp ancestorBodyPtr = ancestorBody.offset(code, this);

			return anonymousObject(
					builder,
					ancestorBodyPtr,
					ancestor);
		}

		public final ObjectMethodsIR.Op methods(Code code) {

			final AnyOp metaPtr =
				writer().ptr(code, getType().methods).load(code);

			return metaPtr.to(code, getType().getMethodsIR());
		}

		@Override
		public String toString() {
			return "*" + getType().codeId(getType().getGenerator());
		}

		final ObjOp op(ObjectDataOp data, Obj ascendant) {
			return new ObjOp(this, ascendant, data);
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
