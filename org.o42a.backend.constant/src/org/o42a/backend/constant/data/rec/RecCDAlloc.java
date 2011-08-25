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
package org.o42a.backend.constant.data.rec;

import static org.o42a.backend.constant.data.ConstBackend.cast;

import org.o42a.backend.constant.code.CCode;
import org.o42a.backend.constant.data.*;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.PtrOp;
import org.o42a.codegen.data.*;


public abstract class RecCDAlloc<
		R extends Rec<P, T>,
		P extends PtrOp<P>,
		T> extends CDAlloc<P, R> implements Content<R> {

	private final TopLevelCDAlloc<?> topLevel;
	private final ContainerCDAlloc<?> enclosing;
	private T value;
	private boolean constant;

	public RecCDAlloc(
			ContainerCDAlloc<?> enclosing,
			R data,
			RecCDAlloc<R, P, T> typeAllocation) {
		super(enclosing.getBackend(), data, typeAllocation);
		this.topLevel = enclosing.getTopLevel();
		this.enclosing = enclosing;
	}

	public RecCDAlloc(ConstBackend backend, Ptr<P> underlyingPtr) {
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

	public final boolean isConstant() {
		return this.constant;
	}

	public final void setConstant(boolean constant) {
		this.constant = constant;
	}

	public final T getValue() {
		return this.value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	@Override
	public void allocated(R instance) {
	}

	@Override
	public void fill(R instance) {
		getUnderlying().setConstant(isConstant()).setValue(getValue());
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
