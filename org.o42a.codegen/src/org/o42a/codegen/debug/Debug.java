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

import static org.o42a.codegen.debug.DbgExitFunc.EXIT_SIGNATURE;

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


public abstract class Debug extends Globals {

	private static final CodeCallback DEBUG_CODE_CALLBACK =
		new DebugCodeCallback();

	private boolean debug;

	private boolean writeDebug;

	private DbgFuncType dbgFuncType;
	private DbgFieldType dbgFieldType;
	private DbgGlobalType dbgGlobalType;
	private DbgStackFrameType dbgStackFrameType;
	private CodePtr<DbgEnterFunc> enterFunc;
	private CodePtr<DbgExitFunc> exitFunc;
	private DebugInfo info;

	private final HashMap<Ptr<?>, DbgStruct> structs =
		new HashMap<Ptr<?>, DbgStruct>();

	public final boolean isDebug() {
		return this.debug;
	}

	public final void setDebug(boolean debug) {
		this.debug = debug;
	}

	@Override
	protected <F extends Func> void addFunction(
			CodeId id,
			Signature<F> signature,
			CodePtr<F> function) {
		super.addFunction(id, signature, function);
		if (this.writeDebug) {
			return;
		}
		if (!checkDebug()) {
			return;
		}

		final DbgFunc dbgFunc =
			this.info.getFunctions().addFunction(id, signature, function);

		final Function<F> code = function.getFunction();

		if (code != null) {

			final Ptr<AnyOp> namePtr =
				addASCIIString(
						generator()
						.id("DEBUG")
						.sub("FUNC_NAME")
						.sub(id),
						id.getId());

			dbgFunc.setNamePtr(namePtr);

			final Op stackFrame = code.allocate(dbgStackFrameType());

			stackFrame.name(code).store(code, namePtr.op(code));
			this.enterFunc.op(code).enter(code, stackFrame);
		}
	}

	@Override
	protected CodeCallback createCodeCallback(Function<?> function) {
		if (isDebug()) {
			return DEBUG_CODE_CALLBACK;
		}
		return super.createCodeCallback(function);
	}

	@Override
	protected void addType(SubData<?> type) {
		super.addType(type);
		if (this.writeDebug) {
			return;
		}
		if (!checkDebug()) {
			return;
		}
		this.writeDebug = true;
		try {
			writeStruct(type);
		} finally {
			this.writeDebug = false;
		}
	}

	@Override
	protected void addGlobal(SubData<?> global) {
		super.addGlobal(global);
		if (this.writeDebug) {
			return;
		}
		if (!checkDebug()) {
			return;
		}
		this.info.getGlobals().addGlobal(global);
	}

	@Override
	protected void writeData() {
		super.writeData();
		if (checkDebug()) {
			this.writeDebug = true;
			try {
				newGlobal().export().setConstant().create(this.info);
				super.writeData();
			} finally {
				this.writeDebug = false;
			}
		}
	}

	final DbgFuncType dbgFuncType() {
		return this.dbgFuncType;
	}

	final DbgFieldType dbgFieldType() {
		return this.dbgFieldType;
	}

	final DbgGlobalType dbgGlobalType() {
		return this.dbgGlobalType;
	}

	final DbgStackFrameType dbgStackFrameType() {
		return this.dbgStackFrameType;
	}

	final Ptr<AnyOp> addASCIIString(CodeId id, String value) {
		return addBinary(id, nullTermASCIIString(value));
	}

	final void setName(AnyPtrRec field, CodeId id, String value) {

		final Ptr<AnyOp> binary = addASCIIString(id, value);

		field.setValue(binary);
	}

	DbgStruct writeStruct(Data<?> fieldData) {

		final Type<?> type;

		if (fieldData.getDataType() == DataType.STRUCT) {

			final SubData<?> fieldStruct = (SubData<?>) fieldData;

			type = fieldStruct.getType().getOriginal();
		} else if (fieldData instanceof StructPtrRec) {
			type = ((StructPtrRec<?>) fieldData).getType().getOriginal();
		} else {
			return null;
		}

		final Ptr<?> typePointer = type.getPointer();
		final DbgStruct found = this.structs.get(typePointer);

		if (found != null) {
			return found;
		}

		final DbgStruct struct = new DbgStruct(type);

		newGlobal().setConstant().create(struct);

		this.structs.put(typePointer, struct);

		return struct;
	}

	DbgStruct typeStruct(Type<?> type) {
		return this.structs.get(type.getOriginal().getPointer());
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
			this.dbgFuncType = addType(new DbgFuncType(generator()));
			this.dbgFieldType = addType(new DbgFieldType());
			this.dbgGlobalType = addType(new DbgGlobalType(generator()));
			this.dbgStackFrameType =
				addType(new DbgStackFrameType());
			this.enterFunc = externalFunction(
					"o42a_dbg_enter",
					new DbgEnterFunc.EnterSignature(this));
			this.exitFunc =
				externalFunction("o42a_dbg_exit", EXIT_SIGNATURE);
			this.info = new DebugInfo(generator());
		} finally {
			this.writeDebug = false;
		}
		return true;
	}

	private final Generator generator() {
		return (Generator) this;
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

			final Debug debug = code.getGenerator();

			debug.exitFunc.op(code).exit(code);
		}

	}

}
