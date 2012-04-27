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
package org.o42a.core.ir.field;

import static org.o42a.core.ir.object.ObjectOp.anonymousObject;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ObjectFunc;
import org.o42a.core.member.MemberKey;
import org.o42a.core.object.Obj;


public abstract class RefFldOp<
		S extends RefFld.Op<S, C>,
		C extends ObjectFunc<C>>
				extends FieldFldOp {

	public RefFldOp(RefFld<C> fld, ObjOp host) {
		super(fld, host);
	}

	@SuppressWarnings("unchecked")
	@Override
	public RefFld<C> fld() {
		return (RefFld<C>) super.fld();
	}

	@Override
	public abstract S ptr();

	@Override
	public final FldOp field(CodeDirs dirs, MemberKey memberKey) {
		return target(dirs).field(dirs, memberKey);
	}

	public ObjectOp target(CodeDirs dirs) {
		if (isOmitted()) {

			final ObjectIR targetIR = fld().getTarget().ir(getGenerator());

			return targetIR.op(getBuilder(), dirs.code());
		}

		final Block code = dirs.code();
		final FldKind kind = fld().getKind();

		code.dumpName(kind + " field: ", this);
		code.dumpName(kind + " host: ", host());

		final DataOp ptr = ptr().target(code, host());
		final Obj hostAscendant = host().getAscendant();
		final Obj targetType = fld().targetType(hostAscendant);

		if (host().getPrecision().isExact()) {

			final ObjectBodyIR.Op targetBodyPtr = ptr.to(
					null,
					code,
					fld().getTargetAscendant()
					.ir(getGenerator()).getBodyType());

			return targetBodyPtr.op(
					getBuilder(),
					fld().getTargetAscendant(),
					ObjectPrecision.EXACT);
		}

		return anonymousObject(getBuilder(), ptr, targetType);
	}

	@Override
	public final ObjectOp materialize(CodeDirs dirs) {
		return target(dirs);
	}

}
