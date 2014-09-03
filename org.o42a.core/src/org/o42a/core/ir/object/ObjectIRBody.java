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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.o42a.codegen.Generator;
import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldAnalysis;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.object.Obj;
import org.o42a.core.object.type.Derivative;


public final class ObjectIRBody {

	private final ObjectIRStruct objectIRStruct;
	private final Obj sampleDeclaration;
	private final Obj closestAscendant;

	private final ArrayList<Fld<?>> fieldList = new ArrayList<>();
	private final HashMap<MemberKey, Fld<?>> fieldMap = new HashMap<>();

	ObjectIRBody(ObjectIRStruct objectIRStruct) {
		this.objectIRStruct = objectIRStruct;
		this.sampleDeclaration = objectIRStruct.getSampleDeclaration();
		this.closestAscendant = this.sampleDeclaration;
	}

	private ObjectIRBody(ObjectIR inheritantIR, Obj sampleDeclaration) {
		this.objectIRStruct = inheritantIR.getStruct();
		this.sampleDeclaration = sampleDeclaration;
		this.closestAscendant =
				inheritantIR.getSampleDeclaration().is(sampleDeclaration)
				? inheritantIR.getObject()
				: sampleDeclaration;
	}

	public final Generator getGenerator() {
		return this.objectIRStruct.getGenerator();
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

	public final ObjectIRBody derive(ObjectIR inheritantIR) {
		return new ObjectIRBody(inheritantIR, getSampleDeclaration());
	}

	public final List<Fld<?>> getDeclaredFields() {
		return this.fieldList;
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

	final void allocate(SubData<?> data) {

		final ObjectIRBodyData bodyData = new ObjectIRBodyData(this, data);

		allocateFields(bodyData);
	}

	final void declareFld(Fld<?> fld) {
		this.fieldList.add(fld);
		this.fieldMap.put(fld.getKey(), fld);
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
