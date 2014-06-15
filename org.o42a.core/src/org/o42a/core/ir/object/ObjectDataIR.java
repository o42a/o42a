/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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
import static org.o42a.core.ir.object.ObjectIRDesc.OBJECT_DESC_TYPE;
import static org.o42a.core.ir.value.Val.FALSE_VAL;
import static org.o42a.core.ir.value.Val.INDEFINITE_VAL;
import static org.o42a.core.ir.value.Val.VAL_EAGER;

import java.util.HashMap;
import java.util.function.Supplier;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.data.Content;
import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.ir.object.dep.DepIR;
import org.o42a.core.ir.object.type.FieldDescIR;
import org.o42a.core.ir.object.type.OverriderDescIR;
import org.o42a.core.ir.op.RelList;
import org.o42a.core.ir.value.Val;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.object.Obj;
import org.o42a.core.value.ValueKnowledge;
import org.o42a.util.string.ID;


public final class ObjectDataIR implements Content<ObjectIRData> {

	public static final ID OBJECT_DESC_ID = ID.id("object_desc");
	public static final ID OBJECT_DATA_ID = ID.id("object_data");

	private final ObjectIRStruct objectIRStruct;
	private final HashMap<MemberKey, FieldDescIR> fieldDescs = new HashMap<>();
	private final ID id;
	private ObjectIRData instance;
	private ObjectIRDesc desc;
	private Val initialValue;

	ObjectDataIR(ObjectIRStruct objectIRStruct) {
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

	public final ObjectIRData getInstance() {
		return this.instance;
	}

	public final ObjectIRDesc getDesc() {
		return this.desc;
	}

	public final FieldDescIR fieldDescIR(MemberKey key) {

		final FieldDescIR fieldDesc = this.fieldDescs.get(key);

		assert fieldDesc != null :
			"Field descriptor for " + key + " is missing from " + this;

		return fieldDesc;
	}

	@Override
	public void allocated(ObjectIRData instance) {
		this.instance = instance;
	}

	public Val getInitialValue() {
		if (this.initialValue != null) {
			return this.initialValue;
		}

		final Obj object = getObjectIR().getObject();
		final boolean eager = object.value().getStatefulness().isEager();

		if (!eager && !object.type().getValueType().isStateful()) {
			// Stateless object has no initial value.
			return this.initialValue = INDEFINITE_VAL;
		}

		final ValueKnowledge knowledge =
				object.value().getValue().getKnowledge();

		if (!knowledge.isInitiallyKnown()) {
			return this.initialValue = INDEFINITE_VAL;
		}
		if (knowledge.isFalse()) {
			return FALSE_VAL;
		}

		final Val initialValue =
				getObjectIR().getValueIR().initialValue(this);

		if (initialValue == null) {
			return this.initialValue = INDEFINITE_VAL;
		}
		if (!eager) {
			return this.initialValue = initialValue;
		}

		return this.initialValue =
				initialValue.setFlags(initialValue.getFlags() | VAL_EAGER);
	}

	@Override
	public void fill(ObjectIRData instance) {

		final Generator generator = instance.getGenerator();

		instance.object().setConstant(true).setValue(
				getObjectIR().getMainBodyIR().data(generator).getPointer()
				.relativeTo(instance.data(generator).getPointer()));
		instance.flags().setConstant(true).setValue(objectFlags());
		instance.start().setConstant(true).setValue(
				this.objectIRStruct.data(generator).getPointer().relativeTo(
						instance.data(generator).getPointer()));

		instance.value().set(getInitialValue());
		instance.resumeFrom().setNull();
		instance.desc()
		.setConstant(true)
		.setValue(getDesc().pointer(generator));
		instance.valueType().setConstant(true).setValue(
				getObjectIR()
				.getValueIR()
				.getValueTypeIR()
				.getValueTypeDesc());
	}

	public final ObjectDataOp op(CodeBuilder builder, Code code) {
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

		final ObjectIRData instance =
				data.addInstance(OBJECT_DATA_ID, OBJECT_DATA_TYPE, this);

		instance.ascendants().addAll(
				this.objectIRStruct.bodyIRs().values());
		instance.ascendants().allocateItems(data);

		allocateDesc(data);

		getObjectIR().getObjectValueIR().allocate(this);
	}

	private void allocateDesc(SubData<?> data) {

		final ObjectIRDesc instance = this.desc = data.addInstance(
				OBJECT_DESC_ID,
				OBJECT_DESC_TYPE,
				new ObjectIRDescContent(this));

		allocateFieldDecls(instance);
		allocateDepDecls(instance);
		allocateOverriders(instance);
		instance.fields().allocateItems(data);
		instance.overriders().allocateItems(data);
	}

	private void allocateFieldDecls(ObjectIRDesc instance) {

		final RelList<FieldDescIR> fields = instance.fields();

		for (Fld<?> fld : getObjectIR().getMainBodyIR().getDeclaredFields()) {
			if (fld.isOmitted()) {
				continue;
			}

			final FieldDescIR fieldDescIR = new FieldDescIR(fld);

			fields.add(fieldDescIR);
			this.fieldDescs.put(fld.getKey(), fieldDescIR);
		}
	}

	private void allocateDepDecls(ObjectIRDesc instance) {

		final RelList<FieldDescIR> fields = instance.fields();

		for (DepIR dep : getObjectIR().getMainBodyIR().getDeclaredDeps()) {
			if (!dep.isOmitted()) {
				fields.add(new FieldDescIR(dep));
			}
		}
	}

	private void allocateOverriders(ObjectIRDesc instance) {

		final RelList<OverriderDescIR> overriders = instance.overriders();

		for (Member member : getObjectIR().getObject().getMembers()) {

			final MemberField field = member.toField();

			if (field == null) {
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
		if (object.value().getDefinitions().hasInherited()) {
			flags |= OBJ_FLAG_ANCESTOR_DEF;
		}
		if (object.isNone()) {
			flags |= OBJ_FLAG_NONE;
		} else if (object.is(object.getContext().getVoid())) {
			flags |= OBJ_FLAG_VOID;
		}

		return flags;
	}

	private void fillDesc(ObjectIRDesc instance) {

		final Generator generator = instance.getGenerator();

		instance.declaration()
		.setConstant(true)
		.setValue(
				getObjectIR()
				.getSampleDeclaration()
				.ir(generator)
				.getDataIR()
				.getDesc()
				.pointer(generator));
		instance.data()
		.setConstant(true)
		.setValue(getInstance().pointer(generator));
		instance.mainBodyLayout().setConstant(true).setLowLevel(true).setValue(
				new Supplier<Integer>() {
					@Override
					public Integer get() {
						return getObjectIR()
								.getMainBodyIR()
								.layout(generator)
								.toBinaryForm();
					}
				});
	}

	private static final class ObjectIRDescContent
			implements Content<ObjectIRDesc> {

		private final ObjectDataIR dataIR;

		ObjectIRDescContent(ObjectDataIR dataIR) {
			this.dataIR = dataIR;
		}

		@Override
		public void allocated(ObjectIRDesc instance) {
			this.dataIR.desc = instance;
		}

		@Override
		public void fill(ObjectIRDesc instance) {
			this.dataIR.fillDesc(instance);
		}

	}

}
