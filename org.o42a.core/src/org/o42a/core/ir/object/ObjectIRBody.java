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

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.ir.object.ObjectIRType.OBJECT_TYPE;
import static org.o42a.core.member.field.FieldUsage.ALL_FIELD_USAGES;

import java.util.*;

import org.o42a.analysis.Analyzer;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.data.*;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.ir.object.dep.DepIR;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldAnalysis;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.object.Obj;
import org.o42a.core.object.state.Dep;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.value.ValueType;
import org.o42a.util.fn.Getter;
import org.o42a.util.string.ID;


public final class ObjectIRBody extends Struct<ObjectIRBodyOp> {

	static final ID BODY_ID = ID.id("body");

	private static final int KIND_MASK = 3;

	private final ObjectIRStruct objectIRStruct;
	private final Obj ascendant;

	private final ArrayList<Fld<?>> fieldList = new ArrayList<>();
	private final HashMap<MemberKey, Fld<?>> fieldMap = new HashMap<>();
	private final LinkedHashMap<Dep, DepIR> deps = new LinkedHashMap<>();

	private StructRec<ObjectIRTypeOp> declaredIn;
	private RelRec objectType;
	private RelRec ancestorBody;
	private Int32rec flags;

	ObjectIRBody(ObjectIRStruct objectIRStruct) {
		super(buildId(objectIRStruct.getObjectIR()));
		this.objectIRStruct = objectIRStruct;
		this.ascendant = objectIRStruct.getObject();
	}

	private ObjectIRBody(ObjectIR inheritantIR, Obj ascendant) {
		super(buildId(ascendant.ir(inheritantIR.getGenerator())));
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

	public ObjectIRBody getAncestorBodyIR() {
		if (isMain()) {
			return getObjectIR().getAncestorBodyIR();
		}

		final TypeRef ancestorRef =
				getAscendant().type().getAncestor();

		if (ancestorRef == null) {
			return null;
		}

		final Obj ancestor = ancestorRef.getType();

		if (ancestor.is(ancestor.getContext().getVoid())) {
			return null;
		}

		return getObjectIR().bodyIR(ancestor);
	}

	public final StructRec<ObjectIRTypeOp> declaredIn() {
		return this.declaredIn;
	}

	public final RelRec objectType() {
		return this.objectType;
	}

	public final RelRec ancestorBody() {
		return this.ancestorBody;
	}

	public final Int32rec flags() {
		return this.flags;
	}

	public final ObjectIRBody derive(ObjectIR inheritantIR) {
		return new ObjectIRBody(inheritantIR, getAscendant());
	}

	public final Fld<?> fld(MemberKey memberKey) {

		final Fld<?> fld = findFld(memberKey);

		assert fld != null :
			fieldNotFound(memberKey);

		return fld;
	}

	public final Fld<?> findFld(MemberKey memberKey) {
		return this.fieldMap.get(memberKey);
	}

	public final DepIR dep(Dep dep) {

		final DepIR ir = this.deps.get(dep);

		assert ir != null :
			dep + " not found in " + this;

		return ir;
	}

	@Override
	public ObjectIRBodyOp op(StructWriter<ObjectIRBodyOp> writer) {
		return new ObjectIRBodyOp(writer);
	}

	@Override
	protected void allocate(SubData<ObjectIRBodyOp> data) {
		this.declaredIn = data.addPtr("declared_in", OBJECT_TYPE);
		this.objectType = data.addRelPtr("object_type");
		this.ancestorBody = data.addRelPtr("ancestor_body");
		this.flags = data.addInt32("flags");

		final ObjectIRBodyData bodyData = new ObjectIRBodyData(this, data);

		allocateValueBody(bodyData);
		allocateFields(bodyData);
		allocateDeps(bodyData);
	}

	@Override
	protected void fill() {
		this.declaredIn.setConstant(true);

		final Generator generator = getGenerator();
		final ObjectIRType objectType =
				getObjectIR().getTypeIR().getObjectType();

		if (isMain()) {
			this.declaredIn.setValue(objectType.data(generator).getPointer());
		} else {
			this.declaredIn.setValue(
					getAscendant().ir(getGenerator())
					.getTypeIR()
					.getObjectType()
					.data(generator)
					.getPointer());
		}

		this.objectType.setConstant(true).setValue(
				objectType.data(generator).getPointer().relativeTo(
						data(generator).getPointer()));

		final ObjectIRBody ancestorBodyIR = getAncestorBodyIR();

		if (ancestorBodyIR != null) {
			this.ancestorBody.setConstant(true).setValue(
					ancestorBodyIR.data(generator).getPointer().relativeTo(
							data(generator).getPointer()));
		} else {
			this.ancestorBody.setConstant(true).setNull();
		}
	}

	final List<Fld<?>> getDeclaredFields() {
		return this.fieldList;
	}

	final Collection<DepIR> getDeclaredDeps() {
		return this.deps.values();
	}

	final void declareFld(Fld<?> fld) {
		this.fieldList.add(fld);
		this.fieldMap.put(fld.getKey(), fld);
	}

	private static ID buildId(ObjectIR ascendantIR) {
		return ascendantIR.getId().detail(BODY_ID);
	}

	private void allocateValueBody(ObjectIRBodyData data) {

		final Obj ascendant = getAscendant();
		final ValueType<?> valueType = ascendant.type().getValueType();
		final Obj typeObject =
				valueType.typeObject(ascendant.getContext().getIntrinsics());

		if (ascendant.is(typeObject)) {
			getObjectIR().getValueIR().allocateBody(data);
		}
	}

	private final void allocateFields(ObjectIRBodyData data) {

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

			final Field field =
					object.member(declaredField.getMemberKey())
					.toField()
					.field(dummyUser());

			if (!generateField(declaredField)) {
				continue;
			}

			final FieldIRBase fieldIR = field.ir(generator);

			fieldIR.allocate(data);
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

	private void allocateDeps(ObjectIRBodyData data) {

		final Analyzer analyzer = getGenerator().getAnalyzer();
		final Obj ascendant = getAscendant();

		for (Dep dep : ascendant.deps()) {
			if (!dep.exists(analyzer)) {
				continue;
			}

			final DepIR depIR = new DepIR(this, dep);

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

	public enum Kind {

		INHERITED,
		EXPLICIT,
		PROPAGATED,
		MAIN

	}

}
