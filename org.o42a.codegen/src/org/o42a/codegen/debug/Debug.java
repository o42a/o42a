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

import static org.o42a.codegen.code.Disposal.DISPOSE_NOTHING;
import static org.o42a.codegen.debug.DbgOptionsType.DBG_OPTIONS_TYPE;
import static org.o42a.codegen.debug.DebugEnterFunc.DEBUG_ENTER;
import static org.o42a.codegen.debug.DebugExitFunc.DEBUG_EXIT;
import static org.o42a.codegen.debug.DebugStackFrameOp.DEBUG_STACK_FRAME_TYPE;
import static org.o42a.util.string.StringCodec.nullTermASCIIString;
import static org.o42a.util.string.StringCodec.nullTermString;

import java.nio.charset.Charset;
import java.util.HashMap;

import org.o42a.codegen.AbstractGenerator;
import org.o42a.codegen.Generator;
import org.o42a.codegen.ProxyGenerator;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.Allocated;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.data.*;
import org.o42a.util.string.ID;


public final class Debug {


	public static final ID DEBUG_ID = ID.id("DEBUG");

	private static final ID FN_STACK_FRAME_ID = ID.id("__fn_stack_frame__");
	private static final ID FN_NAME_ID = ID.id("__fn_name__");
	private static final ID CANT_ENTER_ID = ID.id("__cant_enter__");

	private static final AllocatableFnStackFrame ALLOCATABLE_FN_STACK_FRAME =
			new AllocatableFnStackFrame();
	private static final TraceBeforReturn TRACE_BEFORE_RETURN =
			new TraceBeforReturn();

	private final Generator generator;
	private final DebugSettings settings;

	private int debugSeq;

	private Code dontExitFrom;
	private FuncPtr<DebugEnterFunc> enterFunc;
	private FuncPtr<DebugExitFunc> exitFunc;

	private final HashMap<String, Ptr<AnyOp>> names = new HashMap<>();
	private final HashMap<String, Ptr<AnyOp>> messages = new HashMap<>();
	private final HashMap<Ptr<?>, DebugTypeInfo> typeInfo = new HashMap<>();

	public Debug(AbstractGenerator generator) {
		this.generator = generator;
		this.settings = new DebugSettings();
	}

	public Debug(ProxyGenerator generator) {
		this.generator = generator;
		this.settings = generator.getProxiedGenerator().getDebug().settings;
	}

	public final Generator getGenerator() {
		return this.generator;
	}

	public final boolean isProxied() {
		return getGenerator().isProxied();
	}

	public final boolean isDebug() {
		return this.settings.isDebug();
	}

	public final void setDebug(boolean debug) {
		this.settings.setDebug(debug);
	}

	public final boolean isQuiet() {
		return this.settings.isQuiet();
	}

	public final void setQuiet(boolean quiet) {
		this.settings.setQuiet(quiet);
	}

	public final boolean isNoDebugMessages() {
		return this.settings.isNoDebugMessages();
	}

	public final void setNoDebugMessages(boolean quiet) {
		this.settings.setNoDebugMessages(quiet);
	}

	public final boolean isDebugBlocksOmitted() {
		return this.settings.isDebugBlocksOmitted();
	}

	public final void setDebugBlocksOmitted(boolean debugBlocksOmitted) {
		this.settings.setDebugBlocksOmitted(debugBlocksOmitted);
	}

	public final boolean isSilentCalls() {
		return this.settings.isSilentCalls();
	}

	public final void setSilentCalls(boolean silent) {
		this.settings.setSilentCalls(silent);
	}

	public void write() {
		if (!isDebug()) {
			return;
		}

		final Generator generator = getGenerator();

		if (generator.isProxied()) {
			return;
		}

		generator.newGlobal().export().setConstant().newInstance(
				ID.rawId("o42a_dbg_default_options"),
				DBG_OPTIONS_TYPE,
				this.settings).getPointer();
	}

