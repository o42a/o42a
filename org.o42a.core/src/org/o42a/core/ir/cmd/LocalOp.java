/*
    Compiler Core
    Copyright (C) 2013 Ruslan Lopatin

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
package org.o42a.core.ir.cmd;

import static org.o42a.core.ir.object.ObjectOp.anonymousObject;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;

import org.o42a.codegen.code.Allocator;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.AnyRecOp;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.st.sentence.Local;
import org.o42a.util.string.ID;


public final class LocalOp {

	static LocalOp allocateLocal(CodeDirs dirs, Local local, RefOp ref) {

		final ID id = local.getMemberId().toID();
		final Block code = dirs.code();
		final Allocator allocator = code.getAllocator();
		final Code allocation = allocator.allocation();
		final AnyRecOp ptr = allocation.allocateNull(id);

		ptr.store(
				code,
				ref.path()
				.target()
				.materialize(dirs, tempObjHolder(allocator).toVolatile())
				.toAny(null, code));

		return new LocalOp(local, ptr);
	}

	private final Local local;
	private final AnyRecOp ptr;

	private LocalOp(Local local, AnyRecOp ptr) {
		this.local = local;
		this.ptr = ptr;
	}

	public final ValOp writeValue(ValDirs dirs) {
		return target(dirs.dirs()).value().writeValue(dirs);
	}

	public final void writeCond(CodeDirs dirs) {
		target(dirs).value().writeCond(dirs);
	}

	public final ObjectOp target(CodeDirs dirs) {
		this.ptr.getAllocPlace().ensureAccessibleFrom(dirs.code());

		final Block code = dirs.code();
		final DataOp objectPtr = this.ptr.load(null, code).toData(null, code);

		objectPtr.isNull(null, code).go(code, dirs.falseDir());

		return anonymousObject(
				dirs.getBuilder(),
				objectPtr,
				this.local.ref().getResolution().toObject());
	}

	@Override
	public String toString() {
		if (this.local == null) {
			return super.toString();
		}
		return this.local.toString();
	}

}
