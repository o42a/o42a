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
package org.o42a.core.ir.object;

import static org.o42a.core.ir.object.ObjectDataIR.OBJECT_DESC_ID;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.code.op.StructRecOp;
import org.o42a.util.string.ID;


public final class ObjectIRDescOp extends StructOp<ObjectIRDescOp> {

	ObjectIRDescOp(StructWriter<ObjectIRDescOp> writer) {
		super(writer);
	}

	@Override
	public final ObjectIRDesc getType() {
		return (ObjectIRDesc) super.getType();
	}

	public final StructRecOp<ObjectIRDataOp> data(Code code) {
		return ptr(null, code, getType().data());
	}

	@Override
	protected ID fieldId(Code code, ID local) {
		return OBJECT_DESC_ID.setLocal(local);
	}

}