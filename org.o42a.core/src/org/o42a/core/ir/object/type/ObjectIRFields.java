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
package org.o42a.core.ir.object.type;

import static org.o42a.core.ir.object.type.FieldDescIR.FIELD_DESC_IR;

import org.o42a.codegen.data.Ptr;
import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.field.FldIR;
import org.o42a.core.ir.op.RelList;
import org.o42a.util.string.ID;


final class ObjectIRFields extends RelList<FieldDescIR> {

	private static final ID PREFIX_ID = ID.id("field");

	@Override
	protected Ptr<?> allocateItem(
			SubData<?> data,
			int index,
			FieldDescIR item) {

		final FldIR fld = item.fld();
		final ID id = PREFIX_ID.detail(fld.getId().getLocal());
		final FieldDescIR.Type desc =
				data.addInstance(id, FIELD_DESC_IR, item);

		return desc.data(data.getGenerator()).getPointer();
	}

}
