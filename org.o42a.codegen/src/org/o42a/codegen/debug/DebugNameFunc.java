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
package org.o42a.codegen.debug;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.AnyOp;


final class DebugNameFunc extends Func {

	public static final DebugName DEBUG_NAME = new DebugName();

	private DebugNameFunc(FuncCaller<DebugNameFunc> caller) {
		super(caller);
	}

	public void call(Code code, AnyOp message, AnyOp data) {
		invoke(code, DEBUG_NAME.result(), message, data);
	}

	public static final class DebugName extends Signature<DebugNameFunc> {

		private Return<Void> result;
		private Arg<AnyOp> message;
		private Arg<AnyOp> data;

		private DebugName() {
		}

		public final Return<Void> result() {
			return this.result;
		}

		public final Arg<AnyOp> message() {
			return this.message;
		}

		public final Arg<AnyOp> data() {
			return this.data;
		}

		@Override
		public DebugNameFunc op(FuncCaller<DebugNameFunc> caller) {
			return new DebugNameFunc(caller);
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.id("DEBUG").sub("NameF");
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnVoid();
			this.message = builder.addAny("message");
			this.data = builder.addAny("data");
		}

	}

}
