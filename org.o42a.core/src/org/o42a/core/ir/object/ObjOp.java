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

import static org.o42a.core.ir.IRUtil.encodeMemberId;
import static org.o42a.core.ir.object.ObjectPrecision.EXACT;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.object.ObjectBodyIR.Op;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.local.Dep;


public final class ObjOp extends ObjectOp {

	private final Obj ascendant;

	ObjOp(ObjectBodyIR.Op ptr, Obj ascendant, ObjectTypeOp data) {
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
	public ValOp writeValue(ValDirs dirs, ObjectOp body) {
		if (!getPrecision().isExact()) {
			return super.writeValue(dirs, body);
		}

		final ObjectValueIR valueIR = getAscendant().valueIR(getGenerator());

		return valueIR.writeValue(dirs, this, body);
	}

	@Override
	public void writeRequirement(CodeDirs dirs, ObjectOp body) {
		if (!getPrecision().isExact()) {
			super.writeRequirement(dirs, body);
			return;
		}

		final ObjectValueIR valueIR = getAscendant().valueIR(getGenerator());

		valueIR.writeRequirement(dirs, this, body);
	}

	@Override
	public void writeCondition(CodeDirs dirs, ObjOp body) {
		if (!getPrecision().isExact()) {
			super.writeCondition(dirs, body);
			return;
		}

		final ObjectValueIR valueIR = getAscendant().valueIR(getGenerator());

		valueIR.writeCondition(dirs, this, body);
	}

	@Override
	public ObjOp cast(CodeId id, CodeDirs dirs, Obj ascendant) {
		ptr().getType().getObjectIR().getObject().assertDerivedFrom(
				ascendant);
		if (ascendant == getAscendant()) {
			return this;
		}
		if (ascendant == getContext().getVoid()) {
			return this;
		}
		if (getPrecision().isExact()) {
			return staticCast(dirs.code(), ascendant);
		}
		if (ascendant.cloneOf(ptr().getAscendant())) {
			// Clone shares the body with it`s origin.
			return this;
		}
		return dynamicCast(id, dirs, ascendant);
	}

	@Override
	public FldOp field(CodeDirs dirs, MemberKey memberKey) {
		dirs = dirs.begin("field", "Field " + memberKey + " of " + this);

		final Code code = dirs.code();
		final Fld fld = ptr().getType().getObjectIR().fld(memberKey);
		final CodeId hostId =
			code.id("field_host")
			.sub(encodeMemberId(getGenerator(), memberKey.getMemberId()));
		final ObjOp host = cast(
				hostId,
				dirs,
				memberKey.getOrigin().getContainer().toObject());
		final FldOp op = fld.op(code, host);

		code.dumpName("Field: ", op.ptr());
		dirs.end();

		return op;
	}

	@Override
	public DepOp dep(CodeDirs dirs, Dep dep) {
		dirs = dirs.begin("dep", "Dep " + dep + " of " + this);

		final Code code = dirs.code();
		final DepIR ir = ptr().getType().getObjectIR().dep(dep);
		final String depName = dep.getName();
		final CodeId hostId = code.id("dep_host");
		final ObjOp host = cast(
				depName != null ? hostId.sub(depName) : hostId,
				dirs,
				dep.getObject());
		final DepOp op = ir.op(code, host);

		code.dumpName("Dep: ", op.ptr());
		dirs.end();

		return op;
	}

	public FldOp declaredField(Code code, MemberKey memberKey) {
		return ptr().declaredField(code, this, memberKey);
	}

	private ObjOp staticCast(Code code, Obj ascendant) {

		final ObjectBodyIR ascendantBodyIR =
			ptr().getType().getObjectIR().bodyIR(ascendant);
		final ObjectBodyIR.Op ascendantBody =
			ascendantBodyIR.pointer(code.getGenerator()).op(null, code);

		final ObjectTypeOp cachedData = cachedData();

		if (cachedData != null) {
			return ascendantBody.op(cachedData, ascendant);
		}

		return ascendantBody.op(getBuilder(), ascendant, EXACT);
	}

	@Override
	protected ValOp writeClaim(ValDirs dirs, ObjectOp body) {
		if (!getPrecision().isExact()) {
			return super.writeClaim(dirs, body);
		}

		final ObjectValueIR valueIR = getAscendant().valueIR(getGenerator());

		return valueIR.writeClaim(dirs, this, body);
	}

	@Override
	protected ValOp writeProposition(ValDirs dirs, ObjectOp body) {
		if (!getPrecision().isExact()) {
			return super.writeProposition(dirs, body);
		}

		final ObjectValueIR valueIR = getAscendant().valueIR(getGenerator());

		return valueIR.writeProposition(dirs, this, body);
	}

}
