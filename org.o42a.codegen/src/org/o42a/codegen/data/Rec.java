/*
    Compiler Code Generator
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.codegen.data;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.op.PtrOp;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.DataWriter;


public abstract class Rec<O extends PtrOp, T> extends Data<O> {

	private final SubData<?> enclosing;
	@SuppressWarnings("rawtypes")
	private final Content content;
	private T value;

	Rec(SubData<?> enclosing, CodeId id, Content<?> content) {
		super(enclosing, id);
		this.enclosing = enclosing;
		this.content = content != null ? content : Type.EMPTY_CONTENT;
	}

	@Override
	public final Global<?, ?> getGlobal() {
		return getEnclosing().getGlobal();
	}

	@Override
	public final SubData<?> getEnclosing() {
		return this.enclosing;
	}

	public final T getValue() {
		return this.value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append(getId());
		if (this.value != null) {
			out.append('[').append(this.value).append(']');
		} else {
			out.append("[?]");
		}

		return out.toString();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void setAllocation(DataAllocation<O> allocation) {
		super.setAllocation(allocation);
		this.content.allocated(this);
	}

	@SuppressWarnings("unchecked")
	protected final void fill(DataWriter writer) {
		this.content.fill(this);
	}

}
