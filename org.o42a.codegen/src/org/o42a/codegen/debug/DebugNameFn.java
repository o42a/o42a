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

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.ExtSignature;
import org.o42a.codegen.code.Fn;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.AnyOp;


final class DebugNameFn extends Fn<DebugNameFn> {

	public static final ExtSignature<Void, DebugNameFn> DEBUG_NAME =
			customSignature(DEBUG_ID.sub("NameF"), 2)
			.addPtr("message")
			.addPtr("data")
			.returnVoid(c -> new DebugNameFn(c));

	private DebugNameFn(FuncCaller<DebugNameFn> caller) {
		super(caller);
	}

	public void call(Code code, AnyOp message, AnyOp data) {
		invoke(null, code, DEBUG_NAME.result(), message, data);
	}

}
