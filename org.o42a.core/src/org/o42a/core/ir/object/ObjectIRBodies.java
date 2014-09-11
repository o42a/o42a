/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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

import java.util.Iterator;
import java.util.LinkedHashMap;

import org.o42a.codegen.Generator;
import org.o42a.codegen.data.Ptr;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.member.MemberKey;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectType;
import org.o42a.core.object.type.Sample;
import org.o42a.core.ref.type.TypeRef;


public final class ObjectIRBodies implements Iterable<ObjectIRBody> {

	private final ObjectIR objectIR;
	private final LinkedHashMap<Obj, ObjectIRBody> bodyIRs =
			new LinkedHashMap<>();
	private ObjectIRBody mainBodyIR;
	private ObjectIRStruct struct;
	private final boolean typeBodies;

	public ObjectIRBodies(ObjectIR objectIR, boolean typeBodies) {
		this.objectIR = objectIR;
		this.typeBodies = typeBodies;
	}

	public final Generator getGenerator() {
		return getObjectIR().getGenerator();
	}

	public final Obj getObject() {
		return getObjectIR().getObject();
	}

	public final boolean isSampleDeclaration() {
		return getObjectIR().isSampleDeclaration();
	}

	public final Obj getSampleDeclaration() {
		return getObjectIR().getSampleDeclaration();
	}

	public final boolean isTypeBodies() {
		return this.typeBodies;
	}

	public final ObjectIR getObjectIR() {
		return this.objectIR;
	}

	public final ObjectIRBody getMainBodyIR() {
		return this.mainBodyIR;
	}

	public final LinkedHashMap<Obj, ObjectIRBody> getBodyIRs() {
		return this.bodyIRs;
	}

	public final Ptr<ObjectIROp> ptr() {
		return getStruct().pointer(getGenerator());
	}

	public final ObjectIRStruct getStruct() {
		if (this.struct != null) {
			return this.struct;
		}

		assert getObject().assertFullyResolved();

		if (isTypeBodies()) {
			return this.struct = new ObjectIRStruct(this);
		}

		final ObjectIRBlock block = new ObjectIRBlock(this);

		this.struct = block.getStruct();
		getGenerator().newGlobal().struct(block);
		getObjectIR().getScopeIR().targetAllocated();

		return this.struct;
	}

	@Override
	public final Iterator<ObjectIRBody> iterator() {
		return getBodyIRs().values().iterator();
	}

	public final ObjectIRBody bodyIR(Obj ascendant) {

		final ObjectIRBody bodyIR = findBodyIR(ascendant);

		assert bodyIR != null :
			"Can not find ascendant body for " + ascendant + " in " + this;

		return bodyIR;
	}

	public final ObjectIRBody findBodyIR(Obj ascendant) {
		if (ascendant.is(ascendant.getContext().getVoid())) {
			return getMainBodyIR();
		}
		return this.bodyIRs.get(ascendant.type().getSampleDeclaration());
	}

	public final Fld<?> fld(MemberKey memberKey) {

		final Obj origin = memberKey.getOrigin().toObject();
		final ObjectIRBody bodyIR = bodyIR(origin);

		return bodyIR.fld(memberKey);
	}

	public final Fld<?> findFld(MemberKey memberKey) {

		final Obj origin = memberKey.getOrigin().toObject();
		final ObjectIRBody bodyIR = findBodyIR(origin);

		return bodyIR != null ? bodyIR.findFld(memberKey) : null;
	}

	void allocate() {

		final ObjectIR objectIR = getObjectIR();
		final ObjectType objectType = objectIR.getObject().type();
		final TypeRef ancestorRef = objectType.getAncestor();

		if (ancestorRef != null) {

			final Obj ancestor = ancestorRef.getInterface();

			if (!ancestor.is(ancestor.getContext().getVoid())) {
				deriveBodyIRs(ancestor, true);
			}
		}

		final Sample sample = objectType.getSample();

		if (sample != null) {
			deriveBodyIRs(sample.getObject(), false);
		} else {
			assert isSampleDeclaration() :
				"The object has no sample. It should be a declaration then";
			addBodyIR(new ObjectIRBody(this), false);
		}
	}

	private void deriveBodyIRs(Obj ascendant, boolean inherited) {
		for (ObjectIRBody ascendantBodyIR :
				ascendant.ir(getGenerator()).typeBodies()) {
			if (this.bodyIRs.containsKey(
					ascendantBodyIR.getSampleDeclaration())) {
				// The body for the given declaration is already present.
				continue;
			}

			final ObjectIRBody bodyIR = ascendantBodyIR.derive(this);

			addBodyIR(bodyIR, inherited);
		}
	}

	private void addBodyIR(ObjectIRBody bodyIR, boolean inherited) {
		if (!inherited) {
			assert this.mainBodyIR == null :
				"Main body already allocated: " + this.mainBodyIR;
			this.mainBodyIR = bodyIR;
		}

		final Obj declaration = bodyIR.getSampleDeclaration();

		this.bodyIRs.put(declaration, bodyIR);
	}

}
