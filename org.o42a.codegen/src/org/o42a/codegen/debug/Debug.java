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

import static org.o42a.codegen.debug.DbgEnterFunc.DBG_ENTER;
import static org.o42a.codegen.debug.DbgExitFunc.EXIT_SIGNATURE;
import static org.o42a.codegen.debug.DbgStackFrameType.DBG_STACK_FRAME_TYPE;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.CodeCallback;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.data.*;
import org.o42a.codegen.debug.DbgStackFrameType.Op;


public class Debug {

	public static final CodeCallback DEBUG_CODE_CALLBACK =
		new DebugCodeCallback();

	private final Generator generator;

	private boolean debug;

	private boolean writeDebug;

	private CodePtr<DbgEnterFunc> enterFunc;
	private CodePtr<DbgExitFunc> exitFunc;
	private DebugInfo info;

	private final HashMap<String, Ptr<AnyOp>> names =
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
			Signature<F> signature,
			CodePtr<F> function) {
		if (this.writeDebug) {
			return;
		}
		if (!checkDebug()) {
			return;
		}

		final DbgFunc dbgFunc =
			this.info.functions().addFunction(id, signature, function);

		final Function<F> code = function.getFunction();

		if (code != null) {

			final Ptr<AnyOp> namePtr =
				addASCIIString(
						getGenerator()
						.id("DEBUG")
						.sub("FUNC_NAME")
						.sub(id),
						id.getId());

			dbgFunc.setNamePtr(namePtr);

			final Op stackFrame = code.allocate(DBG_STACK_FRAME_TYPE);

			stackFrame.name(code).store(code, namePtr.op(code));
			this.enterFunc.op(code).enter(code, stackFrame);
		}
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

	public void write() {
		getGenerator().getGlobals().write();
		if (checkDebug()) {
			this.writeDebug = true;
			try {
				getGenerator().newGlobal().export().setConstant().struct(
						this.info);
				getGenerator().getGlobals().write();
			} finally {
				this.writeDebug = false;
			}
		}
	}

	final Ptr<AnyOp> addASCIIString(CodeId id, String value) {
		return getGenerator().addBinary(id, nullTermASCIIString(value));
	}

	final Ptr<AnyOp> allocateName(CodeId id, String value) {

		final Ptr<AnyOp> found = this.names.get(value);

		if (found != null) {
			return found;
		}

		final Ptr<AnyOp> binary = addASCIIString(id, value);

		this.names.put(value, binary);

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

	private final boolean checkDebug() {
		if (!isDebug()) {
			return false;
		}
		if (this.info != null) {
			return true;
		}

		this.writeDebug = true;
		try {
			this.enterFunc = getGenerator().externalFunction(
					"o42a_dbg_enter",
					DBG_ENTER);
			this.exitFunc = getGenerator().externalFunction(
					"o42a_dbg_exit",
					EXIT_SIGNATURE);
			this.info = new DebugInfo();
		} finally {
			this.writeDebug = false;
		}
		return true;
	}

	private static byte[] nullTermASCIIString(String string) {

		final CharsetEncoder encoder = Charset.forName("ASCII").newEncoder();
		final int length = string.length();
		final byte[] result = new byte[length + 1];

		final CharBuffer chars = CharBuffer.wrap(string);
		final ByteBuffer bytes = ByteBuffer.wrap(result);

		encoder.encode(chars, bytes, true);

		return result;
	}

	private static final class DebugCodeCallback implements CodeCallback {

		@Override
		public void beforeReturn(Code code) {

			final Debug debug = code.getGenerator().getDebug();

			debug.exitFunc.op(code).exit(code);
		}

	}

}
