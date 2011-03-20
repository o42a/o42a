/*
    Compiler Code Generator
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
package org.o42a.codegen.debug;

import static org.o42a.codegen.debug.DebugStackFrameOp.DEBUG_STACK_FRAME_TYPE;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.Int32op;
import org.o42a.codegen.code.op.RecOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Int32rec;
import org.o42a.codegen.data.StructRec;
import org.o42a.codegen.data.SubData;


public class DebugEnvOp extends StructOp {

	public static final Type DEBUG_ENV_TYPE = new Type();

	public static int DEBUG_CMD_EXEC = 0;
	public static int DEBUG_CMD_REPORT = 1;

	private DebugEnvOp(StructWriter writer) {
		super(writer);
	}

	@Override
	public final Type getType() {
		return (Type) super.getType();
	}

	public final RecOp<DebugStackFrameOp> stackFrame(Code code) {
		return ptr(code, getType().stackFrame());
	}

	public final RecOp<Int32op> command(Code code) {
		return int32(code, getType().command());
	}

	public static final class Type
			extends org.o42a.codegen.data.Type<DebugEnvOp> {

		private StructRec<DebugStackFrameOp> stackFrame;
		private Int32rec command;

		private Type() {
		}

		@Override
		public boolean isDebuggable() {
			return false;
		}

		public final StructRec<DebugStackFrameOp> stackFrame() {
			return this.stackFrame;
		}

		public final Int32rec command() {
			return this.command;
		}

		@Override
		public DebugEnvOp op(StructWriter writer) {
			return new DebugEnvOp(writer);
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.id("DEBUG").sub("Env");
		}

		@Override
		protected void allocate(SubData<DebugEnvOp> data) {
			this.stackFrame =
				data.addPtr("stack_frame", DEBUG_STACK_FRAME_TYPE);
			this.command = data.addInt32("command");
		}

	}

}
