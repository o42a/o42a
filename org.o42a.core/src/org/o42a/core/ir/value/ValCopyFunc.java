/*
    Compiler Core
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
package org.o42a.core.ir.value;

import static org.o42a.core.ir.value.ValType.VAL_TYPE;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValType.Op;


public final class ValCopyFunc extends Func<ValCopyFunc> {

	public static final ValCopySignature VAL_COPY = new ValCopySignature();

	private ValCopyFunc(FuncCaller<ValCopyFunc> caller) {
		super(caller);
	}

	public void copy(ValDirs dirs, ValOp from) {

		final ValOp to = dirs.value();

		invoke(null, dirs.code(), VAL_COPY.result(), from.ptr(), to.ptr());
		to.go(dirs.code(), dirs);
	}

	public static final class ValCopySignature extends Signature<ValCopyFunc> {

		private Return<Void> result;
		private Arg<Op> from;
		private Arg<Op> to;

		private ValCopySignature() {
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
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.id("ValCopyF");
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnVoid();
			this.from = builder.addPtr("from", VAL_TYPE);
			this.to = builder.addPtr("to", VAL_TYPE);
		}

	}

}
