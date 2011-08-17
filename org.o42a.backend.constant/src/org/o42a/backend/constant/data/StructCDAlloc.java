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

import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.DataLayout;


public abstract class StructCDAlloc<S extends StructOp<S>>
		extends ContainerCDAlloc<S> {

	private final TopLevelCDAlloc<?> topLevel;
	private final ContainerCDAlloc<?> enclosing;
	private final ContainerCDAlloc<?> type;

	public StructCDAlloc(
			ContainerCDAlloc<?> enclosing,
			ContainerCDAlloc<S> type) {
		super(type);
		this.topLevel = enclosing.getTopLevel();
		this.enclosing = enclosing;
		this.type = type;
		enclosing.nest(this);
	}

	@Override
	public DataLayout getLayout() {
		return this.type != null ? this.type.getLayout() : getUnderlying().getLayout();
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
