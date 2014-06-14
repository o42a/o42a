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

import static org.o42a.core.object.type.DerivationUsage.ALL_DERIVATION_USAGES;

import java.util.Collection;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.data.Data;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.ir.object.dep.DepIR;
import org.o42a.core.ir.value.type.ValueIR;
import org.o42a.core.member.MemberKey;
import org.o42a.core.object.Obj;
import org.o42a.core.object.state.Dep;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.util.string.ID;


public class ObjectIR  {

	private final Generator generator;
	private final Obj object;
	private final ValueIR valueIR;
	private ObjectIRStruct struct;
	private ObjectValueIR objectValueIR;

	public ObjectIR(Generator generator, Obj object) {
		this.generator = generator;
		this.object = object;
		this.valueIR =
				object.type().getValueType().ir(generator).valueIR(this);
	}

	public final Generator getGenerator() {
		return this.generator;
	}

	public final Obj getObject() {
		return this.object;
	}

	public final boolean isSampleDeclaration() {
		return getObject().is(getSampleDeclaration());
	}

	public final Obj getSampleDeclaration() {
		return getObject().type().getSampleDeclaration();
	}

	public final ID getId() {
		return getScopeIR().getScope().getId();
	}

	public final boolean isExact() {
		return !getObject().type().derivation().isUsed(
				getGenerator().getAnalyzer(),
				ALL_DERIVATION_USAGES);
	}

	public final ScopeIR getScopeIR() {
		return getObject().getScope().ir(getGenerator());
	}

	public final ObjectIR definitionIR() {
		if (isExact()) {
			return this;
		}
		return getObject().type().getLastDefinition().ir(getGenerator());
	}

	public final ObjectIRBody getBodyType() {
		return definitionIR().getMainBodyIR();
	}

	public final ObjectIRBody getMainBodyIR() {
		return getStruct().mainBodyIR();
	}

	public final Collection<? extends ObjectIRBody> getBodyIRs() {
		return getStruct().bodyIRs().values();
	}

	public ObjectIRBody getAncestorBodyIR() {

		final TypeRef ancestorType = getObject().type().getAncestor();

		if (ancestorType == null) {
			return null;
		}

		final Obj ancestor = ancestorType.getInterface();

		if (ancestor.is(ancestor.getContext().getVoid())) {
			return null;
		}

		return bodyIR(ancestor);
	}

	public final ObjectDataIR getStaticDataIR() {

		final Obj lastDefinition = getObject().type().getLastDefinition();

		return lastDefinition.ir(getGenerator()).getDataIR();
	}

	public final ObjectDataIR getDataIR() {
		return getStruct().dataIR();
	}

	public final ValueIR getValueIR() {
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

	public final ObjOp op(CodeBuilder builder, Code code) {
		return getMainBodyIR().data(getGenerator())
				.getPointer().op(null, code).op(builder, this);
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
		return getStruct().bodyIRs().get(
				ascendant.type().getSampleDeclaration());
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

	public final DepIR dep(Dep dep) {
		return bodyIR(dep.getDeclaredIn()).dep(dep);
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

		assert getObject().assertFullyResolved();

		final ObjectIRBlock block = new ObjectIRBlock(this);

		this.struct = block.getStruct();

		getGenerator().newGlobal().struct(block);
		getScopeIR().targetAllocated();

		return this.struct;
	}

}
