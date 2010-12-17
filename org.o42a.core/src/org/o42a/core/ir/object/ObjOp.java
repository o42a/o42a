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

import static org.o42a.core.ir.object.ObjectPrecision.EXACT;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.object.ObjectBodyIR.Op;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.local.Dep;


public final class ObjOp extends ObjectOp {

	private final Obj ascendant;

	ObjOp(ObjectBodyIR.Op ptr, Obj ascendant, ObjectDataOp data) {
		super(ptr, data);
		this.ascendant = ascendant;
		assert getPrecision().isCompatible() :
			"Wrong object precision: " + this;
		assert (!getPrecision().isExact()
				|| ascendant.cloneOf(ptr.getAscendant())) :
					ascendant + " is not a clone of " + ptr.getAscendant();
	}

	ObjOp(
			CodeBuilder builder,
			ObjectBodyIR.Op ptr,
			Obj ascendant,
			ObjectPrecision precision) {
		super(builder, ptr, precision);
		this.ascendant = ascendant;
		assert getPrecision().isCompatible() :
			"Wrong object precision: " + this;
		assert (!getPrecision().isExact()
				|| ascendant.cloneOf(ptr.getAscendant())) :
					ascendant + " is not a clone of " + ptr.getAscendant();
	}

	public final Obj getAscendant() {
		return this.ascendant;
	}

	@Override
	public final Obj getWellKnownType() {
		return this.ascendant;
	}

	@Override
	public final ObjectBodyIR.Op ptr() {
		return (Op) super.ptr();
	}

	@Override
	public void writeRequirement(Code code, CodePos exit, ObjectOp body) {
		if (!getPrecision().isExact()) {
			super.writeRequirement(code, exit, body);
			return;
		}

		final ObjectValueIR valueIR = getAscendant().valueIR(getGenerator());

		valueIR.writeRequirement(code, exit, this, body);
	}

	@Override
	public void writePostCondition(Code code, CodePos exit, ObjOp body) {
		if (!getPrecision().isExact()) {
			super.writePostCondition(code, exit, body);
			return;
		}

		final ObjectValueIR valueIR = getAscendant().valueIR(getGenerator());

		valueIR.writePostCondition(code, exit, this, body);
	}

	@Override
	public ObjOp cast(Code code, CodePos exit, Obj ascendant) {
		getAscendant().assertDerivedFrom(ascendant);
		if (ascendant == getAscendant()) {
			return this;
		}
		if (ascendant == getContext().getVoid()) {
			return this;
		}
		if (getPrecision().isExact()) {
			return staticCast(code, ascendant);
		}
		if (ascendant.cloneOf(ptr().getAscendant())) {
			return ptr().op(getBuilder(), ascendant, getPrecision());
		}
		return dynamicCast(code, ascendant);
	}

	@Override
	public FldOp field(Code code, CodePos exit, MemberKey memberKey) {

		final Fld fld = ptr().getBodyIR().getObjectIR().fld(memberKey);
		final ObjOp host =
			cast(code, exit, memberKey.getOrigin().getContainer().toObject());

		return fld.op(code, host);
	}

	@Override
	public DepOp dep(Code code, CodePos exit, Dep dep) {

		final DepIR ir = ptr().getBodyIR().getObjectIR().dep(dep);
		final ObjOp host = cast(code, exit, dep.getObject());

		return ir.op(code, host);
	}

	public FldOp declaredField(Code code, MemberKey memberKey) {
		return ptr().declaredField(code, this, memberKey);
	}

	private ObjOp staticCast(Code code, Obj ascendant) {

		final ObjectBodyIR ascendantBodyIR =
			ptr().getBodyIR().getObjectIR().bodyIR(ascendant);
		final ObjectBodyIR.Op ascendantBody =
			ascendantBodyIR.getPointer().op(code);

		final ObjectDataOp cachedData = cachedData();

		if (cachedData != null) {
			return ascendantBody.op(cachedData, ascendant);
		}

		return ascendantBody.op(getBuilder(), ascendant, EXACT);
	}

	@Override
	protected void writeValue(Code code, ValOp result, ObjectOp body) {
		if (!getPrecision().isExact()) {
			super.writeValue(code, result, body);
			return;
		}

		final ObjectValueIR valueIR = getAscendant().valueIR(getGenerator());

		valueIR.writeValue(code, result, this, body);
	}

	@Override
	protected void writeClaim(Code code, ValOp result, ObjectOp body) {
		if (!getPrecision().isExact()) {
			super.writeClaim(code, result, body);
			return;
		}

		final ObjectValueIR valueIR = getAscendant().valueIR(getGenerator());

		valueIR.writeClaim(code, result, this, body);
	}

	@Override
	protected void writeProposition(Code code, ValOp result, ObjectOp body) {
		if (!getPrecision().isExact()) {
			super.writeProposition(code, result, body);
			return;
		}

		final ObjectValueIR valueIR = getAscendant().valueIR(getGenerator());

		valueIR.writeProposition(code, result, this, body);
	}

}
