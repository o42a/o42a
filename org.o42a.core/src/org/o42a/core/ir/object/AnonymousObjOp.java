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

import static org.o42a.core.ir.object.ObjectPrecision.COMPATIBLE;
import static org.o42a.core.ir.object.ObjectPrecision.DERIVED;

import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.local.Dep;


final class AnonymousObjOp extends ObjectOp {

	private final Obj wellKnownType;

	AnonymousObjOp(ObjectTypeOp data, DataOp ptr, Obj wellKnownType) {
		super(ptr, data);
		this.wellKnownType = wellKnownType;
	}

	AnonymousObjOp(CodeBuilder builder, DataOp ptr, Obj wellKnownType) {
		super(builder, ptr, DERIVED);
		this.wellKnownType =
			wellKnownType != null
			? wellKnownType : builder.getContext().getVoid();
	}

	@Override
	public final Obj getWellKnownType() {
		return this.wellKnownType;
	}

	@Override
	public final DataOp ptr() {
		return (DataOp) super.ptr();
	}

	@Override
	public ObjOp cast(CodeDirs dirs, Obj ascendant) {
		getWellKnownType().assertDerivedFrom(ascendant);
		if (ascendant == getContext().getVoid()) {
			// anything is compatible with void

			final ObjectIR ir = getWellKnownType().ir(getGenerator());

			return ptr().to(dirs.code(), ir.getBodyType()).op(
					getBuilder(),
					getWellKnownType(),
					COMPATIBLE);
		}

		return dynamicCast(dirs, ascendant);
	}

	@Override
	public FldOp field(CodeDirs dirs, MemberKey memberKey) {
		dirs = dirs.begin("field", "Field " + memberKey + " of " + this);

		final ObjOp ascendant = cast(
				dirs,
				memberKey.getOrigin().getContainer().toObject());
		final FldOp op = ascendant.field(dirs, memberKey);

		dirs.code().dumpName("Field: ", op.ptr());
		dirs.end();

		return op;
	}

	@Override
	public DepOp dep(CodeDirs dirs, Dep dep) {
		dirs = dirs.begin("dep", "Dep " + dep + " of " + this);

		final ObjOp ascendant = cast(dirs, dep.getObject());
		final DepOp op = ascendant.dep(dirs, dep);

		dirs.code().dumpName("Dep: ", op.ptr());
		dirs.end();

		return op;
	}

}
