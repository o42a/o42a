/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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

import static org.o42a.core.ir.object.ObjectIRStruct.OBJECT_ID;
import static org.o42a.core.ir.object.ObjectOp.anonymousObject;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.RelOp;
import org.o42a.codegen.data.RelPtr;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.object.desc.ObjectIRDescOp;
import org.o42a.core.ir.op.DefiniteIROp;
import org.o42a.core.ir.value.ObjectValueFn;
import org.o42a.core.object.Obj;
import org.o42a.util.string.ID;


public final class ObjectDataOp extends DefiniteIROp<ObjectIRDataOp> {

	private static final ID START_OFFSET_ID = ID.rawId("start_offset");

	private static ObjectStartOffset startOffset;

	private final ObjectIRDataOp ptr;

	ObjectDataOp(CodeBuilder builder, ObjectIRDataOp ptr) {
		super(builder);
		this.ptr = ptr;
	}

	@Override
	public final ObjectIRDataOp ptr() {
		return this.ptr;
	}

	public final ObjectIRDescOp loadDesc(Code code) {
		return ptr().desc(code).load(null, code);
	}

	public final ObjectOp object(Code code, Obj wellKnownType) {
		return anonymousObject(
				getBuilder(),
				code,
				objectPtr(code, wellKnownType),
				wellKnownType);
	}

	public final DataOp objectPtr(Code code, Obj anyObject) {
		return ptr(code)
				.toData(null, code)
				.offset(null, code, startOffset(code, anyObject))
				.toData(OBJECT_ID, code);
	}

	public final void writeValue(DefDirs dirs) {

		final Code code = dirs.code();
		final ObjectValueFn function =
				ptr().valueFunc(code).load(null, code);

		function.call(dirs, objectPtr(code, null));
	}

	@Override
	public String toString() {
		return "ObjectData[" + ptr().toString() + ']';
	}

	private RelOp startOffset(Code code, Obj anyObject) {

		final ObjectStartOffset cached = startOffset;

		if (cached != null && cached.generator == getGenerator()) {
			return cached.get(code);
		}

		final Obj object;

		if (anyObject != null) {
			object = anyObject;
		} else {
			object = getBuilder().getContext().getIntrinsics().getVoid();
		}

		final ObjectIRStruct struct =
				object.ir(getGenerator())
				.getType()
				.allocate();
		final ObjectStartOffset offset = new ObjectStartOffset(
				getGenerator(),
				struct.pointer(getGenerator())
				.relativeTo(struct.objectData().pointer(getGenerator())));

		startOffset = offset;

		return offset.get(code);
	}

	private static final class ObjectStartOffset {

		private final Generator generator;
		private final RelPtr offset;

		ObjectStartOffset(Generator generator, RelPtr offset) {
			this.generator = generator;
			this.offset = offset;
		}

		final RelOp get(Code code) {
			return this.offset.op(START_OFFSET_ID, code);
		}

	}

}
