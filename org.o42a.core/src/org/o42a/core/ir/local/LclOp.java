/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.core.ir.local;

import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Content;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.field.FieldIR;
import org.o42a.core.ir.op.IROp;


public abstract class LclOp extends IROp implements HostOp {

	private final FieldIR<?> fieldIR;

	LclOp(CodeBuilder builder, FieldIR<?> fieldIR) {
		super(builder);
		this.fieldIR = fieldIR;
	}

	public FieldIR<?> getFieldIR() {
		return this.fieldIR;
	}

	@Override
	public abstract Op<?> ptr();

	@Override
	public final LocalOp toLocal() {
		return null;
	}

	public abstract void write(Control control);

	@Override
	public String toString() {
		return getFieldIR().getField().toString();
	}

	protected Content<?> content() {
		return null;
	}

	public static abstract class Op<S extends Op<S>> extends StructOp<S> {

		Op(StructWriter<S> writer) {
			super(writer);
		}

		@Override
		public Type<S> getType() {
			return (Type<S>) super.getType();
		}

		public abstract LclOp op(CodeBuilder builder, FieldIR<?> fieldIR);

	}

	public static abstract class Type<S extends Op<S>>
			extends org.o42a.codegen.data.Type<S> {

	}

}
