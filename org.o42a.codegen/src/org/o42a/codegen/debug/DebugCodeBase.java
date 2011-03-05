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
import static org.o42a.codegen.debug.DebugFunc.DEBUG_SIGNATURE;
import static org.o42a.codegen.debug.DumpFunc.DUMP_SIGNATURE;
import static org.o42a.codegen.debug.DumpNameFunc.DUMP_NAME_SIGNATURE;
import static org.o42a.codegen.debug.DumpStructFunc.DUMP_STRUCT_SIGNATURE;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.CodeBackend;
import org.o42a.codegen.code.op.*;
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
		assertIncomplete();
		if (!getGenerator().isDebug()) {
			return;
		}

		final CodePtr<DebugFunc> func =
			this.generator.externalFunction("o42a_debug", DEBUG_SIGNATURE);

		func.op(code()).call(code(), binaryMessage(message + '\n').op(code()));
	}

	public final void dumpName(String prefix, PtrOp data) {
		assertIncomplete();
		if (!getGenerator().isDebug()) {
			return;
		}

		final CodePtr<DumpNameFunc> func =
			this.generator.externalFunction(
					"o42a_debug_mem_name",
					DUMP_NAME_SIGNATURE);

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

		final CodePtr<DumpNameFunc> func =
			this.generator.externalFunction(
					"o42a_debug_func_name",
					DUMP_NAME_SIGNATURE);

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

		debug(message);

		final CodePtr<DumpFunc> func =
			this.generator.externalFunction(
					"o42a_dbg_dump_mem",
					DUMP_SIGNATURE);

		func.op(code()).call(code(), data.toAny(code()), code().int32(depth));
	}

	public final void dumpValue(String message, StructOp data) {
		dumpValue(message, data, Integer.MAX_VALUE);
	}

	public final void dumpValue(String message, StructOp data, int depth) {
		assertIncomplete();

		final Generator generator = getGenerator();

		if (!generator.isDebug()) {
			return;
		}

		final Debug debug = generator;
		final DbgStruct typeStruct =
			debug.typeStruct(generator, data.getType());

		if (typeStruct == null) {
			assert typeStruct != null :
				"Unknown type structure: " + data.getType();
			dump(message, data, depth);
			return;
		}

		final CodePtr<DebugFunc> debugFunc =
			this.generator.externalFunction("o42a_debug", DEBUG_SIGNATURE);

		debugFunc.op(code()).call(
				code(),
				binaryMessage(message).op(code()));

		final CodePtr<DumpStructFunc> dumpFunc =
			this.generator.externalFunction(
					"o42a_dbg_dump_struct",
					DUMP_STRUCT_SIGNATURE);

		dumpFunc.op(code()).call(
				code(),
				data.toAny(code()),
				typeStruct,
				code().int32(depth));
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

	protected final CodeBackend backend() {
		return this.generator.codeBackend();
	}

	private final Code code() {
		return (Code) this;
	}

}
