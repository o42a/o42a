/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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

import org.o42a.codegen.code.Block;
import org.o42a.core.ir.field.RefFld.StatelessOp;
import org.o42a.core.ir.field.RefFld.StatelessType;
import org.o42a.core.ir.object.ObjBuilder;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;


class LinkVmtRecord
		extends AbstractLinkVmtRecord<StatelessOp, StatelessType> {

	LinkVmtRecord(LinkFld fld) {
		super(fld);
	}

	@Override
	protected void buildConstructor(ObjBuilder builder, CodeDirs dirs) {

		final Block code = dirs.code();
		final ObjectOp result = construct(builder, dirs);

		result.toData(null, code).returnValue(code);
	}

}
