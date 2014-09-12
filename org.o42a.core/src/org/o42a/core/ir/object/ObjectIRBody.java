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
import static org.o42a.core.member.field.FieldUsage.ALL_FIELD_USAGES;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.o42a.analysis.Analyzer;
import org.o42a.codegen.Generator;
import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.ir.object.dep.DepIR;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldAnalysis;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.object.Obj;
import org.o42a.core.object.state.Dep;
import org.o42a.core.object.type.Derivative;


public final class ObjectIRBody {

	private final ObjectIRBodies bodies;
	private final Obj sampleDeclaration;
	private final Obj closestAscendant;

	private final LinkedHashMap<MemberKey, Fld<?>> fields =
			new LinkedHashMap<>();
	private final LinkedHashMap<Dep, DepIR> deps =
			new LinkedHashMap<>();

	ObjectIRBody(ObjectIRBodies bodies) {
		this.bodies = bodies;
		this.sampleDeclaration = bodies.getSampleDeclaration();
		this.closestAscendant = this.sampleDeclaration;
	}

	private ObjectIRBody(
			ObjectIRBodies inheritantBodies,
			Obj sampleDeclaration) {
		this.bodies = inheritantBodies;
		this.sampleDeclaration = sampleDeclaration;
		this.closestAscendant = inheritantBodies.getObject();
	}

	public final Generator getGenerator() {
		return bodies().getGenerator();
	}

	public final ObjectIRBodies bodies() {
		return this.bodies;
	}

	public final ObjectIR getObjectIR() {
		return bodies().getObjectIR();
	}

	public final Obj getSampleDeclaration() {
		return this.sampleDeclaration;
	}

	public final Obj getClosestAscendant() {
		return this.closestAscendant;
	}

	public final boolean isMain() {
		return this == bodies().getMainBodyIR();
	}

	public final ObjectIRBody derive(ObjectIRBodies inheritantBodies) {
		return new ObjectIRBody(inheritantBodies, getSampleDeclaration());
	}

	public final Collection<Fld<?>> getFields() {
		return fields().values();
	}

	public final Collection<DepIR> getDeps() {
		return deps().values();
	}

	public final Fld<?> fld(MemberKey memberKey) {

		final Fld<?> fld = findFld(memberKey);

		assert fld != null :
			fieldNotFound(memberKey);

		return fld;
	}

	public final Fld<?> findFld(MemberKey memberKey) {
		return fields().get(memberKey);
	}

	public final DepIR dep(Dep dep) {

		final DepIR ir = deps().get(dep);

		assert ir != null :
			dep + " not found in " + this;

		return ir;
	}

	final void allocate(SubData<?> data) {
		allocateFields(new ObjectIRBodyData(this, data));
	}

	final void declareFld(Fld<?> fld) {
		this.fields.put(fld.getKey(), fld);
	}

	private HashMap<MemberKey, Fld<?>> fields() {
		ensureFieldsAllocated();
		return this.fields;
	}

	private LinkedHashMap<Dep, DepIR> deps() {
		ensureFieldsAllocated();
		return this.deps;
	}

	private void ensureFieldsAllocated() {
		bodies().getStruct().allocate();
	}

	private final void allocateFields(ObjectIRBodyData data) {
		allocateFieldsDeclaredIn(data, getSampleDeclaration());
		allocateDepsDeclaredIn(data, getSampleDeclaration());
	}

	private void allocateFieldsDeclaredIn(
			ObjectIRBodyData data,
			Obj ascendant) {

		final Generator generator = getGenerator();
		final Obj object = bodies().getObject();

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

}
