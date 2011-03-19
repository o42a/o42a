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
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.RecOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.*;


public final class DebugStackFrameOp extends StructOp {

	public static final DebugStackFrameType DEBUG_STACK_FRAME_TYPE =
		new DebugStackFrameType();

	private DebugStackFrameOp(StructWriter writer) {
		super(writer);
	}

	@Override
	public final DebugStackFrameType getType() {
		return (DebugStackFrameType) super.getType();
	}

	public final RecOp<AnyOp> name(Code code) {
		return writer().ptr(code, getType().name());
	}

	public final RecOp<DebugStackFrameOp> prev(Code code) {
		return writer().ptr(code, getType().prev());
	}

	public static final class DebugStackFrameType
			extends Type<DebugStackFrameOp> {

		private AnyPtrRec name;
		private StructRec<DebugStackFrameOp> prev;

		private DebugStackFrameType() {
		}

		@Override
		public boolean isDebuggable() {
			return false;
		}

		public final AnyPtrRec name() {
			return this.name;
		}

		public final StructRec<DebugStackFrameOp> prev() {
			return this.prev;
		}

		@Override
		public DebugStackFrameOp op(StructWriter writer) {
			return new DebugStackFrameOp(writer);
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.id("DEBUG").sub("StackFrame");
		}

		@Override
		protected void allocate(SubData<DebugStackFrameOp> data) {
			this.name = data.addPtr("name");
			this.prev = data.addPtr("prev", DEBUG_STACK_FRAME_TYPE);
		}

	}

}