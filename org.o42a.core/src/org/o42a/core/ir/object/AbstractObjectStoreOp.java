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
package org.o42a.core.ir.object;

import static org.o42a.core.ir.object.ObjectOp.anonymousObject;

import org.o42a.codegen.code.Allocator;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.AnyRecOp;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.TargetOp;
import org.o42a.core.ir.op.TargetStoreOp;
import org.o42a.core.object.Obj;
import org.o42a.util.string.ID;


public abstract class AbstractObjectStoreOp implements TargetStoreOp {

	private final Allocator allocator;
	private final AnyRecOp ptr;

	public AbstractObjectStoreOp(ID id, Code code) {
		this.allocator = code.getAllocator();
		this.ptr = code.allocatePtr(id);
	}

	public abstract Obj getWellKnownType();

	public final Allocator getAllocator() {
		return this.allocator;
	}

	@Override
	public void storeTarget(CodeDirs dirs) {

		final Block code = dirs.code();
		final ObjectOp object = object(dirs);

		this.ptr.store(code, object.toAny(null, code));
	}

	@Override
	public TargetOp loadTarget(CodeDirs dirs) {

		final Block code = dirs.code();
		final DataOp objectPtr = this.ptr.load(null, code).toData(null, code);

		return anonymousObject(
				dirs.getBuilder(),
				objectPtr,
				getWellKnownType());
	}

	@Override
	public String toString() {
		if (this.ptr == null) {
			return super.toString();
		}
		return this.ptr.toString();
	}

	protected abstract ObjectOp object(CodeDirs dirs);

}
