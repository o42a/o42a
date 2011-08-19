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

import static org.o42a.backend.constant.data.ConstBackend.cast;

import org.o42a.backend.constant.code.CCode;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.PtrOp;
import org.o42a.codegen.data.*;


public abstract class RecCDAlloc<
		R extends Rec<P, T>,
		P extends PtrOp<P>,
		T> extends CDAlloc<P, R> {

	private final TopLevelCDAlloc<?> topLevel;
	private final ContainerCDAlloc<?> enclosing;
	private final R data;

	public RecCDAlloc(
			ContainerCDAlloc<?> enclosing,
			R data,
			CDAlloc<P, R> type) {
		super(type);
		this.topLevel = enclosing.getTopLevel();
		this.enclosing = enclosing;
		this.data = data;
		enclosing.nest(this);
	}

	public RecCDAlloc(
			ContainerCDAlloc<?> enclosing,
			Ptr<P> underlyingPtr) {
		super(underlyingPtr);
		this.topLevel = enclosing.getTopLevel();
		this.enclosing = enclosing;
		this.data = null;
	}

	@Override
	public final TopLevelCDAlloc<?> getTopLevel() {
		return this.topLevel;
	}

	@Override
	public final ContainerCDAlloc<?> getEnclosing() {
		return this.enclosing;
	}

	public final R getData() {
		return this.data;
	}

	@Override
	public final P op(CodeId id, AllocClass allocClass, CodeWriter writer) {

		final CCode<?> ccode = cast(writer);
		final P underlyingOp =
				getUnderlyingPtr().op(id, ccode.getUnderlying());

		return op(ccode, underlyingOp);
	}

	@Override
	protected final R allocateUnderlying(SubData<?> container) {
		return allocateUnderlying(
				container,
				getData().getId().getLocal().getId());
	}

	protected abstract R allocateUnderlying(SubData<?> container, String name);

	protected abstract P op(CCode<?> code, P underlying);

}
