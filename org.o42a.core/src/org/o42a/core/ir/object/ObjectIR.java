/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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

import static org.o42a.core.ir.object.ObjectPrecision.EXACT;

import java.util.Collection;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.data.Data;
import org.o42a.codegen.data.Struct;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.local.Dep;
import org.o42a.core.ref.type.TypeRef;


public final class ObjectIR  {

	private final Generator generator;
	private final Obj object;
	private ObjectIRStruct struct;

	public ObjectIR(Generator generator, Obj object) {
		this.generator = generator;
		this.object = object;
	}

	public final Generator getGenerator() {
		return this.generator;
	}

	public final Obj getObject() {
		return this.object;
	}

	public final CodeId getId() {
		return getStruct().codeId(getGenerator());
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
		return getStruct().mainBodyIR();
	}

	public final Collection<? extends ObjectBodyIR> getBodyIRs() {
		return getStruct().bodyIRs().values();
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
		return getStruct().allBodies();
	}

	public final ObjectTypeIR getTypeIR() {
		return getStruct().typeIR();
	}

	public final ObjectValueIR getValueIR() {
		return this.object.valueIR(getGenerator());
	}

	public final Data<?> getData() {
		return getStruct().data(getGenerator());
	}

	public final void allocate() {
		getStruct();
	}

	public ObjOp op(CodeBuilder builder, Code code) {

		final ObjectBodyIR bodyType = getBodyType();

		return bodyType.data(code.getGenerator())
		.getPointer().op(code).op(builder, getObject(), EXACT);
	}

	public ObjectBodyIR bodyIR(Obj ascendant) {

		if (ascendant == ascendant.getContext().getVoid()) {
			return getMainBodyIR();
		}

		final ObjectBodyIR bodyIR = getStruct().bodyIRs().get(ascendant);

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

	final ObjectIRStruct getStruct() {
		if (this.struct != null) {
			return this.struct;
		}

		this.struct = new ObjectIRStruct(this);

		getGenerator().newGlobal().struct(this.struct);
		getScopeIR().targetAllocated();

		return this.struct;
	}

}
