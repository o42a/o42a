/*
    Compiler Code Generator
    Copyright (C) 2012-2014 Ruslan Lopatin

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
import static org.o42a.codegen.debug.DebugCodeBase.binaryMessage;
import static org.o42a.codegen.debug.DebugStackFrameOp.DEBUG_STACK_FRAME_TYPE;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.ExtSignature;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.backend.FuncCaller;


final class DebugDoFunc extends Func<DebugDoFunc> {

	static final ExtSignature<Void, DebugDoFunc> DEBUG_DO =
			customSignature(DEBUG_ID.sub("DoF"), 2)
			.addPtr("stack_frame", DEBUG_STACK_FRAME_TYPE)
			.addPtr("comment")
			.returnVoid(c -> new DebugDoFunc(c));

	private DebugDoFunc(FuncCaller<DebugDoFunc> caller) {
		super(caller);
	}

	public final void call(
			Code code,
			DebugStackFrameOp stackFrame,
			String comment) {
		invoke(
				null,
				code,
				DEBUG_DO.result(),
				stackFrame,
				binaryMessage(code.getGenerator(), comment).op(null, code));
	}

}
