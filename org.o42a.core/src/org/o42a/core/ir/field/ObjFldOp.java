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

import org.o42a.core.ir.object.ObjOp;


public class ObjFldOp extends RefFldOp {

	ObjFldOp(ObjFld fld, ObjOp host, ObjFld.Op ptr) {
		super(fld, host, ptr);
	}

	@Override
	public ObjFld fld() {
		return (ObjFld) super.fld();
	}

	@Override
	public ObjFld.Op ptr() {
		return (ObjFld.Op) super.ptr();
	}

}
