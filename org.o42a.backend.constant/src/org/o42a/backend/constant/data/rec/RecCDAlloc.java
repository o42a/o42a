/*
    Constant Handler Compiler Back-end
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import org.o42a.backend.constant.code.op.OpBE;
import org.o42a.backend.constant.data.CDAlloc;
import org.o42a.backend.constant.data.ContainerCDAlloc;
import org.o42a.backend.constant.data.TopLevelCDAlloc;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.PtrOp;
import org.o42a.codegen.data.*;
import org.o42a.util.func.Getter;


public abstract class RecCDAlloc<
		R extends Rec<P, T>,
		P extends PtrOp<P>,
		T> extends CDAlloc<P, R> implements Content<R> {

	private final TopLevelCDAlloc<?> topLevel;
	private final ContainerCDAlloc<?> enclosing;
	private Getter<T> value;

	public RecCDAlloc(
			ContainerCDAlloc<?> enclosing,
			R data,
			RecCDAlloc<R, P, T> typeAllocation) {
		super(enclosing.getBackend(), data, typeAllocation);
		this.topLevel = enclosing.getTopLevel();
		this.enclosing = enclosing;
	}

	@Override
	public final TopLevelCDAlloc<?> getTopLevel() {
		return this.topLevel;
	}

	@Override
	public final ContainerCDAlloc<?> getEnclosing() {
		return this.enclosing;
	}

	public final Getter<T> getValue() {
		return this.value;
	}

	public void setValue(Getter<T> value) {
		this.value = value;
	}

	public abstract Getter<T> underlyingValue(Getter<T> value);

	@Override
	public void allocated(R instance) {
	}

	@Override
	public void fill(R instance) {
		getUnderlying()
		.setAttributes(getData())
		.setValue(underlyingValue(getValue()));
	}

	@Override
	public final P op(CodeId id, AllocClass allocClass, CodeWriter writer) {
		return op(
				new OpBE<P>(id, cast(writer)) {
					@Override
					public void prepare() {
					}
					@Override
					protected P write() {
						return getUnderlyingPtr().op(
								getId(),
								part().underlying());
					}
				},
				allocClass);
	}

	@Override
	protected final R allocateUnderlying(SubData<?> container) {
		return allocateUnderlying(
				container,
				getData().getId().getLocal().getId());
	}

	protected abstract R allocateUnderlying(SubData<?> container, String name);

	protected abstract P op(OpBE<P> backend, AllocClass allocClass);

}
