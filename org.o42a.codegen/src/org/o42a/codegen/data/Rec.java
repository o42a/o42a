/*
    Compiler Code Generator
    Copyright (C) 2010-2014 Ruslan Lopatin

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

import java.util.function.Supplier;

import org.o42a.codegen.code.op.AllocPtrOp;
import org.o42a.util.string.ID;


public abstract class Rec<P extends AllocPtrOp<P>, T>
		extends Data<P>
		implements RecAttributes {

	private final SubData<?> enclosing;
	private Supplier<T> value;
	private int flags;

	Rec(SubData<?> enclosing, ID id) {
		super(enclosing.getGenerator(), id);
		this.enclosing = enclosing;
	}

	@Override
	public final Global<?, ?> getGlobal() {
		return this.enclosing.getGlobal();
	}

	public Rec<P, T> setConstant(boolean constant) {
		if (constant) {
			this.flags |= CONSTANT;
		} else {
			this.flags &= ~CONSTANT;
		}
		return this;
	}

	@Override
	public final boolean isLowLevel() {
		return (getDataFlags() & LOW_LEVEL) != 0;
	}

	public Rec<P, T> setLowLevel(boolean lowLevel) {
		if (lowLevel) {
			this.flags |= LOW_LEVEL;
		} else {
			this.flags &= ~LOW_LEVEL;
		}
		return this;
	}

	public Rec<P, T> setAttributes(RecAttributes attributes) {
		this.flags = attributes.getDataFlags();
		return this;
	}

	@Override
	public final int getDataFlags() {
		return this.flags | (this.enclosing.getDataFlags() & NESTED_FLAGS);
	}

	@Override
	public final Type<?> getEnclosing() {
		return this.enclosing.getInstance();
	}

	@Override
	public final Type<?> getInstance() {
		return null;
	}

	public final Supplier<T> getValue() {
		return this.value;
	}

	public void setValue(Supplier<T> value) {
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

}
