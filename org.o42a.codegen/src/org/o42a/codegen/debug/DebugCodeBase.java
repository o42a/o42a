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

import static org.o42a.codegen.data.StringCodec.bytesPerChar;
import static org.o42a.codegen.data.StringCodec.stringToBinary;
import static org.o42a.codegen.debug.DebugNameFunc.DEBUG_NAME;
import static org.o42a.codegen.debug.DebugPrintFunc.DEBUG_PRINT;
import static org.o42a.codegen.debug.DumpFunc.DEBUG_DUMP;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.OpCodeBase;
import org.o42a.codegen.code.op.PtrOp;
import org.o42a.codegen.data.DataAlignment;
import org.o42a.codegen.data.Ptr;


public abstract class DebugCodeBase extends OpCodeBase {

	private static Generator cachedGenerator;
	private static int debugSeq;

	private final Generator generator;
	private final Function<?> function;

	public DebugCodeBase(Code enclosing) {
		this.generator = enclosing.getGenerator();
		this.function = enclosing.getFunction();
	}

	public DebugCodeBase(Generator generator) {
		this.generator = generator;
		this.function = (Function<?>) this;
		if (cachedGenerator != generator) {
			cachedGenerator = generator;
			debugSeq = 0;
		}
	}

	public final Generator getGenerator() {
		return this.generator;
	}

	public final Function<?> getFunction() {
		return this.function;
	}

	public void debug(String message) {
		debug(message, true);
	}

	public void debug(String message, boolean nl) {
		assertIncomplete();
		if (!getGenerator().isDebug()) {
			return;
		}

		final FuncPtr<DebugPrintFunc> func =
			this.generator.externalFunction("o42a_dbg_print", DEBUG_PRINT);

		func.op(code()).call(
				code(),
				binaryMessage(nl ? message + '\n' : message).op(code()));
	}

	public final void dumpName(String prefix, PtrOp data) {
		assertIncomplete();
		if (!getGenerator().isDebug()) {
			return;
		}

		final FuncPtr<DebugNameFunc> func =
			this.generator.externalFunction(
					"o42a_dbg_mem_name",
					DEBUG_NAME);

		func.op(code()).call(
				code(),
				binaryMessage(prefix).op(code()),
				data.toAny(code()));
	}

	public void dumpName(String prefix, Func code) {
		assertIncomplete();
		if (!getGenerator().isDebug()) {
			return;
		}

		final FuncPtr<DebugNameFunc> func =
			this.generator.externalFunction(
					"o42a_dbg_func_name",
					DEBUG_NAME);

		func.op(code()).call(
				code(),
				binaryMessage(prefix).op(code()),
				code.toAny(code()));
	}

	public final void dump(String message, PtrOp data) {
		dump(message, data, Integer.MAX_VALUE);
	}

	public final void dump(String message, PtrOp data, int depth) {
		assertIncomplete();
		if (!getGenerator().isDebug()) {
			return;
		}

		debug(message, false);

		final FuncPtr<DumpFunc> func =
			this.generator.externalFunction(
					"o42a_dbg_dump_mem",
					DEBUG_DUMP);

		func.op(code()).call(code(), data.toAny(code()), code().int32(depth));
	}

	private Ptr<AnyOp> binaryMessage(String message) {

		final DataAlignment bytesPerChar = bytesPerChar(message);
		final int size = (message.length() + 1) * bytesPerChar.getBytes();
		final byte[] bytes = new byte[size];

		stringToBinary(message, bytes, bytesPerChar);

		return getGenerator().addBinary(
				getGenerator().id("DEBUG_" + (debugSeq++)),
				bytes);
	}

	private final Code code() {
		return (Code) this;
	}

}
