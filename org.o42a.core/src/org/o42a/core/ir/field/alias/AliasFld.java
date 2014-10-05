/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.core.ir.field.alias;

import static org.o42a.analysis.use.User.dummyUser;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.OpMeans;
import org.o42a.core.ir.field.*;
import org.o42a.core.ir.field.RefFld.StatefulOp;
import org.o42a.core.ir.field.RefFld.StatefulType;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIRBody;
import org.o42a.core.ir.object.op.ObjectRefFn;
import org.o42a.core.member.alias.AliasField;
import org.o42a.core.object.Obj;


public class AliasFld extends RefFld<StatefulOp, StatefulType, ObjectRefFn> {

	public AliasFld(
			ObjectIRBody bodyIR,
			AliasField field,
			boolean dummy,
			Obj target) {
		super(bodyIR, field, dummy, target, target);
	}

	@Override
	public FldKind getKind() {
		return FldKind.ALIAS;
	}

	@Override
	protected StatefulType getType() {
		return STATEFUL_FLD;
	}

	@Override
	protected Obj targetType(Obj bodyType) {
		return bodyType.member(getField().getKey())
				.toField()
				.object(dummyUser());
	}

	@Override
	protected AliasVmtRecord createVmtRecord() {
		return new AliasVmtRecord(this);
	}

	@Override
	protected AliasFldOp op(Code code, ObjOp host, OpMeans<StatefulOp> ptr) {
		return new AliasFldOp(host, this, ptr);
	}

}
