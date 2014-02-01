/*
    Root Object Definition
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
package org.o42a.root.string;

import static org.o42a.core.ir.value.ValType.VAL_TYPE;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.Int64op;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType.Op;
import org.o42a.util.string.ID;


final class SubStringFunc extends Func<SubStringFunc> {

	public static final Signature SUB_STRING = new Signature();

	private SubStringFunc(FuncCaller<SubStringFunc> caller) {
		super(caller);
	}

	public void substring(
			DefDirs dirs,
			ValOp string,
			Int64op from,
			Int64op to) {

		final ValOp sub = dirs.value();
		final Block code = dirs.code();

		substring(code, sub, string, from, to);

		sub.go(code, dirs);
		dirs.returnValue(sub);
	}

	public void substring(
			Code code,
			ValOp sub,
			ValOp string,
			Int64op from,
			Int64op to) {
		invoke(
				null,
				code,
				SUB_STRING.result(),
				sub.ptr(),
				string.ptr(),
				from,
				to);
	}

	static final class Signature
			extends org.o42a.codegen.code.Signature<SubStringFunc> {

		private Return<Void> result;
		private Arg<Op> sub;
		private Arg<Op> string;
		private Arg<Int64op> from;
		private Arg<Int64op> to;

		private Signature() {
			super(ID.id("SubStringF"));
		}

		public final Return<Void> result() {
			return this.result;
		}

		public final Arg<Op> sub() {
			return this.sub;
		}

		public final Arg<Op> string() {
			return this.string;
		}

		public final Arg<Int64op> from() {
			return this.from;
		}

		public final Arg<Int64op> to() {
			return this.to;
		}

		@Override
		public SubStringFunc op(FuncCaller<SubStringFunc> caller) {
			return new SubStringFunc(caller);
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnVoid();
			this.sub = builder.addPtr("sub", VAL_TYPE);
			this.string = builder.addPtr("string", VAL_TYPE);
			this.from = builder.addInt64("from");
			this.to = builder.addInt64("to");
		}

	}

}
