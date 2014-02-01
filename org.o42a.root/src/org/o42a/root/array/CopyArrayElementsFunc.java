/*
    Root Object Definition
    Copyright (C) 2013,2014 Ruslan Lopatin

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
package org.o42a.root.array;

import static org.o42a.core.ir.value.ValType.VAL_TYPE;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.Int64op;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType.Op;
import org.o42a.util.string.ID;


public final class CopyArrayElementsFunc extends Func<CopyArrayElementsFunc> {

	public static final Signature COPY_ARRAY_ELEMENTS = new Signature();

	private static final ID COPIED = ID.rawId("copied");

	private CopyArrayElementsFunc(FuncCaller<CopyArrayElementsFunc> caller) {
		super(caller);
	}

	public void copyElements(
			CodeDirs dirs,
			ValOp source,
			Int64op sourceFrom,
			Int64op sourceTo,
			ValOp target,
			Int64op targetStart) {

		final Block code = dirs.code();
		final BoolOp copied = invoke(
				COPIED,
				code,
				COPY_ARRAY_ELEMENTS.result(),
				source.ptr(),
				sourceFrom,
				sourceTo,
				target.ptr(),
				targetStart);

		copied.goUnless(code, dirs.falseDir());
	}

	public static final class Signature
			extends org.o42a.codegen.code.Signature<CopyArrayElementsFunc> {

		private Return<BoolOp> result;
		private Arg<Op> source;
		private Arg<Int64op> sourceFrom;
		private Arg<Int64op> sourceTo;
		private Arg<Op> target;
		private Arg<Int64op> targetStart;

		private Signature() {
			super(ID.rawId("CopyArrayElementsF"));
		}

		public final Return<BoolOp> result() {
			return this.result;
		}

		public final Arg<Op> source() {
			return this.source;
		}

		public final Arg<Int64op> sourceFrom() {
			return this.sourceFrom;
		}

		public final Arg<Int64op> sourceTo() {
			return this.sourceTo;
		}

		public final Arg<Op> target() {
			return this.target;
		}

		public final Arg<Int64op> targetStart() {
			return this.targetStart;
		}

		@Override
		public CopyArrayElementsFunc op(
				FuncCaller<CopyArrayElementsFunc> caller) {
			return new CopyArrayElementsFunc(caller);
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnBool();
			this.source = builder.addPtr("source", VAL_TYPE);
			this.sourceFrom = builder.addInt64("source_from");
			this.sourceTo = builder.addInt64("source_to");
			this.target = builder.addPtr("target", VAL_TYPE);
			this.targetStart = builder.addInt64("target_start");
		}

	}

}
