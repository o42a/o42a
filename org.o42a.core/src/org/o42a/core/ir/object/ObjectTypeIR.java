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

import static org.o42a.core.ir.object.ObjectIRData.*;
import static org.o42a.core.ir.object.ObjectIRType.OBJECT_TYPE;
import static org.o42a.core.ir.op.ObjectRefFunc.OBJECT_REF;

import java.util.HashMap;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.data.Content;
import org.o42a.codegen.data.SubData;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.ir.object.impl.ObjectTypeIRAncestorFunc;
import org.o42a.core.ir.op.ObjectRefFunc;
import org.o42a.core.ir.op.RelList;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.MemberField;


public final class ObjectTypeIR implements Content<ObjectIRType> {

	private final ObjectIRStruct objectIRStruct;
	private final HashMap<MemberKey, FieldDescIR> fieldDescs =
			new HashMap<MemberKey, FieldDescIR>();
	private ObjectIRType instance;

	ObjectTypeIR(ObjectIRStruct objectIRStruct) {
		this.objectIRStruct = objectIRStruct;
	}

	public final Generator getGenerator() {
		return getObjectIR().getGenerator();
	}

	public final ObjectIR getObjectIR() {
		return this.objectIRStruct.getObjectIR();
	}

	public final ObjectIRType getObjectType() {
		return this.instance;
	}

	public final ObjectIRData getObjectData() {
		return this.instance.data();
	}

	public final ObjectIRType getInstance() {
		return this.instance;
	}

	public final FieldDescIR fieldDescIR(MemberKey key) {

		final FieldDescIR fieldDesc = this.fieldDescs.get(key);

		assert fieldDesc != null :
			"Field descriptor for " + key + " is missing from " + this;

		return fieldDesc;
	}

	@Override
	public void allocated(ObjectIRType instance) {
		this.instance = instance;
	}

	@Override
	public void fill(ObjectIRType instance) {

		final Generator generator = instance.getGenerator();
		final ObjectIRData data = instance.data();

		data.object().setConstant(true).setValue(
				getObjectIR().getMainBodyIR().data(generator).getPointer()
				.relativeTo(data.data(generator).getPointer()));
		data.flags().setConstant(true).setValue(objectFlags());
		data.start().setConstant(true).setValue(
				this.objectIRStruct.data(generator).getPointer().relativeTo(
						data.data(generator).getPointer()));

		fillOwnerTypePointer(data);
		fillAncestor(data);
		instance.mainBodyLayout().setConstant(true).setValue(
				getObjectIR().getMainBodyIR().layout(generator).toBinaryForm());

		getObjectIR().getValueIR().fill(this);
	}

	public ObjectTypeOp op(CodeBuilder builder, Code code) {
		return getInstance()
				.pointer(getGenerator())
				.op(null, code)
				.op(builder, ObjectPrecision.EXACT);
	}

	@Override
	public String toString() {
		return this.objectIRStruct.codeId(getGenerator())
				.sub("object_data").toString();
	}

	void allocate(SubData<?> data) {
		data.addInstance(
				data.getGenerator().id("object_type"),
				OBJECT_TYPE,
				this);

		getObjectData().ascendants().addAll(
				this.objectIRStruct.bodyIRs().values());
		getObjectData().samples().addAll(
				this.objectIRStruct.sampleBodyIRs());
		fillFields();

		getObjectData().ascendants().allocateItems(data);
		getObjectData().samples().allocateItems(data);
		getObjectType().fields().allocateItems(data);
		getObjectType().overriders().allocateItems(data);

		getObjectIR().getValueIR().allocate(this);
	}

	private void fillFields() {

		final ObjectIR objectIR = getObjectIR();
		final RelList<FieldDescIR> fields = getObjectType().fields();

		for (Fld fld : objectIR.getMainBodyIR().getDeclaredFields()) {
			if (fld.isOmitted()) {
				continue;
			}

			final FieldDescIR fieldDescIR = new FieldDescIR(fld);

			fields.add(fieldDescIR);
			if (!fld.getKind().isSynthetic()) {
				this.fieldDescs.put(fld.getField().getKey(), fieldDescIR);
			}
		}

		final RelList<OverriderDescIR> overriders =
				getObjectType().overriders();

		for (Member member : objectIR.getObject().getMembers()) {

			final MemberField field = member.toField();

			if (field == null) {
				continue;
			}
			if (!field.isOverride()) {
				continue;
			}

			final Fld fld = objectIR.findFld(field.getKey());

			if (fld == null || fld.isOmitted() || !fld.isOverrider()) {
				continue;
			}

			overriders.add(new OverriderDescIR(fld));
		}
	}

	private int objectFlags() {

		final Obj object = getObjectIR().getObject();
		int flags = 0;

		if (object.isAbstract()) {
			flags |= OBJ_FLAG_ABSTRACT;
		}
		if (object.isPrototype()) {
			flags |= OBJ_FLAG_PROTOTYPE;
		}
		if (object == object.getContext().getFalse()) {
			flags |= OBJ_FLAG_FALSE;
		} else if (object == object.getContext().getVoid()) {
			flags |= OBJ_FLAG_VOID;
		}

		return flags;
	}

	private void fillOwnerTypePointer(ObjectIRData instance) {

		final Obj owner =
				getObjectIR().getObject().getScope()
				.getEnclosingContainer().toObject();

		if (owner == null) {
			instance.ownerType().setConstant(true).setNull();
			return;
		}

		final ObjectIRType ownerType =
				owner.ir(getGenerator()).getTypeIR().getObjectType();

		instance.ownerType().setConstant(true).setValue(
				ownerType.pointer(instance.getGenerator()));
	}

	private void fillAncestor(ObjectIRData instance) {

		final ObjectIR objectIR = getObjectIR();
		final ObjectBodyIR ancestorBodyIR = objectIR.getAncestorBodyIR();

		if (ancestorBodyIR == null) {
			instance.ancestorType().setConstant(true).setNull();
			instance.ancestorFunc().setConstant(true).setValue(nullObjectRef());
			return;
		}

		instance.ancestorType().setConstant(true).setValue(
				ancestorBodyIR.getAscendant().ir(
						getGenerator()).getTypeIR().getObjectType()
						.pointer(instance.getGenerator()));
		if (getObjectIR().getObject().type().runtimeConstruction().isUsedBy(
				getGenerator())) {
			instance.ancestorFunc().setConstant(true).setValue(
					createAncestorFunc(instance));
		} else {
			instance.ancestorFunc().setConstant(true).setValue(
					stubObjectRef());
		}
	}

	private FuncPtr<ObjectRefFunc> nullObjectRef() {
		return getGenerator().externalFunction(
				"o42a_obj_ref_null",
				OBJECT_REF);
	}

	private FuncPtr<ObjectRefFunc> stubObjectRef() {
		return getGenerator().externalFunction(
				"o42a_obj_ref_stub",
				OBJECT_REF);
	}

	private FuncPtr<ObjectRefFunc> createAncestorFunc(ObjectIRData instance) {
		return getGenerator().newFunction().create(
					this.objectIRStruct
					.codeId(instance.getGenerator())
					.detail("ancestor"),
					OBJECT_REF,
					new ObjectTypeIRAncestorFunc(getObjectIR())).getPointer();
	}

}
