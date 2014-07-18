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
import static org.o42a.core.ir.object.ObjectIRDesc.OBJECT_DESC_TYPE;
import static org.o42a.core.member.field.FieldUsage.ALL_FIELD_USAGES;

import java.util.*;
import java.util.function.Supplier;

import org.o42a.analysis.Analyzer;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.data.*;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.ir.object.VmtIRChain.Op;
import org.o42a.core.ir.object.dep.DepIR;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldAnalysis;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.object.Obj;
import org.o42a.core.object.state.Dep;
import org.o42a.core.object.type.Derivative;
import org.o42a.util.string.ID;


public final class ObjectIRBody extends Struct<ObjectIRBodyOp> {

	static final ID BODY_ID = ID.id("body");

	private static final int KIND_MASK = 3;

	private final ObjectIRStruct objectIRStruct;
	private final Obj sampleDeclaration;
	private final Obj closestAscendant;

	private final ArrayList<Fld<?>> fieldList = new ArrayList<>();
	private final HashMap<MemberKey, Fld<?>> fieldMap = new HashMap<>();
	private final LinkedHashMap<Dep, DepIR> deps = new LinkedHashMap<>();

	private VmtIR vmtIR;

	private StructRec<ObjectIRDescOp> definedIn;
	private StructRec<VmtIRChain.Op> vmtc;
	private RelRec objectData;
	private Int32rec flags;

	ObjectIRBody(ObjectIRStruct objectIRStruct) {
		super(buildId(objectIRStruct.getObjectIR()));
		this.objectIRStruct = objectIRStruct;
		this.sampleDeclaration = objectIRStruct.getSampleDeclaration();
		this.closestAscendant = this.sampleDeclaration;
	}

	private ObjectIRBody(ObjectIR inheritantIR, Obj sampleDeclaration) {
		super(buildId(sampleDeclaration.ir(inheritantIR.getGenerator())));
		this.objectIRStruct = inheritantIR.getStruct();
		this.sampleDeclaration = sampleDeclaration;
		this.closestAscendant =
				inheritantIR.getSampleDeclaration().is(sampleDeclaration)
				? inheritantIR.getObject()
				: sampleDeclaration;
	}

	public final ObjectIR getObjectIR() {
		return this.objectIRStruct.getObjectIR();
	}

	public final Obj getSampleDeclaration() {
		return this.sampleDeclaration;
	}

	public final Obj getClosestAscendant() {
		return this.closestAscendant;
	}

	public final boolean isMain() {
		return this == this.objectIRStruct.mainBodyIR();
	}

	public final VmtIR getVmtIR() {
		if (this.vmtIR != null) {
			return this.vmtIR;
		}
		return this.vmtIR =
			getGenerator().newGlobal().struct(new VmtIR(this)).getInstance();
	}

	public void setKind(Kind kind) {

		final Supplier<Integer> value = this.flags.getValue();

		if (value == null) {
			this.flags.setConstant(true).setValue(kind.ordinal());
			return;
		}

		this.flags.setValue(
				(value.get().intValue() & ~KIND_MASK) | kind.ordinal());
	}

	public Kind getKind() {

		final Supplier<Integer> value = this.flags.getValue();

		if (value == null) {
			return null;
		}

		return Kind.values()[value.get().intValue() & KIND_MASK];
	}

	public final StructRec<ObjectIRDescOp> definedIn() {
		return this.definedIn;
	}

	public final StructRec<Op> vmtc() {
		return this.vmtc;
	}

	public final RelRec objectData() {
		return this.objectData;
	}

	public final Int32rec flags() {
		return this.flags;
	}

	public final ObjectIRBody derive(ObjectIR inheritantIR) {
		return new ObjectIRBody(inheritantIR, getSampleDeclaration());
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
		this.definedIn = data.addPtr("defined_in", OBJECT_DESC_TYPE);
		this.vmtc = data.addPtr("vmtc", VmtIRChain.VMT_IR_CHAIN_TYPE);
		this.objectData = data.addRelPtr("object_data");
		this.flags = data.addInt32("flags");

		final ObjectIRBodyData bodyData = new ObjectIRBodyData(this, data);

		allocateFields(bodyData);
		allocateDeps(bodyData);
	}

