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
package org.o42a.core.ir.field.object;

import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.field.RefFldOp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.op.CodeDirs;


public class ObjFldOp extends RefFldOp<ObjFld.Op, ObjectConstructorFunc> {

	ObjFldOp(ObjFld fld, ObjOp host, ObjFld.Op ptr) {
		super(fld, host, ptr);
	}

	@Override
	public ObjFld fld() {
		return (ObjFld) super.fld();
	}

	@Override
	public void assign(CodeDirs dirs, HostOp value) {
		throw new UnsupportedOperationException();
	}

}
