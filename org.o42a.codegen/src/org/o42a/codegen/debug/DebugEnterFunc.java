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

import static org.o42a.codegen.debug.DebugStackFrameType.DEBUG_STACK_FRAME_TYPE;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;


final class DebugEnterFunc extends Func {

	public static final DebugEnter DEBUG_ENTER = new DebugEnter();

	private DebugEnterFunc(FuncCaller<DebugEnterFunc> caller) {
		super(caller);
	}

	public void enter(Code code, DebugStackFrameType.Op stackFrame) {
		invoke(code, DEBUG_ENTER.result(), stackFrame);
	}

	public static final class DebugEnter extends Signature<DebugEnterFunc> {

		private Return<Void> result;
		private Arg<DebugStackFrameType.Op> stackFrame;

		private DebugEnter() {
		}

		public final Return<Void> result() {
			return this.result;
		}

		public final Arg<DebugStackFrameType.Op> stackFrame() {
			return this.stackFrame;
		}

		@Override
		public DebugEnterFunc op(FuncCaller<DebugEnterFunc> caller) {
			return new DebugEnterFunc(caller);
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.id("DEBUG").sub("EnterF");
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnVoid();
			this.stackFrame =
				builder.addPtr("stack_frame", DEBUG_STACK_FRAME_TYPE);
		}

	}

}