	@Override
	protected void fill() {
		this.definedIn.setConstant(true);

		final Generator generator = getGenerator();
		final ObjectIRDesc objectDesc = getObjectIR().getDataIR().getDesc();

		if (isMain()) {
			this.definedIn.setValue(objectDesc.data(generator).getPointer());
		} else {
			this.definedIn.setValue(
					getSampleDeclaration()
					.ir(getGenerator())
					.getDataIR()
					.getDesc()
					.data(generator)
					.getPointer());
		}

		this.vmtc.setConstant(true)
		.setValue(getVmtIR().terminator().pointer(getGenerator()));

		final ObjectIRData objectData =
				getObjectIR().getDataIR().getInstance();

		this.objectData.setConstant(true).setValue(
				objectData.data(generator)
				.getPointer()
				.relativeTo(data(generator).getPointer()));
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

	private final void allocateFields(ObjectIRBodyData data) {
		allocateFieldsDeclaredIn(data, getSampleDeclaration());
	}

	private void allocateFieldsDeclaredIn(
			ObjectIRBodyData data,
			Obj ascendant) {

		final Generator generator = getGenerator();
		final Obj object = getObjectIR().getObject();

		for (Member declared : ascendant.getMembers()) {
			if (declared.isOverride()) {
				continue;
			}

			final MemberField declaredField = declared.toField();

			if (declaredField == null) {
				continue;
			}
			if (declaredField != declared) {
				// An alias.
				continue;
			}
			if (!generateField(declaredField)) {
				continue;
			}

			final Member member =
					object.member(declaredField.getMemberKey());

			if (member != null) {
				// Member present in object.

				final Field field = member.toField().field(dummyUser());
				final FieldIRBase fieldIR = field.ir(generator);

				fieldIR.allocate(data);
			} else {

				final Obj origin =
						declaredField.getMemberKey().getOrigin().toObject();

				assert origin.assertDerivedFrom(getSampleDeclaration());

				final Field field =
						origin.member(declaredField.getMemberKey())
						.toField()
						.field(dummyUser());
				final FieldIRBase fieldIR = field.ir(generator);

				fieldIR.allocateDummy(data);
			}
		}

		for (Derivative derivative : ascendant.type().allDerivatives()) {
			if (derivative.isSample()) {
				allocateFieldsDeclaredIn(data, derivative.getDerivedObject());
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

	private void allocateDeps(ObjectIRBodyData data) {
		allocateDepsDeclaredIn(data, getSampleDeclaration());
	}

	private void allocateDepsDeclaredIn(
			ObjectIRBodyData data,
			Obj ascendant) {

		final Analyzer analyzer = getGenerator().getAnalyzer();

		for (Dep dep : ascendant.deps()) {
			if (!dep.exists(analyzer)) {
				continue;
			}

			final DepIR depIR = new DepIR(this, dep);

			depIR.allocate(data);
			this.deps.put(dep, depIR);
		}

		for (Derivative derivative : ascendant.type().allDerivatives()) {
			if (derivative.isSample()) {
				allocateDepsDeclaredIn(data, derivative.getDerivedObject());
			}
		}
	}

	private String fieldNotFound(MemberKey memberKey) {

		final StringBuilder out = new StringBuilder();

		out.append("Field `").append(memberKey);
		out.append("` not found in `").append(this);

		final Member member = findMemberIn(getSampleDeclaration(), memberKey);

		if (member == null) {
			return out.append("`: no such member").toString();
		}

		final MemberField field = member.toField();

		if (field == null) {
			return out.append("`: not a field").toString();
		}

		final FieldAnalysis analysis = field.getAnalysis();

		out.append("`: ").append(
				analysis.reasonNotFound(getGenerator().getAnalyzer()));

		return out.toString();
	}

	private Member findMemberIn(Obj descendant, MemberKey memberKey) {

		final Member member = descendant.member(memberKey);

		if (member != null) {
			return member;
		}

		for (Derivative derivative : descendant.type().allDerivatives()) {
			if (derivative.isSample()) {

				final Member found =
						findMemberIn(derivative.getDerivedObject(), memberKey);

				if (found != null) {
					return found;
				}
			}
		}

		return null;
	}

	public enum Kind {

		INHERITED,
		EXPLICIT,
		PROPAGATED,
		MAIN

	}

}
