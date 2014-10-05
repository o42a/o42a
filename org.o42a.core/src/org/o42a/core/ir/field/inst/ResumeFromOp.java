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
package org.o42a.core.ir.field.inst;

import org.o42a.codegen.code.op.OpMeans;
import org.o42a.core.ir.field.FldIR;
import org.o42a.core.ir.field.inst.ResumeFromFld.Op;
import org.o42a.core.ir.field.inst.ResumeFromFld.Type;
import org.o42a.core.ir.object.ObjOp;


public class ResumeFromOp extends InstFldOp<Op, Type> {

	ResumeFromOp(
			ObjOp host,
			FldIR<Op, Type> fld,
			OpMeans<ResumeFromFld.Op> ptr) {
		super(host, fld, ptr);
	}

}
