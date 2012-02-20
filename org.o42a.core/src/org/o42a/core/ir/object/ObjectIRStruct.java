/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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
import static org.o42a.core.object.type.Derivation.IMPLICIT_PROPAGATION;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Struct;
import org.o42a.codegen.data.SubData;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectType;
import org.o42a.core.object.type.Sample;
import org.o42a.core.ref.type.TypeRef;


final class ObjectIRStruct extends Struct<ObjectIRStruct.Op> {

	private final ObjectIR objectIR;

	private final ObjectTypeIR typeIR;
	private final LinkedHashMap<Obj, ObjectBodyIR> bodyIRs =
			new LinkedHashMap<Obj, ObjectBodyIR>();
	private final ArrayList<ObjectBodyIR> sampleBodyIRs =
			new ArrayList<ObjectBodyIR>();
	private final ObjectBodyIR mainBodyIR;

	public ObjectIRStruct(ObjectIR objectIR) {
		this.objectIR = objectIR;
		this.mainBodyIR = new ObjectBodyIR(this);
		this.typeIR = new ObjectTypeIR(this);
	}

	public final Obj getObject() {
		return getObjectIR().getObject();
	}

	public final ObjectIR getObjectIR() {
		return this.objectIR;
	}

	public final ObjectTypeIR typeIR() {
		return this.typeIR;
	}

	public final ObjectBodyIR mainBodyIR() {
		return this.mainBodyIR;
	}

	public final LinkedHashMap<Obj, ObjectBodyIR> bodyIRs() {
		return this.bodyIRs;
	}

	public final ArrayList<ObjectBodyIR> sampleBodyIRs() {
		return this.sampleBodyIRs;
	}

	@Override
	public Op op(StructWriter<Op> writer) {
		return new Op(writer);
	}

	@Override
	protected final void allocate(SubData<Op> data) {
		allocateBodyIRs(data);
		this.typeIR.allocate(data);
		allocateMethodsIRs(data);
	}

	@Override
	protected void fill() {
	}

	@Override
	protected CodeId buildCodeId(CodeIdFactory factory) {
		return getObject().getScope().ir(getObjectIR().getGenerator()).getId();
	}

	private void allocateBodyIRs(SubData<?> data) {

		final ObjectType objectType = getObject().type();
		final TypeRef ancestorRef = objectType.getAncestor();

		if (ancestorRef != null) {

			final Obj ancestor = ancestorRef.typeObject(dummyUser());

			if (ancestor != ancestor.getContext().getVoid()) {
				deriveBodyIRs(data, ancestor, true);
			}
		}

		final Sample[] samples = objectType.getSamples();

		for (int i = samples.length - 1; i >= 0; --i) {
			deriveBodyIRs(data, samples[i].typeObject(dummyUser()), false);
		}

		allocateBodyIR(data, this.mainBodyIR, null, false);
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

			final ObjectBodyIR bodyIR = ascendantBodyIR.derive(getObjectIR());

			allocateBodyIR(data, bodyIR, ascendantBodyIR, inherited);
			if (!inherited) {
				this.sampleBodyIRs.add(bodyIR);
			}
		}
	}

	private void allocateBodyIR(
			SubData<?> data,
			ObjectBodyIR bodyIR,
			ObjectBodyIR derivedFrom,
			boolean inherited) {

		final Obj ascendant = bodyIR.getAscendant();

		this.bodyIRs.put(ascendant, bodyIR);
		if (derivedFrom != null) {
			data.addStruct(
					bodyId(data.getGenerator(), bodyIR),
					derivedFrom,
					bodyIR);
		} else {
			data.addStruct(
					bodyId(data.getGenerator(), bodyIR),
					bodyIR);
		}

		if (inherited) {
			bodyIR.setKind(ObjectBodyIR.Kind.INHERITED);
		} else if (bodyIR.isMain()) {
			bodyIR.setKind(ObjectBodyIR.Kind.MAIN);
		} else if (getObject().type().derivedFrom(
				bodyIR.getAscendant().type(),
				IMPLICIT_PROPAGATION)) {
			bodyIR.setKind(ObjectBodyIR.Kind.PROPAGATED);
		} else {
			bodyIR.setKind(ObjectBodyIR.Kind.EXPLICIT);
		}
	}

	private void allocateMethodsIRs(SubData<?> data) {
		for (ObjectBodyIR bodyIR : bodyIRs().values()) {
			bodyIR.allocateMethodsIR(data);
		}
	}

	private static CodeId bodyId(Generator generator, ObjectBodyIR bodyIR) {

		final ObjectIR ascendantIR = bodyIR.getAscendant().ir(generator);

		return generator.id().detail("body").detail(ascendantIR.getId());
	}

	public static final class Op extends StructOp<Op> {

		public Op(StructWriter<Op> writer) {
			super(writer);
		}

	}

}
