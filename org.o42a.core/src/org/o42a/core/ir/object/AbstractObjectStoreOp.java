/*
    Compiler Core
    Copyright (C) 2013,2014 Ruslan Lopatin

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

import static org.o42a.codegen.code.AllocationMode.LAZY_ALLOCATION;
import static org.o42a.core.ir.object.ObjectOp.anonymousObject;

import java.util.function.Function;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.DataRecOp;
import org.o42a.core.ir.field.local.LocalIROp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.TargetStoreOp;
import org.o42a.core.object.Obj;
import org.o42a.util.string.ID;


public abstract class AbstractObjectStoreOp
		implements TargetStoreOp {

	private static final AllocatableObjectStore ALLOCATABLE_OBJECT_STORE =
			new AllocatableObjectStore();

	private final ID id;
	private final Allocator allocator;
	private final Function<CodeDirs, DataRecOp> getPtr;

	public AbstractObjectStoreOp(ID id, Code code) {
		this.id = id;
		this.allocator = code.getAllocator();

		final Allocated<DataRecOp> allocated =
				code.allocate(id, ALLOCATABLE_OBJECT_STORE);

		this.getPtr = dirs -> allocated.get(dirs.code());
	}

	public AbstractObjectStoreOp(
			ID id,
			Function<CodeDirs, LocalIROp> getLocal) {
		this.id = id;
		this.allocator = null;
		this.getPtr =
				dirs -> getLocal.apply(dirs).ptr().object(id, dirs.code());
	}

	public abstract Obj getWellKnownType();

	@Override
	public void storeTarget(CodeDirs dirs) {

		final Block code = dirs.code();
		final ObjectOp object = object(dirs, this.allocator);

		ptr(dirs).store(code, object.toData(null, code));
	}

	@Override
	public ObjectOp loadTarget(CodeDirs dirs) {

		final Block code = dirs.code();
		final DataOp objectPtr =
				ptr(dirs).load(null, code).toData(null, code);

		return anonymousObject(
				dirs,
				objectPtr,
				getWellKnownType());
	}

	@Override
	public String toString() {
		if (this.id == null) {
			return super.toString();
		}
		return this.id.toString();
	}

	protected abstract ObjectOp object(CodeDirs dirs, Allocator allocator);

	private final DataRecOp ptr(CodeDirs dirs) {
		return this.getPtr.apply(dirs);
	}

	private static final class AllocatableObjectStore
			implements Allocatable<DataRecOp> {

		@Override
		public AllocationMode getAllocationMode() {
			return LAZY_ALLOCATION;
		}

		@Override
		public DataRecOp allocate(
				Allocations code,
				Allocated<DataRecOp> allocated) {
			return code.allocateDataPtr();
		}

		@Override
		public void init(Code code, DataRecOp allocated) {
		}

		@Override
		public void dispose(Code code, Allocated<DataRecOp> allocated) {
		}

	}

}
