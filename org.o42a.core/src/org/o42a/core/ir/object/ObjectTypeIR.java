/*
    Compiler Core
    Copyright (C) 2010-2013 Ruslan Lopatin

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

import java.util.HashMap;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.data.Content;
import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.ir.object.state.DepIR;
import org.o42a.core.ir.object.state.KeeperIR;
import org.o42a.core.ir.object.type.FieldDescIR;
import org.o42a.core.ir.object.type.OverriderDescIR;
import org.o42a.core.ir.op.RelList;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.object.Obj;
import org.o42a.util.fn.Getter;
import org.o42a.util.string.ID;


public final class ObjectTypeIR implements Content<ObjectIRType> {

	public static final ID OBJECT_TYPE_ID = ID.id("object_type");
	public static final ID OBJECT_DATA_ID = ID.id("object_data");

	private final ObjectIRStruct objectIRStruct;
	private final HashMap<MemberKey, FieldDescIR> fieldDescs =
			new HashMap<>();
	private final ID id;
	private ObjectIRType instance;

	ObjectTypeIR(ObjectIRStruct objectIRStruct) {
		this.objectIRStruct = objectIRStruct;
		this.id = objectIRStruct.getId().sub(OBJECT_DATA_ID);
	}

	public final Generator getGenerator() {
		return getObjectIR().getGenerator();
	}

	public final ID getId() {
		return this.id;
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
		instance.mainBodyLayout().setConstant(true).setLowLevel(true).setValue(
				new Getter<Integer>() {
					@Override
					public Integer get() {
						return getObjectIR()
								.getMainBodyIR()
								.layout(generator)
								.toBinaryForm();
					}
				});
		getObjectIR().getValueIR().setInitialValue(this);
		instance.data().valueType().setConstant(true).setValue(
				getObjectIR()
				.getValueIR()
				.getValueTypeIR()
				.getValueTypeDesc());
	}

	public ObjectTypeOp op(CodeBuilder builder, Code code) {
		return getInstance()
				.pointer(getGenerator())
				.op(null, code)
				.op(builder, ObjectPrecision.EXACT);
	}

	@Override
	public String toString() {
		if (this.id == null) {
			return super.toString();
		}
		return this.id.toString();
	}

	void allocate(SubData<?> data) {
		data.addInstance(OBJECT_TYPE_ID, OBJECT_TYPE, this);

		getObjectData().ascendants().addAll(
				this.objectIRStruct.bodyIRs().values());
		getObjectData().samples().addAll(
				this.objectIRStruct.sampleBodyIRs());

		allocateFieldDecls();
		allocateKeeperDecls();
		allocateDepDecls();
		allocateOverriders();

		getObjectData().ascendants().allocateItems(data);
		getObjectData().samples().allocateItems(data);
		getObjectType().fields().allocateItems(data);
		getObjectType().overriders().allocateItems(data);

		getObjectIR().getObjectValueIR().allocate(this);
	}

	private void allocateFieldDecls() {

		final RelList<FieldDescIR> fields = getObjectType().fields();

		for (Fld<?> fld : getObjectIR().getMainBodyIR().getDeclaredFields()) {
			if (fld.isOmitted()) {
				continue;
			}

			final FieldDescIR fieldDescIR = new FieldDescIR(fld);

			fields.add(fieldDescIR);
			this.fieldDescs.put(fld.getKey(), fieldDescIR);
		}
	}

	private void allocateKeeperDecls() {

		final RelList<FieldDescIR> fields = getObjectType().fields();

		for (KeeperIR<?, ?> keeper
				: getObjectIR().getMainBodyIR().getDeclaredKeepers()) {
			fields.add(new FieldDescIR(keeper));
		}
	}

	private void allocateDepDecls() {

		final RelList<FieldDescIR> fields = getObjectType().fields();

		for (DepIR dep : getObjectIR().getMainBodyIR().getDeclaredDeps()) {
			fields.add(new FieldDescIR(dep));
		}
	}

	private void allocateOverriders() {

		final RelList<OverriderDescIR> overriders =
				getObjectType().overriders();

		for (Member member : getObjectIR().getObject().getMembers()) {

			final MemberField field = member.toField();

			if (field == null) {
				continue;
			}
			if (!field.isOverride()) {
				continue;
			}

			final Fld<?> fld = getObjectIR().findFld(field.getMemberKey());

			if (fld == null || fld.isOmitted() || !fld.isOverrider()) {
				continue;
			}

			overriders.add(new OverriderDescIR(fld));
		}
	}

	private short objectFlags() {

		final Obj object = getObjectIR().getObject();
		short flags = 0;

		if (object.isAbstract()) {
			flags |= OBJ_FLAG_ABSTRACT;
		}
		if (object.isPrototype()) {
			flags |= OBJ_FLAG_PROTOTYPE;
		}
		if (object.isNone()) {
			flags |= OBJ_FLAG_NONE;
		} else if (object.is(object.getContext().getVoid())) {
			flags |= OBJ_FLAG_VOID;
		}

		return flags;
	}

}
