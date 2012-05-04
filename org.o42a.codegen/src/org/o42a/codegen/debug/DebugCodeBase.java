/*
    Compiler Code Generator
    Copyright (C) 2010-2012 Ruslan Lopatin

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

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.data.CodeBase;
import org.o42a.codegen.data.Ptr;


public abstract class DebugCodeBase extends CodeBase {

	private final Generator generator;
	private final Function<?> function;

	public DebugCodeBase(Code enclosing) {
		this.generator = enclosing.getGenerator();
		this.function = enclosing.getFunction();
	}

	public DebugCodeBase(Generator generator) {
		this.generator = generator;
		this.function = (Function<?>) this;
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
		assert assertIncomplete();
		if (!getGenerator().isDebug()) {
			return;
		}

		printFunc(getGenerator()).op(null, code()).call(
				code(),
				binaryMessage(getGenerator(), message).op(null, code()));
		if (nl) {
			printWoPrefixFunc(getGenerator()).op(null, code()).call(
					code(),
					binaryMessage(getGenerator(), "\n").op(null, code()));
		}
	}

	public final void dumpName(String prefix, Dumpable data) {
		assert assertIncomplete();
		if (!getGenerator().isDebug()) {
			return;
		}

		final FuncPtr<DebugNameFunc> func =
				getGenerator()
				.externalFunction()
				.link("o42a_dbg_mem_name", DEBUG_NAME);

		func.op(null, code()).call(
				code(),
				binaryMessage(getGenerator(), prefix).op(null, code()),
				data.toAny(null, code()));
	}

	public void dumpName(String prefix, Func<?> func) {
		assert assertIncomplete();
		assert func.getSignature().isDebuggable() :
			"Can not dump " + func + " name: it is not debuggable";
		if (!getGenerator().isDebug()) {
			return;
		}

		final FuncPtr<DebugNameFunc> debugFunc =
				getGenerator()
				.externalFunction()
				.link("o42a_dbg_func_name", DEBUG_NAME);

		debugFunc.op(null, code()).call(
				code(),
				binaryMessage(getGenerator(), prefix).op(null, code()),
				func.toAny(null, code()));
	}

	public final void dump(String message, Dumpable data) {
		dump(message, data, 5);
	}

	public final void dump(String message, Dumpable data, int depth) {
		assert assertIncomplete();
		if (!getGenerator().isDebug()) {
			return;
		}

		final FuncPtr<DebugDumpFunc> func =
				getGenerator()
				.externalFunction()
				.link("o42a_dbg_dump_mem", DEBUG_DUMP);
		final Code code = code();

		func.op(null, code).call(
				code,
				binaryMessage(getGenerator(), message).op(null, code),
				data.toData(code.id("dump"), code),
				code.int32(depth));
	}

	protected static final FuncPtr<DebugPrintFunc> printFunc(
			Generator generator) {
		return generator.externalFunction().link("o42a_dbg_print", DEBUG_PRINT);
	}

	protected static final FuncPtr<DebugPrintFunc> printWoPrefixFunc(
			Generator generator) {
		return generator.externalFunction()
				.link("o42a_dbg_print_wo_prefix", DEBUG_PRINT);
	}

	protected static final Ptr<AnyOp> binaryMessage(
			Generator generator,
			String message) {

		final Debug debug = generator.getDebug();

		return debug.allocateMessage(debug.nextDebugId(), message);
	}

	private final Code code() {
		return (Code) this;
	}

}
