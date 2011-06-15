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
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.*;


public final class DebugStackFrameOp extends StructOp<DebugStackFrameOp> {

	public static final DebugStackFrameType DEBUG_STACK_FRAME_TYPE =
		new DebugStackFrameType();

	private DebugStackFrameOp(StructWriter<DebugStackFrameOp> writer) {
		super(writer);
	}

	@Override
	public final DebugStackFrameType getType() {
		return (DebugStackFrameType) super.getType();
	}

	public final AnyRecOp name(Code code) {
		return ptr(null, code, getType().name());
	}

	public final StructRecOp<DebugStackFrameOp> prev(Code code) {
		return ptr(null, code, getType().prev());
	}

	public final AnyRecOp comment(Code code) {
		return ptr(null, code, getType().comment());
	}

	public final AnyRecOp file(Code code) {
		return ptr(null, code, getType().file());
	}

	public final Int32recOp line(Code code) {
		return int32(null, code, getType().line());
	}

	public static final class DebugStackFrameType
			extends Type<DebugStackFrameOp> {

		private AnyPtrRec name;
		private StructRec<DebugStackFrameOp> prev;
		private AnyPtrRec comment;
		private AnyPtrRec file;
		private Int32rec line;

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

		public final AnyPtrRec comment() {
			return this.comment;
		}

		public final AnyPtrRec file() {
			return this.file;
		}

		public final Int32rec line() {
			return this.line;
		}

		@Override
		public DebugStackFrameOp op(StructWriter<DebugStackFrameOp> writer) {
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
			this.comment = data.addPtr("comment");
			this.file = data.addPtr("file");
			this.line = data.addInt32("line");
		}

	}

}