	public <F extends Func<F>> void addFunction(ID id, FuncPtr<F> functionPtr) {
		if (isProxied()) {
			return;
		}
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

		final Ptr<AnyOp> namePtr = allocateName(FN_NAME_ID.sub(id), id);
		final DebugStackFrameOp stackFrame =
				function.allocation().allocate(
						FN_STACK_FRAME_ID,
						ALLOCATABLE_FN_STACK_FRAME)
				.get();

		stackFrame.name(function).store(function, namePtr.op(null, function));
		stackFrame.comment(function).store(function, function.nullPtr());
		stackFrame.file(function).store(function, function.nullPtr());
		stackFrame.line(function).store(function, function.int32(0));

		final BoolOp canEnter =
				enterFunc().op(null, function).enter(function, stackFrame);
		final Block cantEnter = function.addBlock(CANT_ENTER_ID);

		canEnter.goUnless(function, cantEnter.head());

		final Code oldDontExitFrom = this.dontExitFrom;

		try {
			this.dontExitFrom = cantEnter;
			signature.returns(getGenerator()).returnNull(cantEnter);
		} finally {
			this.dontExitFrom = oldDontExitFrom;
		}
	}

	public Disposal createBeforeReturn(Function<?> function) {
		if (isProxied()) {
			return DISPOSE_NOTHING;
		}
		if (isDebug() && function.getSignature().isDebuggable()) {
			return TRACE_BEFORE_RETURN;
		}
		return DISPOSE_NOTHING;
	}

	public void registerType(SubData<?> typeData) {
		if (isProxied()) {
			return;
		}
		if (!isDebug()) {
			return;
		}
		if (typeData.getInstance().isDebugInfo()) {
			return;
		}

		final Type<?> type = typeData.getInstance();

		if (!type.isDebuggable()) {
			return;
		}

		final DebugTypeInfo typeInfo = type.createTypeInfo();

		final DebugTypeInfo old =
				this.typeInfo.put(typeData.getPointer(), typeInfo);

		assert old == null :
			"Type info already exists: " + old;
	}

	final Ptr<AnyOp> allocateName(ID id, ID name) {

		final String value = getGenerator().nameEncoder().print(name);
		final Ptr<AnyOp> found = this.names.get(value);

		if (found != null) {
			return found;
		}

		final Ptr<AnyOp> binary =
				getGenerator().addBinary(id, true, nullTermASCIIString(value));

		this.names.put(value, binary);

		return binary;
	}

	final Ptr<AnyOp> allocateMessage(ID id, String value) {

		final Ptr<AnyOp> found = this.messages.get(value);

		if (found != null) {
			return found;
		}

		final Ptr<AnyOp> binary =  getGenerator().addBinary(
				id,
				true,
				nullTermString(Charset.defaultCharset(), value));

		this.messages.put(value, binary);

		return binary;
	}

	final void setName(AnyRec field, ID id, ID name) {
		field.setValue(allocateName(id, name));
	}

	DebugTypeInfo typeInfo(Type<?> instance) {

		final DebugTypeInfo typeInfo =
				this.typeInfo.get(instance.getType().pointer(getGenerator()));

		assert typeInfo != null :
			"Unknown debug type info of " + instance.getType();

		return typeInfo;
	}

	final ID nextDebugId() {
		return ID.id("DEBUG_" + (this.debugSeq++));
	}

	private FuncPtr<DebugEnterFunc> enterFunc() {
		if (this.enterFunc != null) {
			return this.enterFunc;
		}
		return this.enterFunc =
				getGenerator()
				.externalFunction()
				.link("o42a_dbg_enter", DEBUG_ENTER);
	}

	private FuncPtr<DebugExitFunc> exitFunc() {
		if (this.exitFunc != null) {
			return this.exitFunc;
		}
		return this.exitFunc =
				getGenerator()
				.externalFunction()
				.link("o42a_dbg_exit", DEBUG_EXIT);
	}

	private static final class AllocatableFnStackFrame
			implements Allocatable<DebugStackFrameOp> {

		@Override
		public boolean isImmedite() {
			return true;
		}

		@Override
		public int getPriority() {
			return NORMAL_ALLOC_PRIORITY;
		}

		@Override
		public DebugStackFrameOp allocate(
				AllocationCode<DebugStackFrameOp> code) {
			return code.allocate(DEBUG_STACK_FRAME_TYPE);
		}

		@Override
		public void initialize(
				AllocationCode<DebugStackFrameOp> code) {
		}

		@Override
		public void dispose(
				Code code,
				Allocated<DebugStackFrameOp> allocated) {
		}

	}

	private static final class TraceBeforReturn implements Disposal {

		@Override
		public void dispose(Code code) {

			final Debug debug = code.getGenerator().getDebug();

			if (debug.dontExitFrom != code) {
				debug.exitFunc().op(null, code).exit(code);
			}
		}

	}

}
