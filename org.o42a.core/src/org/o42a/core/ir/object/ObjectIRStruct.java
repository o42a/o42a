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

import static org.o42a.core.ir.gc.GCDescOp.GC_DESC_TYPE;
import static org.o42a.core.ir.object.ObjectIRBody.BODY_ID;
import static org.o42a.core.object.type.Derivation.IMPLICIT_PROPAGATION;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Content;
import org.o42a.codegen.data.Struct;
import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.gc.GCBlockOp;
import org.o42a.core.ir.gc.GCBlockOp.Type;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectType;
import org.o42a.core.object.type.Sample;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.util.fn.Getter;
import org.o42a.util.string.ID;


final class ObjectIRStruct
		extends Struct<ObjectIRStruct.Op>
		implements Content<GCBlockOp.Type> {

	static final ID OBJECT_ID = ID.id("object");
	private static final ID BODY_PREFIX_ID = ID.id().detail(BODY_ID);

	private final ObjectIR objectIR;

	private final ObjectTypeIR typeIR;
	private final LinkedHashMap<Obj, ObjectIRBody> bodyIRs =
			new LinkedHashMap<>();
	private final ArrayList<ObjectIRBody> sampleBodyIRs = new ArrayList<>();
	private final ObjectIRBody mainBodyIR;

	public ObjectIRStruct(ObjectIR objectIR) {
		super(objectIR.getId().detail(OBJECT_ID));
		this.objectIR = objectIR;
		this.mainBodyIR = new ObjectIRBody(this);
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

	public final ObjectIRBody mainBodyIR() {
		return this.mainBodyIR;
	}

	public final LinkedHashMap<Obj, ObjectIRBody> bodyIRs() {
		return this.bodyIRs;
	}

	public final ArrayList<ObjectIRBody> sampleBodyIRs() {
		return this.sampleBodyIRs;
	}

	@Override
	public Op op(StructWriter<Op> writer) {
		return new Op(writer);
	}

	@Override
	public void allocated(Type instance) {
	}

	@Override
	public void fill(Type instance) {
		instance.lock().setValue((byte) 0);
		instance.list().setValue((byte) 0);
		instance.flags().setValue((short) 0);
		instance.useCount().setValue(0);
		instance.desc().setConstant(true).setValue(
				getGenerator()
				.externalGlobal()
				.setConstant()
				.link("o42a_obj_gc_desc", GC_DESC_TYPE));
		instance.prev().setNull();
		instance.next().setNull();
		instance.size()
		.setConstant(true)
		.setLowLevel(true)
		.setValue(new Getter<Integer>() {
			@Override
			public Integer get() {
				return layout(getGenerator()).size();
			}
		});
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

	private void allocateBodyIRs(SubData<?> data) {

		final ObjectType objectType = getObject().type();
		final TypeRef ancestorRef = objectType.getAncestor();

		if (ancestorRef != null) {

			final Obj ancestor = ancestorRef.getType();

			if (!ancestor.is(ancestor.getContext().getVoid())) {
				deriveBodyIRs(data, ancestor, true);
			}
		}

		final Sample[] samples = objectType.getSamples();

		for (int i = samples.length - 1; i >= 0; --i) {
			deriveBodyIRs(data, samples[i].getObject(), false);
		}

		allocateBodyIR(data, this.mainBodyIR, null, false);
	}

	private void deriveBodyIRs(
			SubData<?> data,
			Obj ascendant,
			boolean inherited) {

		final Collection<? extends ObjectIRBody> ascendantBodyIRs =
				ascendant.ir(getGenerator()).getBodyIRs();

		for (ObjectIRBody ascendantBodyIR : ascendantBodyIRs) {
			if (this.bodyIRs.containsKey(ascendantBodyIR.getAscendant())) {
				// body for the given ascendant already present
				continue;
			}

			final ObjectIRBody bodyIR = ascendantBodyIR.derive(getObjectIR());

			allocateBodyIR(data, bodyIR, ascendantBodyIR, inherited);
			if (!inherited) {
				this.sampleBodyIRs.add(bodyIR);
			}
		}
	}

	private void allocateBodyIR(
			SubData<?> data,
			ObjectIRBody bodyIR,
			ObjectIRBody derivedFrom,
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
			bodyIR.setKind(ObjectIRBody.Kind.INHERITED);
		} else if (bodyIR.isMain()) {
			bodyIR.setKind(ObjectIRBody.Kind.MAIN);
		} else if (getObject().type().derivedFrom(
				bodyIR.getAscendant().type(),
				IMPLICIT_PROPAGATION)) {
			bodyIR.setKind(ObjectIRBody.Kind.PROPAGATED);
		} else {
			bodyIR.setKind(ObjectIRBody.Kind.EXPLICIT);
		}
	}

	private void allocateMethodsIRs(SubData<?> data) {
		for (ObjectIRBody bodyIR : bodyIRs().values()) {
			bodyIR.allocateMethodsIR(data);
		}
	}

	private static ID bodyId(Generator generator, ObjectIRBody bodyIR) {

		final ObjectIR ascendantIR = bodyIR.getAscendant().ir(generator);

		return BODY_PREFIX_ID.detail(ascendantIR.getId());
	}

	public static final class Op extends StructOp<Op> {

		public Op(StructWriter<Op> writer) {
			super(writer);
		}

	}

}
