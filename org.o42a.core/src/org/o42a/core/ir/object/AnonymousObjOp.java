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

import static org.o42a.core.ir.IRUtil.encodeMemberId;
import static org.o42a.core.ir.object.ObjectPrecision.COMPATIBLE;
import static org.o42a.core.ir.object.ObjectPrecision.DERIVED;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.value.struct.ValueIR;
import org.o42a.core.ir.value.struct.ValueOp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.local.Dep;
import org.o42a.core.object.Obj;


final class AnonymousObjOp extends ObjectOp {

	private final DataOp ptr;
	private final Obj wellKnownType;
	private ValueOp value;

	AnonymousObjOp(ObjectTypeOp data, DataOp ptr, Obj wellKnownType) {
		super(data);
		this.ptr = ptr;
		assert wellKnownType != null :
			"Object type not specified";
		this.wellKnownType = wellKnownType;
	}

	AnonymousObjOp(CodeBuilder builder, DataOp ptr, Obj wellKnownType) {
		super(builder, DERIVED);
		this.ptr = ptr;
		assert wellKnownType != null :
			"Object type not specified";
		this.wellKnownType = wellKnownType;
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
	public final ValueOp value() {
		if (this.value != null) {
			return this.value;
		}

		final ValueIR<?> valueIR =
				getWellKnownType().ir(getGenerator()).getValueIR();

		return this.value = valueIR.op(this);
	}

	@Override
	public ObjOp cast(CodeId id, CodeDirs dirs, Obj ascendant) {
		getWellKnownType().assertDerivedFrom(ascendant);
		if (ascendant == getContext().getVoid()) {
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
	public FldOp field(CodeDirs dirs, MemberKey memberKey) {

		final CodeDirs subDirs =
				dirs.begin("field", "Field " + memberKey + " of " + this);
		final Code code = subDirs.code();
		final CodeId hostId =
				code.id("field_host")
				.sub(encodeMemberId(getGenerator(), memberKey.getMemberId()));
		final ObjOp ascendant = cast(
				hostId,
				subDirs,
				memberKey.getOrigin().toObject());
		final FldOp op = ascendant.field(subDirs, memberKey);

		if (!op.isOmitted()) {
			subDirs.code().dumpName("Field: ", op);
		} else {
			subDirs.code().debug("Final field: " + op.getId());
		}
		subDirs.end();

		return op;
	}

	@Override
	public DepOp dep(CodeDirs dirs, Dep dep) {

		final CodeDirs subDirs = dirs.begin("dep", dep.toString());
		final Code code = subDirs.code();
		final String depName = dep.getName();
		final CodeId hostId = code.id("dep_host");
		final ObjOp ascendant = cast(
				depName != null ? hostId.sub(depName) : hostId,
				subDirs,
				dep.getObject());
		final DepOp op = ascendant.dep(subDirs, dep);

		subDirs.code().dumpName("Dep: ", op);
		subDirs.end();

		return op;
	}

}
