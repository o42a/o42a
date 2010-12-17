/*
    Compiler Core
    Copyright (C) 2010 Ruslan Lopatin

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

import static org.o42a.core.ir.IRSymbolSeparator.DETAIL;
import static org.o42a.core.ir.object.ObjectPrecision.EXACT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Struct;
import org.o42a.codegen.data.SubData;
import org.o42a.core.artifact.TypeRef;
import org.o42a.core.artifact.object.Derivation;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.Sample;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.IRGenerator;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.local.Dep;


public final class ObjectIR extends Struct<ObjectIR.Op> {

	private final IRGenerator generator;
	private final Obj object;
	private final ObjectTypeIR typeIR;
	private final LinkedHashMap<Obj, ObjectBodyIR> bodyIRs =
		new LinkedHashMap<Obj, ObjectBodyIR>();
	private final ArrayList<ObjectBodyIR> sampleBodyIRs =
		new ArrayList<ObjectBodyIR>();
	private ObjectBodyIR mainBodyIR;

	private Struct<?> allBodies;

	public ObjectIR(IRGenerator generator, Obj object) {
		super(object.getScope().ir(generator).getId());
		this.generator = generator;
		this.object = object;
		this.typeIR = new ObjectTypeIR(this);
	}

	public final IRGenerator getGenerator() {
		return this.generator;
	}

	public final Obj getObject() {
		return this.object;
	}

	public final ScopeIR getScopeIR() {
		return this.object.getScope().ir(getGenerator());
	}

	public final ObjectBodyIR getBodyType() {

		final Field<?> field = getObject().getScope().toField();

		if (field == null) {
			return getMainBodyIR();
		}

		final ObjectIR lastDefinitionIR =
			field.getLastDefinition().getArtifact()
			.toObject().ir(getGenerator());

		return lastDefinitionIR.getMainBodyIR();
	}

	public final ObjectBodyIR getMainBodyIR() {
		allocate();
		return this.mainBodyIR;
	}

	public final Collection<? extends ObjectBodyIR> getBodyIRs() {
		allocate();
		return this.bodyIRs.values();
	}

	public ObjectBodyIR getAncestorBodyIR() {

		final TypeRef ancestorType = getObject().getAncestor();

		if (ancestorType == null) {
			return null;
		}

		final Obj ancestor = ancestorType.getType();

		if (ancestor == ancestor.getContext().getVoid()) {
			return null;
		}

		return bodyIR(ancestor);
	}

	public final Struct<?> getAllBodies() {
		allocate();
		return this.allBodies;
	}

	public final ObjectTypeIR getTypeIR() {
		allocate();
		return this.typeIR;
	}

	public final ObjectValueIR getValueIR() {
		return this.object.valueIR(getGenerator());
	}

	@Override
	public void allocate() {
		if (this.mainBodyIR != null) {
			return;
		}
		getGenerator().newGlobal().create(this);
		getScopeIR().targetAllocated();
	}

	public ObjOp op(CodeBuilder builder, Code code) {

		final ObjectBodyIR bodyType = getBodyType();

		return bodyType.getPointer().op(code).op(builder, getObject(), EXACT);
	}

	@Override
	public Op op(StructWriter writer) {
		allocate();
		return new Op(writer, this);
	}

	public ObjectBodyIR bodyIR(Obj ascendant) {
		allocate();

		if (ascendant == ascendant.getContext().getVoid()) {
			return getMainBodyIR();
		}

		final ObjectBodyIR bodyIR = this.bodyIRs.get(ascendant);

		assert bodyIR != null :
			"Can not find ascendant body for " + ascendant + " in " + this;

		return bodyIR;
	}

	public Fld fld(MemberKey memberKey) {

		final Obj origin = memberKey.getOrigin().getContainer().toObject();
		final ObjectBodyIR bodyIR = bodyIR(origin);

		return bodyIR.fld(memberKey);
	}

	public DepIR dep(Dep dep) {

		final ObjectBodyIR bodyIR = bodyIR(dep.getObject());

		return bodyIR.dep(dep);
	}

	@Override
	public String toString() {
		return this.object + " IR";
	}

	protected ObjectValueIR createValueIR() {
		return new ObjectValueIR(this);
	}

	@Override
	protected final void allocate(SubData<Op> data) {
		// it's here to prevent recursion
		this.mainBodyIR = new ObjectBodyIR(this);
		this.allBodies = new AllBodies();
		data.addStruct("all_bodies", this.allBodies);
		this.typeIR.allocate(data);
		allocateMetaIRs(data);
	}

	@Override
	protected void fill() {
	}

	final ArrayList<ObjectBodyIR> getSampleBodyIRs() {
		return this.sampleBodyIRs;
	}

	private void allocateBodyIRs(SubData<?> data) {

		final Obj object = getObject();
		final TypeRef ancestorRef = object.getAncestor();

		if (ancestorRef != null) {

			final Obj ancestor = ancestorRef.getType();

			if (ancestor != ancestor.getContext().getVoid()) {
				deriveBodyIRs(data, ancestor, true);
			}
		}

		final Sample[] samples = object.getSamples();

		for (int i = samples.length - 1; i >= 0; --i) {
			deriveBodyIRs(data, samples[i].getType(), false);
		}

		allocateBodyIR(data, this.mainBodyIR, false);
	}

	private void deriveBodyIRs(
			SubData<?> data,
			Obj ascendant,
			boolean inherited) {

		final Collection<? extends ObjectBodyIR> ascendantBodyIRs =
			ascendant.ir(getGenerator()).getBodyIRs();

		for (ObjectBodyIR ascendantBodyIR : ascendantBodyIRs) {
			if (this.bodyIRs.containsKey(ascendantBodyIR.getAscendant())) {
				// body for the given ascendant already present
				continue;
			}

			final ObjectBodyIR bodyIR = ascendantBodyIR.derive(this);

			allocateBodyIR(data, bodyIR, inherited);
			if (!inherited) {
				this.sampleBodyIRs.add(bodyIR);
			}
		}
	}

	private void allocateBodyIR(
			SubData<?> data,
			ObjectBodyIR bodyIR,
			boolean inherited) {

		final Obj ascendant = bodyIR.getAscendant();

		this.bodyIRs.put(ascendant, bodyIR);

		final String name;

		if (bodyIR.isMain()) {
			name = "main_body";
		} else {
			name = "body" + DETAIL + ascendant.ir(getGenerator()).getId();
		}

		data.addStruct(name, bodyIR);

		if (inherited) {
			bodyIR.setKind(ObjectBodyIR.Kind.INHERITED);
		} else if (bodyIR.isMain()) {
			bodyIR.setKind(ObjectBodyIR.Kind.MAIN);
		} else if (getObject().derivedFrom(
				bodyIR.getAscendant(),
				Derivation.MEMBER_OVERRIDE)) {
			bodyIR.setKind(ObjectBodyIR.Kind.PROPAGATED);
		} else {
			bodyIR.setKind(ObjectBodyIR.Kind.EXPLICIT);
		}
	}

	private void allocateMetaIRs(SubData<?> data) {
		for (ObjectBodyIR bodyIR : getBodyIRs()) {
			bodyIR.allocateMetaIR(data);
		}
	}

	public static final class Op extends StructOp {

		private final ObjectIR objectIR;

		public Op(StructWriter writer, ObjectIR objectIR) {
			super(writer);
			this.objectIR = objectIR;
		}

		public final ObjectIR getObjectIR() {
			return this.objectIR;
		}

		@Override
		public Op create(StructWriter writer) {
			return new Op(writer, this.objectIR);
		}

	}

	private final class AllBodiesOp extends StructOp {

		public AllBodiesOp(StructWriter writer) {
			super(writer);
		}

		@Override
		public AllBodiesOp create(StructWriter writer) {
			return new AllBodiesOp(writer);
		}

	}

	private final class AllBodies extends Struct<AllBodiesOp> {

		AllBodies() {
			super(ObjectIR.this.getId() + DETAIL + "all_bodies");
		}

		@Override
		public AllBodiesOp op(StructWriter writer) {
			return new AllBodiesOp(writer);
		}

		@Override
		protected void fill() {
		}

		@Override
		protected void allocate(SubData<AllBodiesOp> data) {
			allocateBodyIRs(data);
		}

	}

}
