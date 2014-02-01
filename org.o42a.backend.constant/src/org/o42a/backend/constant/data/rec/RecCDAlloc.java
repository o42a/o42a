/*
    Constant Handler Compiler Back-end
    Copyright (C) 2011-2014 Ruslan Lopatin

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
import org.o42a.backend.constant.code.op.OpBE;
import org.o42a.backend.constant.data.ContainerCDAlloc;
import org.o42a.backend.constant.data.DCDAlloc;
import org.o42a.backend.constant.data.TopLevelCDAlloc;
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.AllocPtrOp;
import org.o42a.codegen.data.*;
import org.o42a.util.fn.Getter;
import org.o42a.util.string.ID;


public abstract class RecCDAlloc<
		R extends Rec<P, T>,
		P extends AllocPtrOp<P>,
		T> extends DCDAlloc<P, R> implements Getter<T> {

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
	public T get() {
		return underlyingValue(getValue().get());
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
		if (isUnderlyingAllocated()) {
			getUnderlying().setAttributes(getData());
		}
		this.value = value;
	}

	public abstract T underlyingValue(T value);

	@Override
	public final P op(ID id, AllocClass allocClass, CodeWriter writer) {

		final CCode<?> ccode = cast(writer);

		return op(
				new OpBE<P>(id, ccode) {
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
				allocClass.allocPlace(ccode.code()));
	}

	@Override
	protected R allocateUnderlying(SubData<?> container) {

		final R underlying = allocateUnderlying(
				container,
				getData().getId().getLocal().toString());

		underlying.setAttributes(getData());
		underlying.setValue(this);

		return underlying;
	}

	protected abstract R allocateUnderlying(SubData<?> container, String name);

	protected abstract P op(OpBE<P> backend, AllocPlace allocPlace);

}
