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
package org.o42a.core.ir.field.link;

import org.o42a.codegen.code.Code;
import org.o42a.core.ir.field.FldKind;
import org.o42a.core.ir.field.RefFld.StatefulOp;
import org.o42a.core.ir.field.RefFld.StatefulType;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIRBody;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;


public class EagerLinkFld extends AbstractLinkFld<StatefulOp, StatefulType> {

	public EagerLinkFld(
			ObjectIRBody bodyIR,
			Field field,
			boolean dummy,
			Obj target,
			Obj targetAscendant) {
		super(bodyIR, field, dummy, target, targetAscendant);
	}

	@Override
	public final FldKind getKind() {
		return FldKind.ALIAS;
	}

	@Override
	protected StatefulType getType() {
		return STATEFUL_FLD;
	}

	@Override
	protected EagerLinkVmtRecord createVmtRecord() {
		return new EagerLinkVmtRecord(this);
	}

	@Override
	protected EagerLinkFldOp op(Code code, ObjOp host, StatefulOp ptr) {
		return new EagerLinkFldOp(host, this, ptr);
	}

}
