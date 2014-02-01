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
package org.o42a.codegen.debug;

import static org.o42a.codegen.debug.Debug.DEBUG_ID;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.Int32op;


final class DebugDumpFunc extends Func<DebugDumpFunc> {

	public static final Signature DEBUG_DUMP = new Signature();

	private DebugDumpFunc(FuncCaller<DebugDumpFunc> caller) {
		super(caller);
	}

	public void call(Code code, AnyOp prefix, DataOp data, Int32op depth) {
		invoke(null, code, DEBUG_DUMP.result(), prefix, data, depth);
	}

	public static final class Signature
			extends org.o42a.codegen.code.Signature<DebugDumpFunc> {

		private Return<Void> result;
		private Arg<AnyOp> prefix;
		private Arg<DataOp> data;
		private Arg<Int32op> depth;

		private Signature() {
			super(DEBUG_ID.sub("DumpF"));
		}

		public final Return<Void> result() {
			return this.result;
		}

		public final Arg<AnyOp> prefix() {
			return this.prefix;
		}

		public final Arg<DataOp> data() {
			return this.data;
		}

		public final Arg<Int32op> depth() {
			return this.depth;
		}

		@Override
		public DebugDumpFunc op(FuncCaller<DebugDumpFunc> caller) {
			return new DebugDumpFunc(caller);
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnVoid();
			this.prefix = builder.addPtr("prefix");
			this.data = builder.addData("data");
			this.depth = builder.addInt32("depth");
		}

	}

}
