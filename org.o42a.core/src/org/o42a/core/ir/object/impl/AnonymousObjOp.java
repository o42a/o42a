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
package org.o42a.core.ir.object.impl;

import static org.o42a.core.ir.field.Fld.FIELD_ID;
import static org.o42a.core.ir.object.ObjectPrecision.COMPATIBLE;
import static org.o42a.core.ir.object.ObjectPrecision.DERIVED;
import static org.o42a.core.ir.object.dep.DepOp.DEP_ID;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.object.dep.DepOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.value.type.ValueIR;
import org.o42a.core.ir.value.type.ValueOp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.object.Obj;
import org.o42a.core.object.state.Dep;
import org.o42a.util.string.ID;


public final class AnonymousObjOp extends ObjectOp {

	private final DataOp ptr;
	private final Obj wellKnownType;
	private ValueOp value;

	public AnonymousObjOp(ObjectDataOp data, DataOp ptr, Obj wellKnownType) {
		super(data);
		this.ptr = ptr;
		assert wellKnownType != null :
			"Object type not specified";
		this.wellKnownType = wellKnownType.getInterface();
	}

	public AnonymousObjOp(CodeBuilder builder, DataOp ptr, Obj wellKnownType) {
		super(builder, DERIVED);
		this.ptr = ptr;
		assert wellKnownType != null :
			"Object type not specified";
		this.wellKnownType = wellKnownType.getInterface();
	}

	@Override
	public final Obj getWellKnownType() {
		return this.wellKnownType;
	}

	@Override
	public final DataOp ptr() {
		return this.ptr;
	}

	@Override
	public final DataOp ptr(Code code) {
		return ptr();
	}

	@Override
	public final ValueOp value() {
		if (this.value != null) {
			return this.value;
		}

		final ValueIR valueIR =
				getWellKnownType().ir(getGenerator()).getValueIR();

		return this.value = valueIR.op(this);
	}

	@Override
	public ObjOp cast(ID id, CodeDirs dirs, Obj ascendant) {
		assert (getWellKnownType().type().derivedFrom(ascendant.type())
				|| ascendant.type().derivedFrom(getWellKnownType().type()))
			: "Can not cast " + getWellKnownType() + " to " + ascendant;
		if (ascendant.is(getContext().getVoid())) {
			// Everything is compatible with void.

			final ObjectIR ir = getWellKnownType().ir(getGenerator());

			return ptr().to(null, dirs.code(), ir.getBodyType()).op(
					getBuilder(),
					getWellKnownType(),
					COMPATIBLE);
		}

		return dynamicCast(id, dirs, ascendant);
	}

	@Override
	public FldOp<?> field(CodeDirs dirs, MemberKey memberKey) {

		final CodeDirs subDirs =
				dirs.begin(FIELD_ID, "Field " + memberKey + " of " + this);
		final ID hostId = FIELD_HOST_ID.sub(memberKey.getMemberId());
		final ObjOp ascendant = cast(
				hostId,
				subDirs,
				memberKey.getOrigin().toObject());
		final FldOp<?> op = ascendant.field(subDirs, memberKey);

		if (!op.isOmitted()) {
			subDirs.code().dumpName("Field: ", op);
		} else {
			subDirs.code().debug("Final field: " + op.getId());
		}
		subDirs.done();

		return op;
	}

	@Override
	public DepOp dep(CodeDirs dirs, Dep dep) {

		final CodeDirs subDirs = dirs.begin(DEP_ID, dep.toString());
		final ObjOp ascendant = cast(
				DEP_HOST_ID.sub(dep),
				subDirs,
				dep.getDeclaredIn());
		final DepOp op = ascendant.dep(subDirs, dep);

		subDirs.done();

		return op;
	}

}
