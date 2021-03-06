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

import static org.o42a.util.fn.Init.init;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.o42a.codegen.Generator;
import org.o42a.codegen.data.Ptr;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.ir.field.dep.DepIR;
import org.o42a.core.ir.field.inst.InstFld;
import org.o42a.core.ir.field.inst.InstFldKind;
import org.o42a.core.ir.field.local.LocalIR;
import org.o42a.core.member.MemberKey;
import org.o42a.core.object.Obj;
import org.o42a.core.object.state.Dep;
import org.o42a.core.object.type.Sample;
import org.o42a.util.fn.Init;


public final class ObjectIRBodies implements Iterable<ObjectIRBody> {

	private final ObjectIR objectIR;
	private final LinkedHashMap<Obj, ObjectIRBody> bodyIRs =
			new LinkedHashMap<>();
	private ObjectIRBody mainBodyIR;
	private final Init<EnumMap<InstFldKind, InstFld<?, ?>>> instFields =
			init(this::findInstFields);
	private final Init<ObjectIRStruct> struct = init(this::allocateStruct);
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
		return this.struct.get();
	}

	private ObjectIRStruct allocateStruct() {
		assert getObject().assertFullyResolved();

		if (isTypeBodies()) {
			return new ObjectIRStruct(this);
		}

		final ObjectIRBlock block = new ObjectIRBlock(this);
		final ObjectIRStruct struct = block.getStruct();

		this.struct.set(struct);// Preliminary initialization.
		getGenerator().newGlobal().struct(block);
		getObjectIR().getScopeIR().targetAllocated();

		return struct;
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

	public final InstFld<?, ?> instFld(InstFldKind kind) {

		final InstFld<?, ?> fld = findInstFld(kind);

		assert fld != null :
			"Instance field " + kind.getId() + " not found";

		return fld;
	}

	public final InstFld<?, ?> findInstFld(InstFldKind kind) {
		return instFields().get(kind);
	}

	public final Fld<?, ?> fld(MemberKey memberKey) {

		final Obj origin = memberKey.getOrigin().toObject();
		final ObjectIRBody bodyIR = bodyIR(origin);

		return bodyIR.fld(memberKey);
	}

	public final Fld<?, ?> findFld(MemberKey memberKey) {

		final Obj origin = memberKey.getOrigin().toObject();
		final ObjectIRBody bodyIR = findBodyIR(origin);

		return bodyIR != null ? bodyIR.findFld(memberKey) : null;
	}

	public final DepIR dep(Dep dep) {
		return bodyIR(dep.getDeclaredIn()).dep(dep);
	}

	public final LocalIR local(MemberKey memberKey) {

		final Obj origin = memberKey.getOrigin().toObject();
		final ObjectIRBody bodyIR = bodyIR(origin);

		return bodyIR.local(memberKey);
	}

	final ObjectIRBodies allocate() {

		final Obj ancestor = getObjectIR().getAncestor();

		if (ancestor != null
				&& !ancestor.is(ancestor.getContext().getVoid())) {
			deriveBodyIRs(ancestor, true);
		}

		final Sample sample = getObject().type().getSample();

		if (sample != null) {
			deriveBodyIRs(sample.getObject(), false);
		} else {
			assert isSampleDeclaration() :
				"The object has no sample. It should be a declaration then";
			addBodyIR(new ObjectIRBody(this), false);
		}

		return this;
	}

	private final EnumMap<InstFldKind, InstFld<?, ?>> instFields() {
		return this.instFields.get();
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

	private EnumMap<InstFldKind, InstFld<?, ?>> findInstFields() {

		final EnumMap<InstFldKind, InstFld<?, ?>> fields =
				new EnumMap<>(InstFldKind.class);

		for (ObjectIRBody body : this) {
			fields.putAll(body.instFields());
		}

		return fields;
	}

}
