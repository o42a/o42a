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

import java.util.Collection;
import java.util.LinkedHashMap;

import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.data.Struct;
import org.o42a.codegen.data.SubData;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectType;
import org.o42a.core.object.type.Sample;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.util.string.ID;


public final class ObjectIRStruct extends Struct<ObjectIROp> {

	public static final ID OBJECT_ID = ID.id("object");

	private final ObjectIR objectIR;

	private final ObjectDataIR dataIR;
	private final LinkedHashMap<Obj, ObjectIRBody> bodyIRs =
			new LinkedHashMap<>();
	private ObjectIRBody mainBodyIR;

	private ObjectIRData objectData;

	ObjectIRStruct(ObjectIR objectIR) {
		super(objectIR.getId().detail(OBJECT_ID));
		this.objectIR = objectIR;
		this.dataIR = new ObjectDataIR(this);
	}

	public final Obj getObject() {
		return getObjectIR().getObject();
	}

	public final ObjectIR getObjectIR() {
		return this.objectIR;
	}

	public final Obj getSampleDeclaration() {
		return getObjectIR().getSampleDeclaration();
	}

	public final ObjectDataIR dataIR() {
		return this.dataIR;
	}

	public final ObjectIRBody mainBodyIR() {
		assert this.mainBodyIR != null :
			"Main body is not allocated yet";
		return this.mainBodyIR;
	}

	public final LinkedHashMap<Obj, ObjectIRBody> bodyIRs() {
		return this.bodyIRs;
	}

	public final ObjectIRData objectData() {
		return this.objectData;
	}

	@Override
	public ObjectIROp op(StructWriter<ObjectIROp> writer) {
		return new ObjectIROp(writer);
	}

	@Override
	protected final void allocate(SubData<ObjectIROp> data) {
		this.objectData = this.dataIR.allocate(data);
		allocateBodyIRs(data);
	}

	@Override
	protected void fill() {
	}

	private void allocateBodyIRs(SubData<?> data) {

		final ObjectType objectType = getObject().type();
		final TypeRef ancestorRef = objectType.getAncestor();

		if (ancestorRef != null) {

			final Obj ancestor = ancestorRef.getInterface();

			if (!ancestor.is(ancestor.getContext().getVoid())) {
				deriveBodyIRs(data, ancestor, true);
			}
		}

		final Sample sample = objectType.getSample();

		if (sample != null) {
			deriveBodyIRs(data, sample.getObject(), false);
		} else {
			assert getObjectIR().isSampleDeclaration() :
				"The object has no sample. It should be a declaration then";
			allocateBodyIR(data, new ObjectIRBody(this), false);
		}
	}

	private void deriveBodyIRs(
			SubData<?> data,
			Obj ascendant,
			boolean inherited) {

		final Collection<? extends ObjectIRBody> ascendantBodyIRs =
				ascendant.ir(getGenerator()).getBodyIRs();

		for (ObjectIRBody ascendantBodyIR : ascendantBodyIRs) {
			if (this.bodyIRs.containsKey(
					ascendantBodyIR.getSampleDeclaration())) {
				// The body for the given declaration is already present.
				continue;
			}

			final ObjectIRBody bodyIR = ascendantBodyIR.derive(getObjectIR());

			allocateBodyIR(data, bodyIR, inherited);
		}
	}

	private void allocateBodyIR(
			SubData<?> data,
			ObjectIRBody bodyIR,
			boolean inherited) {
		if (!inherited) {
			assert this.mainBodyIR == null :
				"Main body already allocated: " + this.mainBodyIR;
			this.mainBodyIR = bodyIR;
		}

		final Obj declaration = bodyIR.getSampleDeclaration();

		this.bodyIRs.put(declaration, bodyIR);
		bodyIR.allocate(data);
	}

}
