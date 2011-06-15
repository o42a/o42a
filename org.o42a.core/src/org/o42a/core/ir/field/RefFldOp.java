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
package org.o42a.core.ir.field;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.field.RefFld.Op;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectBodyIR;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ObjectFunc;
import org.o42a.core.member.MemberKey;


public abstract class RefFldOp<C extends ObjectFunc<C>> extends FldOp {

	RefFldOp(RefFld<C> fld, ObjOp host, RefFld.Op<C> ptr) {
		super(fld, host, ptr);
	}

	@SuppressWarnings("unchecked")
	@Override
	public RefFld<C> fld() {
		return (RefFld<C>) super.fld();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Op<C> ptr() {
		return (Op<C>) super.ptr();
	}

	@Override
	public final ObjOp toObject(CodeDirs dirs) {

		final Artifact<?> artifact = fld().getField().getArtifact();

		if (artifact.getKind() == ArtifactKind.OBJECT) {
			return target(dirs);
		}

		return null;
	}

	@Override
	public final FldOp field(CodeDirs dirs, MemberKey memberKey) {

		final Artifact<?> artifact = fld().getField().getArtifact();

		if (artifact.getKind() == ArtifactKind.OBJECT) {
			return target(dirs).field(dirs, memberKey);
		}

		return null;
	}

	public ObjOp target(CodeDirs dirs) {

		final Code code = dirs.code();
		final FldKind kind = fld().getKind();

		code.dumpName(kind + " field: ", ptr());
		code.dumpName(kind + " host: ", host().ptr());

		final DataOp ptr = ptr().target(code, host());
		final Obj targetAscendant = fld().getTargetAscendant();
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

		final ObjectBodyIR.Op targetBodyPtr = ptr.to(
				null,
				code, targetAscendant.ir(getGenerator()).getBodyType());

		return targetBodyPtr.op(
				getBuilder(),
				targetType,
				host().getPrecision());
	}

	@Override
	public final ObjOp materialize(CodeDirs dirs) {
		return target(dirs);
	}

}
