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
package org.o42a.core.ir.field;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.field.RefFld.Op;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectBodyIR;
import org.o42a.core.member.MemberKey;


public abstract class RefFldOp extends FldOp {

	RefFldOp(RefFld fld, ObjOp host, RefFld.Op ptr) {
		super(fld, host, ptr);
	}

	@Override
	public RefFld fld() {
		return (RefFld) super.fld();
	}

	@Override
	public Op ptr() {
		return (Op) super.ptr();
	}

	@Override
	public final ObjOp toObject(Code code, CodePos exit) {

		final Artifact<?> artifact = fld().getField().getArtifact();

		if (artifact.getKind() == ArtifactKind.OBJECT) {
			return target(code);
		}

		return null;
	}

	@Override
	public final FldOp field(Code code, CodePos exit, MemberKey memberKey) {

		final Artifact<?> artifact = fld().getField().getArtifact();

		if (artifact.getKind() == ArtifactKind.OBJECT) {
			return target(code).field(code, exit, memberKey);
		}

		return null;
	}

	public ObjOp target(Code code) {
		code.dumpName("Link field: ", ptr());
		code.dumpName("Link host: ", host().ptr());

		final AnyOp ptr = ptr().target(code, host().toAny(code));
		final Obj targetAscendant = fld().getTargetAscendant();
		final ObjectBodyIR.Op bodyPtr = ptr.to(
				code,
				targetAscendant.ir(getGenerator()).getBodyType());
		final Obj targetType;

		final Obj hostAscendant = host().getAscendant();
		final Obj hostPtrAscendant = host().ptr().getAscendant();

		if (hostAscendant == hostPtrAscendant) {
			targetType = targetAscendant;
		} else if (fld().getBodyIR().getAscendant() == hostPtrAscendant) {
			targetType = fld().targetType(hostAscendant);
		} else {
			targetType = targetAscendant;
		}

		return bodyPtr.op(getBuilder(), targetType, host().getPrecision());
	}

	@Override
	public final ObjOp materialize(Code code, CodePos exit) {
		return target(code);
	}

}
