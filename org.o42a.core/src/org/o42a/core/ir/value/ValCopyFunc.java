/*
    Compiler Core
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
package org.o42a.core.ir.value;

import static org.o42a.core.ir.value.ValType.VAL_TYPE;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.value.ValType.Op;
import org.o42a.util.string.ID;


public final class ValCopyFunc extends Func<ValCopyFunc> {

	public static final Signature VAL_COPY = new Signature();

	private ValCopyFunc(FuncCaller<ValCopyFunc> caller) {
		super(caller);
	}

	public void copy(CodeDirs dirs, ValOp from, ValOp to) {

		final Block code = dirs.code();

		invoke(null, code, VAL_COPY.result(), from.ptr(), to.ptr());
		to.go(code, dirs);
		to.holder().hold(code);
	}

	public static final class Signature
			extends org.o42a.codegen.code.Signature<ValCopyFunc> {

		private Return<Void> result;
		private Arg<Op> from;
		private Arg<Op> to;

		private Signature() {
			super(ID.id("ValCopyF"));
		}

		public final Return<Void> result() {
			return this.result;
		}

		public final Arg<Op> from() {
			return this.from;
		}

		public final Arg<Op> to() {
			return this.to;
		}

		@Override
		public ValCopyFunc op(FuncCaller<ValCopyFunc> caller) {
			return new ValCopyFunc(caller);
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnVoid();
			this.from = builder.addPtr("from", VAL_TYPE);
			this.to = builder.addPtr("to", VAL_TYPE);
		}

	}

}
