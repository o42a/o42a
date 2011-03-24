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

import static org.o42a.codegen.debug.DebugDumpFunc.DEBUG_DUMP;
import static org.o42a.codegen.debug.DebugNameFunc.DEBUG_NAME;
import static org.o42a.codegen.debug.DebugPrintFunc.DEBUG_PRINT;
import static org.o42a.codegen.debug.DebugStackFrameOp.DEBUG_STACK_FRAME_TYPE;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.op.*;
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

	public final boolean isDebug() {
		return getGenerator().isDebug();
	}

	public final void debug(String message) {
		debug(message, true);
	}

	public void debug(String message, boolean nl) {
		assertIncomplete();
		if (!getGenerator().isDebug()) {
			return;
		}

		printFunc().op(code()).call(
				code(),
				binaryMessage(nl ? message + '\n' : message).op(code()));
	}

	public final void begin(String comment) {
		if (!isDebug()) {
			return;
		}

		final Code code = code();
		final Function<?> function = code.getFunction();

		final DebugEnvOp debugEnv = function.debugEnv(code);
		final RecOp<DebugStackFrameOp> envStackFrame =
			debugEnv.stackFrame(code);
		final DebugStackFrameOp prevStackFrame = envStackFrame.load(code);
		final DebugStackFrameOp stackFrame =
			code.allocate(DEBUG_STACK_FRAME_TYPE);

		stackFrame.name(code).store(
				code,
				prevStackFrame.name(code).load(code));
		stackFrame.prev(code).store(code, prevStackFrame);
		stackFrame.comment(code).store(
				code,
				binaryMessage(comment).op(code));
		stackFrame.file(code).store(code, code.nullPtr());
		stackFrame.line(code).store(code, code.int32(0));

		final DebugPrintFunc printFunc = printWoPrefixFunc().op(code);

		debug("((( /* ", false);
		printFunc.call(code, binaryMessage(comment).op(code));
		printFunc.call(code, binaryMessage(" */\n").op(code));
		envStackFrame.store(code, stackFrame);

		final RecOp<Int8op> indent = debugEnv.indent(code);

		indent.store(code, indent.load(code).add(code, code.int8((byte) 1)));
	}

	public CodePos end(String id, CodePos codePos) {
		if (codePos == null) {
			return null;
		}
		if (!isDebug()) {
			return codePos;
		}

		final CodeBlk block = code().addBlock(id);

		block.end();
		block.go(codePos);

		return block.head();
	}

	public final void end() {
		if (!isDebug()) {
			return;
		}

		final Code code = code();
		final Function<?> function = code.getFunction();

		final DebugEnvOp debugEnv = function.debugEnv(code);

		final RecOp<DebugStackFrameOp> envStackFrame =
			debugEnv.stackFrame(code);
		final DebugStackFrameOp oldStackFrame = envStackFrame.load(code);

		envStackFrame.store(code, oldStackFrame.prev(code).load(code));

		final RecOp<Int8op> indent = debugEnv.indent(code);

		indent.store(code, indent.load(code).sub(code, code.int8((byte) 1)));

		final DebugPrintFunc printFunc = printWoPrefixFunc().op(code);

		debug("))) /* ", false);
		printFunc.call(code, oldStackFrame.comment(code).load(code));
		printFunc.call(code, binaryMessage(" */\n").op(code));
	}

	public final void dumpName(String prefix, StructOp data) {
		assertIncomplete();
		if (isDebug()) {
			dumpName(prefix, data.toData(code()));
		}
	}

	public final void dumpName(String prefix, DataOp data) {
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

	public void dumpName(String prefix, Func func) {
		assertIncomplete();
		assert func.getSignature().isDebuggable() :
			"Can not dump " + func + " name: it is not debuggable";
		if (!getGenerator().isDebug()) {
			return;
		}

		final FuncPtr<DebugNameFunc> debugFunc =
			this.generator.externalFunction(
					"o42a_dbg_func_name",
					DEBUG_NAME);

		debugFunc.op(code()).call(
				code(),
				binaryMessage(prefix).op(code()),
				func.toAny(code()));
	}

	public final void dump(String message, StructOp data) {
		dump(message, data, Integer.MAX_VALUE);
	}

	public final void dump(String message, StructOp data, int depth) {
		assertIncomplete();
		if (isDebug()) {
			dump(message, data.toData(code()), depth);
		}
	}

	public final void dump(String message, DataOp data) {
		dump(message, data, 3);
	}

	public final void dump(String message, DataOp data, int depth) {
		assertIncomplete();
		if (!getGenerator().isDebug()) {
			return;
		}

		final FuncPtr<DebugDumpFunc> func =
			this.generator.externalFunction(
					"o42a_dbg_dump_mem",
					DEBUG_DUMP);
		final Code code = code();

		func.op(code).call(
				code,
				binaryMessage(message).op(code),
				data,
				code.int32(depth));
	}

	private FuncPtr<DebugPrintFunc> printFunc() {
		return this.generator.externalFunction("o42a_dbg_print", DEBUG_PRINT);
	}

	private FuncPtr<DebugPrintFunc> printWoPrefixFunc() {
		return this.generator.externalFunction(
				"o42a_dbg_print_wo_prefix",
				DEBUG_PRINT);
	}

	private Ptr<AnyOp> binaryMessage(String message) {
		return getGenerator().getDebug().allocateMessage(
				getGenerator().id("DEBUG_" + (debugSeq++)),
				message);
	}

	private final Code code() {
		return (Code) this;
	}

}
