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
import static org.o42a.util.collect.Iterators.combineIterators;
import static org.o42a.util.fn.Init.init;

import java.util.*;

import org.o42a.analysis.Analyzer;
import org.o42a.codegen.Generator;
import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.ir.field.FldIR;
import org.o42a.core.ir.field.dep.DepIR;
import org.o42a.core.ir.field.inst.InstFld;
import org.o42a.core.ir.field.inst.InstFldKind;
import org.o42a.core.ir.field.local.LocalIR;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldAnalysis;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.member.local.MemberLocal;
import org.o42a.core.object.Obj;
import org.o42a.core.object.state.Dep;
import org.o42a.core.object.type.Derivative;
import org.o42a.util.fn.Init;


public final class ObjectIRBody {

	private final ObjectIRBodies bodies;
	private final Obj sampleDeclaration;

	private final Init<EnumMap<InstFldKind, InstFld<?, ?>>> instFields =
			init(this::createInstFields);
	private final Init<LinkedHashMap<MemberKey, FldIR<?, ?>>> fields =
			init(this::createFields);
	private final Init<LinkedHashMap<Dep, DepIR>> deps =
			init(this::createDeps);

	ObjectIRBody(ObjectIRBodies bodies) {
		this.bodies = bodies;
		this.sampleDeclaration = bodies.getSampleDeclaration();
	}

