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

import static org.o42a.codegen.debug.DebugExecCommandFunc.DEBUG_EXEC_COMMAND;
import static org.o42a.codegen.debug.DebugStackFrameOp.DEBUG_STACK_FRAME_TYPE;
import static org.o42a.codegen.debug.DebugTraceFunc.DEBUG_TRACE;
import static org.o42a.util.StringCodec.nullTermASCIIString;
import static org.o42a.util.StringCodec.nullTermString;

import java.nio.charset.Charset;
import java.util.HashMap;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.CodeCallback;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.RecOp;
import org.o42a.codegen.data.*;


public class Debug {

	public static final CodeCallback DEBUG_CODE_CALLBACK =
		new DebugCodeCallback();

	private final Generator generator;

	private boolean debug;

	private Code dontExitFrom;
	private FuncPtr<DebugTraceFunc> enterFunc;
	private FuncPtr<DebugTraceFunc> exitFunc;
	private FuncPtr<DebugExecCommandFunc> execCommandFunc;

	private final HashMap<String, Ptr<AnyOp>> names =
		new HashMap<String, Ptr<AnyOp>>();
	private final HashMap<String, Ptr<AnyOp>> messages =
		new HashMap<String, Ptr<AnyOp>>();
	private final HashMap<Ptr<?>, DebugTypeInfo> typeInfo =
		new HashMap<Ptr<?>, DebugTypeInfo>();

	public Debug(Generator generator) {
		this.generator = generator;
	}

	public final Generator getGenerator() {
		return this.generator;
	}

	public final boolean isDebug() {
		return this.debug;
	}

	public final void setDebug(boolean debug) {
		this.debug = debug;
	}

	public <F extends Func> void addFunction(
			CodeId id,
			FuncPtr<F> functionPtr) {
		if (!isDebug()) {
			return;
		}

		final Function<F> function = functionPtr.getFunction();

		if (function == null) {
			return;// External function.
		}

		final Signature<F> signature = functionPtr.getSignature();

		if (!signature.isDebuggable()) {
			return;
		}

		final Ptr<AnyOp> namePtr =
			allocateName(
					getGenerator()
					.id("DEBUG")
					.sub("func_name")
					.sub(id),
					id.getId());

		final DebugEnvOp debugEnv = function.debugEnv(function);
		final RecOp<DebugStackFrameOp> envStackFrame =
			debugEnv.stackFrame(function);
		final DebugStackFrameOp stackFrame =
			function.allocate(DEBUG_STACK_FRAME_TYPE);

		stackFrame.name(function).store(function, namePtr.op(function));
		stackFrame.prev(function).store(function, envStackFrame.load(function));
		stackFrame.comment(function).store(function, function.nullPtr());
		stackFrame.file(function).store(function, function.nullPtr());
		stackFrame.line(function).store(function, function.int32(0));

		envStackFrame.store(function, stackFrame);

		final BoolOp execResult =
			execCommandFunc().op(function).exec(function, debugEnv);
		final CodeBlk commandExecuted = function.addBlock("command_executed");

		execResult.go(function, commandExecuted.head());

		final Code oldDontExitFrom = this.dontExitFrom;

		try {
			this.dontExitFrom = commandExecuted;
			signature.returns(getGenerator()).returnNull(commandExecuted);
		} finally {
			this.dontExitFrom = oldDontExitFrom;
		}

		enterFunc().op(function).trace(function, debugEnv);
	}

	public void registerType(SubData<?> typeData) {
		if (!isDebug()) {
			return;
		}
		if (typeData.getInstance().isDebugInfo()) {
			return;
		}

		final Type<?> type = typeData.getInstance();
		final DebugTypeInfo typeInfo = new DebugTypeInfo(type);

		getGenerator().newGlobal().struct(typeInfo);

		final DebugTypeInfo old =
			this.typeInfo.put(typeData.getPointer(), typeInfo);

		assert old == null :
			"Type info already exists: " + old;
	}


	final Ptr<AnyOp> allocateName(CodeId id, String value) {

		final Ptr<AnyOp> found = this.names.get(value);

		if (found != null) {
			return found;
		}

		final Ptr<AnyOp> binary =
			getGenerator().addBinary(id, nullTermASCIIString(value));

		this.names.put(value, binary);

		return binary;
	}

	final Ptr<AnyOp> allocateMessage(CodeId id, String value) {

		final Ptr<AnyOp> found = this.messages.get(value);

		if (found != null) {
			return found;
		}

		final Ptr<AnyOp> binary =  getGenerator().addBinary(
				id,
				nullTermString(Charset.defaultCharset(), value));

		this.messages.put(value, binary);

		return binary;
	}

	final void setName(AnyPtrRec field, CodeId id, String value) {
		field.setValue(allocateName(id, value));
	}

	DebugTypeInfo typeInfo(Type<?> instance) {

		final DebugTypeInfo typeInfo =
			this.typeInfo.get(instance.getType().pointer(getGenerator()));

		assert typeInfo != null :
			"Unknown debug type info of " + instance.getType();

		return typeInfo;
	}

	private FuncPtr<DebugTraceFunc> enterFunc() {
		if (this.enterFunc != null) {
			return this.enterFunc;
		}
		return this.enterFunc =
			getGenerator().externalFunction("o42a_dbg_enter", DEBUG_TRACE);
	}

	private FuncPtr<DebugTraceFunc> exitFunc() {
		if (this.exitFunc != null) {
			return this.exitFunc;
		}
		return this.exitFunc =
			getGenerator().externalFunction("o42a_dbg_exit", DEBUG_TRACE);
	}

	private FuncPtr<DebugExecCommandFunc> execCommandFunc() {
		if (this.execCommandFunc != null) {
			return this.execCommandFunc;
		}
		return this.execCommandFunc = getGenerator().externalFunction(
				"o42a_dbg_exec_command",
				DEBUG_EXEC_COMMAND);
	}

	private static final class DebugCodeCallback implements CodeCallback {

		@Override
		public void beforeReturn(Code code) {

			final Debug debug = code.getGenerator().getDebug();

			if (debug.dontExitFrom != code) {
				debug.exitFunc().op(code).trace(
						code,
						code.getFunction().debugEnv(code));
			}
		}

	}

}
