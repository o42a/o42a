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
import static org.o42a.core.object.type.DerivationUsage.ALL_DERIVATION_USAGES;

import java.util.Collection;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.data.Data;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.ir.value.struct.ValueIR;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.local.Dep;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.type.TypeRef;


public class ObjectIR  {

	private final Generator generator;
	private final Obj object;
	private final ValueIR<?> valueIR;
	private ObjectIRStruct struct;
	private ObjectValueIR objectValueIR;

	public ObjectIR(Generator generator, Obj object) {
		this.generator = generator;
		this.object = object;
		this.valueIR =
				object.value().getValueStruct().ir(generator).valueIR(this);
	}

	public final Generator getGenerator() {
		return this.generator;
	}

	public final Obj getObject() {
		return this.object;
	}

	public final CodeId getId() {
		return getScopeIR().getId();
	}

	public final boolean isExact() {
		return !getObject().type().derivation().isUsed(
				getGenerator().getAnalyzer(),
				ALL_DERIVATION_USAGES);
	}

	public final ScopeIR getScopeIR() {
		return getObject().getScope().ir(getGenerator());
	}

	public final ObjectBodyIR getBodyType() {

		final Obj lastDefinition = getObject().type().getLastDefinition();

		return lastDefinition.ir(getGenerator()).getMainBodyIR();
	}

	public final ObjectBodyIR getMainBodyIR() {
		return getStruct().mainBodyIR();
	}

	public final Collection<? extends ObjectBodyIR> getBodyIRs() {
		return getStruct().bodyIRs().values();
	}

	public ObjectBodyIR getAncestorBodyIR() {

		final TypeRef ancestorType = getObject().type().getAncestor();

		if (ancestorType == null) {
			return null;
		}

		final Obj ancestor = ancestorType.typeObject(dummyUser());

		if (ancestor == ancestor.getContext().getVoid()) {
			return null;
		}

		return bodyIR(ancestor);
	}

	public final ObjectTypeIR getStaticTypeIR() {

		final Obj lastDefinition = getObject().type().getLastDefinition();

		return lastDefinition.ir(getGenerator()).getTypeIR();
	}

	public final ObjectTypeIR getTypeIR() {
		return getStruct().typeIR();
	}

	public final ValueIR<?> getValueIR() {
		return this.valueIR;
	}

	public final ObjectValueIR getObjectValueIR() {
		if (this.objectValueIR != null) {
			return this.objectValueIR;
		}
		return this.objectValueIR = new ObjectValueIR(this);
	}

	public final Data<?> getData() {
		return getStruct().data(getGenerator());
	}

	public final ObjectIR allocate() {
		getStruct();
		return this;
	}

	public ObjOp op(CodeBuilder builder, Code code) {

		final ObjectBodyIR bodyType = getBodyType();

		return bodyType.data(getGenerator())
				.getPointer().op(null, code).op(builder, this);
	}

	public final ObjectBodyIR bodyIR(Obj ascendant) {

		final ObjectBodyIR bodyIR = findBodyIR(ascendant);

		assert bodyIR != null :
			"Can not find ascendant body for " + ascendant + " in " + this;

		return bodyIR;
	}

	public final ObjectBodyIR findBodyIR(Obj ascendant) {
		if (ascendant == ascendant.getContext().getVoid()) {
			return getMainBodyIR();
		}
		return getStruct().bodyIRs().get(ascendant);
	}

	public final Fld fld(MemberKey memberKey) {

		final Obj origin = memberKey.getOrigin().toObject();
		final ObjectBodyIR bodyIR = bodyIR(origin);

		return bodyIR.fld(memberKey);
	}

	public final Fld findFld(MemberKey memberKey) {

		final Obj origin = memberKey.getOrigin().toObject();
		final ObjectBodyIR bodyIR = findBodyIR(origin);

		return bodyIR.findFld(memberKey);
	}

	public DepIR dep(Dep dep) {
		return bodyIR(dep.getObject()).dep(dep);
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
		if (getObject().isClone()) {
			getObject().pin();
		}

		assert getObject().assertFullyResolved();

		this.struct = new ObjectIRStruct(this);

		getGenerator().newGlobal().struct(this.struct);
		getScopeIR().targetAllocated();

		return this.struct;
	}

}
