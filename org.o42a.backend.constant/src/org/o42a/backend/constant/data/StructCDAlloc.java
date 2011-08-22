/*
    Constant Handler Compiler Back-end
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
package org.o42a.backend.constant.data;

import org.o42a.backend.constant.data.struct.CType;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Ptr;


public abstract class StructCDAlloc<S extends StructOp<S>>
		extends ContainerCDAlloc<S> {

	private final TopLevelCDAlloc<?> topLevel;
	private final ContainerCDAlloc<?> enclosing;

	public StructCDAlloc(
			ContainerCDAlloc<?> enclosing,
			ContainerCDAlloc<S> typeAllocation,
			CType<S> underlyingStruct) {
		super(enclosing.getBackend(), typeAllocation, underlyingStruct);
		this.topLevel = enclosing.getTopLevel();
		this.enclosing = enclosing;
		enclosing.nest(this);
	}

	public StructCDAlloc(ConstBackend backend, Ptr<S> underlyingPtr) {
		super(backend, underlyingPtr);
		this.topLevel = null;
		this.enclosing = null;
	}

	@Override
	public final TopLevelCDAlloc<?> getTopLevel() {
		return this.topLevel;
	}

	@Override
	public final ContainerCDAlloc<?> getEnclosing() {
		return this.enclosing;
	}

}
