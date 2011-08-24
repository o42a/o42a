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


public abstract class Rec<P extends PtrOp<P>, T> extends Data<P> {

	private final SubData<?> enclosing;
	@SuppressWarnings("rawtypes")
	private final Content content;
	private T value;
	private boolean constant;

	Rec(SubData<?> enclosing, CodeId id, Content<?> content) {
		super(enclosing.getGenerator(), id);
		this.enclosing = enclosing;
		this.content = content != null ? content : Type.emptyContent();
	}

	@Override
	public final Global<?, ?> getGlobal() {
		return this.enclosing.getGlobal();
	}

	@Override
	public boolean isConstant() {
		return this.constant || this.enclosing.isConstant();
	}

	public Rec<P, T> setConstant(boolean constant) {
		this.constant = constant;
		return this;
	}

	@Override
	public final Type<?> getEnclosing() {
		return this.enclosing.getInstance();
	}

	@Override
	public final Type<?> getInstance() {
		return null;
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
	protected void setAllocation(DataAllocation<P> allocation) {
		super.setAllocation(allocation);
		this.content.allocated(this);
	}

	@SuppressWarnings("unchecked")
	protected final void fill(DataWriter writer) {
		this.content.fill(this);
	}

}
