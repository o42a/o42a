/*
    Intrinsics
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.intrinsic.root;

import static org.o42a.core.ir.object.ObjectIRType.OBJECT_TYPE;
import static org.o42a.util.use.SimpleUsage.ALL_SIMPLE_USAGES;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Struct;
import org.o42a.codegen.data.StructRec;
import org.o42a.codegen.data.SubData;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectIRType;


final class IntrinsicsIR extends Struct<IntrinsicsIR.Op> {

	private final Root root;

	private StructRec<ObjectIRType.Op> rootType;
	private StructRec<ObjectIRType.Op> voidType;
	private StructRec<ObjectIRType.Op> falseType;
	private StructRec<ObjectIRType.Op> integerType;
	private StructRec<ObjectIRType.Op> floatType;
	private StructRec<ObjectIRType.Op> stringType;

	IntrinsicsIR(Root root) {
		this.root = root;
	}

	@Override
	public Op op(StructWriter<Op> writer) {
		return new Op(writer);
	}

	@Override
	protected CodeId buildCodeId(CodeIdFactory factory) {
		return factory.rawId("o42a");
	}

	@Override
	protected void allocate(SubData<Op> data) {
		this.rootType = data.addPtr("root_type", OBJECT_TYPE);
		this.voidType = data.addPtr("void_type", OBJECT_TYPE);
		this.falseType = data.addPtr("false_type", OBJECT_TYPE);
		this.integerType = data.addPtr("integer_type", OBJECT_TYPE);
		this.floatType = data.addPtr("float_type", OBJECT_TYPE);
		this.stringType = data.addPtr("string_type", OBJECT_TYPE);
	}

	@Override
	protected void fill() {
		set(this.rootType, this.root);
		set(this.voidType, this.root.getContext().getVoid());
		set(this.falseType, this.root.getContext().getFalse());
		set(this.integerType, this.root.getInteger());
		set(this.floatType, this.root.getFloat());
		set(this.stringType, this.root.getString());
	}

	private void set(StructRec<ObjectIRType.Op> ptr, Obj object) {
		if (!object.content().toUser().isUsed(
				getGenerator(),
				ALL_SIMPLE_USAGES)) {
			ptr.setNull();
			return;
		}

		final ObjectIR ir = object.ir(getGenerator());

		ptr.setValue(ir.getTypeIR().getObjectType().pointer(getGenerator()));
	}

	static final class Op extends StructOp<Op> {

		private Op(StructWriter<Op> writer) {
			super(writer);
		}

	}

}
