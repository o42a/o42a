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
import org.o42a.codegen.code.op.StructOp;


public abstract class Struct<S extends StructOp<S>> extends Type<S> {

	@SuppressWarnings("rawtypes")
	private static final StructContent<?> STRUCT_CONTENT = new StructContent();

	@SuppressWarnings("unchecked")
	public static final <S extends Struct<?>> Content<S> structContent() {
		return (Content<S>) STRUCT_CONTENT;
	}

	protected abstract void fill();

	final void setStruct(SubData<?> enclosing, CodeId name) {
		this.data = new StructData<S>(enclosing, this, name);
	}

	final void setGlobal(Global<S, ?> global) {
		this.data = new GlobalStructData<S>(global, this);
	}

	private static final class StructContent<T extends Struct<?>>
			implements Content<T> {

		@Override
		public void allocated(T instance) {
		}

		@Override
		public void fill(T instance) {
			instance.fill();
		}

	}

}