	private ObjectIRBody(
			ObjectIRBodies inheritantBodies,
			Obj sampleDeclaration) {
		this.bodies = inheritantBodies;
		this.sampleDeclaration = sampleDeclaration;
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

	public final boolean isMain() {
		return this == bodies().getMainBodyIR();
	}

	public final ObjectIRBody derive(ObjectIRBodies inheritantBodies) {
		return new ObjectIRBody(inheritantBodies, getSampleDeclaration());
	}

	public final Iterable<FldIR<?, ?>> allFields() {
		return () -> combineIterators(
				instFields().values().iterator(),
				combineIterators(
						fields().values().iterator(),
						deps().values().iterator()));
	}

	public final Iterable<FldIR<?, ?>> vmtFields() {
		return fields().values();
	}

	public final Iterable<DepIR> allDeps() {
		return deps().values();
	}

	public final Fld<?, ?> fld(MemberKey memberKey) {

		final Fld<?, ?> fld = findFld(memberKey);

		assert fld != null :
			fieldNotFound(memberKey);

		return fld;
	}

	public final Fld<?, ?> findFld(MemberKey memberKey) {

		final FldIR<?, ?> fldIR = fields().get(memberKey);

		return fldIR != null ? fldIR.toFld() : null;
	}

	public final DepIR dep(Dep dep) {

		final DepIR ir = deps().get(dep);

		assert ir != null :
			dep + " not found in " + this;

		return ir;
	}

	public final LocalIR local(MemberKey memberKey) {

		final FldIR<?, ?> fld = fields().get(memberKey);

		assert fld != null :
			memberKey + " not found in " + this;

		final LocalIR local = fld.toLocal();

		assert local != null :
			memberKey + " is not a local field";

		return local;
	}

	@Override
	public String toString() {
		if (this.sampleDeclaration == null) {
			return super.toString();
		}
		return getObjectIR().getObject() + "@@(" + this.sampleDeclaration + ")";
	}

	final EnumMap<InstFldKind, InstFld<?, ?>> instFields() {
		return this.instFields.get();
	}

	final void allocate(SubData<?> data) {
		allocateInstFields(data);
		allocateFields(data);
		allocateDeps(data);
	}

	private final LinkedHashMap<MemberKey, FldIR<?, ?>> fields() {
		return this.fields.get();
	}

	private final LinkedHashMap<Dep, DepIR> deps() {
		return this.deps.get();
	}

	private void allocateInstFields(SubData<?> data) {
		for (InstFld<?, ?> fld : instFields().values()) {
			fld.allocate(data);
		}
	}

	private void allocateFields(SubData<?> data) {
		for (FldIR<?, ?> fld : fields().values()) {
			fld.allocate(data);
		}
	}

	private void allocateDeps(SubData<?> data) {
		for (DepIR dep : deps().values()) {
			dep.allocate(data);
		}
	}

	private EnumMap<InstFldKind, InstFld<?, ?>> createInstFields() {

		final EnumMap<InstFldKind, InstFld<?, ?>> fields =
				new EnumMap<>(InstFldKind.class);

		if (!isMain()) {
			deriveInstFields(fields);
		} else {
			createInstFieldsDeclaredIn(
					fields,
					instFldKindsToDeclare(),
					getSampleDeclaration());
		}

		return fields;
	}

	private void deriveInstFields(EnumMap<InstFldKind, InstFld<?, ?>> fields) {
		for (InstFld<?, ?> fld
				: getSampleDeclaration()
				.ir(getGenerator())
				.typeBodies()
				.getMainBodyIR()
				.instFields()
				.values()) {
			fields.put(fld.getInstFldKind(), fld.derive(this));
		}
	}

	private EnumSet<InstFldKind> instFldKindsToDeclare() {

		final EnumSet<InstFldKind> kinds = EnumSet.allOf(InstFldKind.class);

		for (ObjectIRBody body : bodies()) {
			if (body.isMain()) {
				break;
			}
			// An instance field can be declared at most once per object
			kinds.removeAll(body.instFields().keySet());
		}

		return kinds;
	}

	private void createInstFieldsDeclaredIn(
			EnumMap<InstFldKind, InstFld<?, ?>> fields,
			EnumSet<InstFldKind> kinds,
			Obj ascendant) {

		final Iterator<InstFldKind> it = kinds.iterator();

		while (it.hasNext()) {

			final InstFldKind kind = it.next();
			final InstFld<?, ?> fld = kind.declare(this, ascendant);

			if (fld != null) {
				fields.put(kind, fld);
				it.remove();
			}
		}
		if (kinds.isEmpty()) {
			return;
		}

		for (Derivative derivative : ascendant.type().allDerivatives()) {
			if (!derivative.isSample()) {
				continue;
			}
			createInstFieldsDeclaredIn(
					fields,
					kinds,
					derivative.getDerivedObject());
			if (kinds.isEmpty()) {
				break;
			}
		}
	}

	private LinkedHashMap<MemberKey, FldIR<?, ?>> createFields() {

		final LinkedHashMap<MemberKey, FldIR<?, ?>> fields =
				new LinkedHashMap<>();

		createFieldsDeclaredIn(fields, getSampleDeclaration());

		return fields;
	}

	private void createFieldsDeclaredIn(
			LinkedHashMap<MemberKey, FldIR<?, ?>> fields,
			Obj ascendant) {
		for (Member declared : ascendant.getMembers()) {
			if (declared.isOverride()) {
				continue;
			}
			if (createField(fields, declared)) {
				continue;
			}
			if (createLocal(fields, declared)) {
				continue;
			}

			final MemberField declaredField = declared.toField();

			if (declaredField != null) {
				if (declaredField != declared) {
					// An alias.
					continue;
				}
				if (!generateField(declaredField)) {
					continue;
				}
				createField(fields, declaredField);
				continue;
			}
		}

		for (Derivative derivative : ascendant.type().allDerivatives()) {
			if (derivative.isSample()) {
				createFieldsDeclaredIn(fields, derivative.getDerivedObject());
			}
		}
	}

	private boolean createField(
			LinkedHashMap<MemberKey, FldIR<?, ?>> fields,
			Member declared) {

		final MemberField declaredField = declared.toField();

		if (declaredField == null) {
			return false;
		}
		if (declaredField != declared) {
			// An alias.
			return true;
		}
		if (!generateField(declaredField)) {
			return true;
		}

		final Obj object = bodies().getObject();
		final Generator generator = getGenerator();
		final Member member =
				object.member(declaredField.getMemberKey());

		if (member != null) {
			// The member is present in object.

			final Field field = member.toField().field(dummyUser());
			final FieldIRBase fieldIR = field.ir(generator);
			final Fld<?, ?> fld = fieldIR.declare(this);

			if (fld != null) {
				fields.put(fld.getKey(), fld);
			}

			return true;
		}

		final Obj origin =
				declaredField.getMemberKey().getOrigin().toObject();

		assert origin.assertDerivedFrom(getSampleDeclaration());

		final Field field =
				origin.member(declaredField.getMemberKey())
				.toField()
				.field(dummyUser());
		final FieldIRBase fieldIR = field.ir(generator);
		final Fld<?, ?> fld = fieldIR.declareDummy(this);

		if (fld != null) {
			fields.put(fld.getKey(), fld);
		}

		return true;
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

	private boolean createLocal(
			LinkedHashMap<MemberKey, FldIR<?, ?>> fields,
			Member declared) {

		final MemberLocal local = declared.toLocal();

		if (local == null) {
			return false;
		}

		if (!local.getLocal().isOmitted(getGenerator())) {
			fields.put(local.getMemberKey(), new LocalIR(this, local));
		}

		return true;
	}

	private LinkedHashMap<Dep, DepIR> createDeps() {

		final LinkedHashMap<Dep, DepIR> deps = new LinkedHashMap<>();

		createDepsDeclaredIn(deps, getSampleDeclaration());

		return deps;
	}

	private void createDepsDeclaredIn(
			LinkedHashMap<Dep, DepIR> deps,
			Obj ascendant) {

		final Analyzer analyzer = getGenerator().getAnalyzer();

		for (Dep dep : ascendant.deps()) {
			if (!dep.exists(analyzer)) {
				continue;
			}

			deps.put(dep, new DepIR(this, dep));
		}

		for (Derivative derivative : ascendant.type().allDerivatives()) {
			if (derivative.isSample()) {
				createDepsDeclaredIn(deps, derivative.getDerivedObject());
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
