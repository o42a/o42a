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

import static org.o42a.core.ir.object.ObjectPrecision.COMPATIBLE;
import static org.o42a.core.ir.object.ObjectPrecision.DERIVED;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.PtrOp;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.local.Dep;


final class AnonymousObjOp extends ObjectOp {

	private final Obj wellKnownType;

	AnonymousObjOp(ObjectDataOp data, AnyOp ptr, Obj wellKnownType) {
		super(ptr, data);
		this.wellKnownType = wellKnownType;
	}

	AnonymousObjOp(
			CodeBuilder builder,
			PtrOp ptr,
			Obj wellKnownType) {
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
	public final AnyOp ptr() {
		return (AnyOp) super.ptr();
	}

	@Override
	public ObjOp cast(Code code, CodePos exit, Obj ascendant) {
		getWellKnownType().assertDerivedFrom(ascendant);
		if (ascendant == getContext().getVoid()) {
			// anything is compatible with void

			final ObjectIR ir = getWellKnownType().ir(getGenerator());

			return ptr().to(code, ir.getBodyType()).op(
					getBuilder(),
					getWellKnownType(),
					COMPATIBLE);
		}

		return dynamicCast(code, ascendant);
	}

	@Override
	public FldOp field(Code code, CodePos exit, MemberKey memberKey) {

		final ObjOp ascendant =
			cast(code, exit, memberKey.getOrigin().getContainer().toObject());

		return ascendant.field(code, exit, memberKey);
	}

	@Override
	public DepOp dep(Code code, CodePos exit, Dep dep) {

		final ObjOp ascendant = cast(code, exit, dep.getObject());

		return ascendant.dep(code, exit, dep);
	}

}
