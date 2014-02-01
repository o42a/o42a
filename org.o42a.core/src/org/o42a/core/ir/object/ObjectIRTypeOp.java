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

import static org.o42a.core.ir.object.ObjectTypeIR.OBJECT_TYPE_ID;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.util.string.ID;


public final class ObjectIRTypeOp extends StructOp<ObjectIRTypeOp> {

	ObjectIRTypeOp(StructWriter<ObjectIRTypeOp> writer) {
		super(writer);
	}

	@Override
	public final ObjectIRType getType() {
		return (ObjectIRType) super.getType();
	}

	public final ObjectIRDataOp data(Code code) {
		return struct(null, code, getType().data());
	}

	public final ObjectTypeOp op(
			CodeBuilder builder,
			ObjectPrecision precision) {
		return new ObjectTypeOp(builder, this, precision);
	}

	@Override
	protected ID fieldId(Code code, ID local) {
		return OBJECT_TYPE_ID.setLocal(local);
	}

}
