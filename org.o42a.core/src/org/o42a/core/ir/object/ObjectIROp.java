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
package org.o42a.core.ir.object;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Type;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.object.Obj;


public final class ObjectIROp extends StructOp<ObjectIROp> {

	public ObjectIROp(StructWriter<ObjectIROp> writer) {
		super(writer);
	}

	@Override
	public final ObjectIRStruct getType() {
		return (ObjectIRStruct) super.getType();
	}

	public final Obj getSampleDeclaration() {
		return getType().getObjectIR().getSampleDeclaration();
	}

	public final ObjectIRDataOp objectData(Code code) {
		return struct(null, code, getType().objectData());
	}

	public final ObjOp op(
			CodeBuilder builder,
			Obj ascendant,
			ObjectPrecision precision) {
		assert ascendant.assertDerivedFrom(getType().getObjectIR().getObject());
		return new ObjOp(
				builder,
				ascendant.ir(builder.getGenerator()),
				this,
				ascendant,
				precision);
	}

	public final <O extends StructOp<O>> O field(Code code, Type<O> instance) {
		return struct(null, code, instance);
	}

	final ObjOp op(CodeBuilder builder, ObjectIR objectIR) {
		return new ObjOp(builder, objectIR, this);
	}

}
