/*
    Compiler Core
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
package org.o42a.core.ir.object;

import static org.o42a.core.ir.object.ObjectOp.anonymousObject;
import static org.o42a.core.ir.object.ObjectType.OBJECT_TYPE;
import static org.o42a.core.ir.op.NewObjectFunc.NEW_OBJECT;
import static org.o42a.core.ir.op.ObjectRefFunc.OBJECT_REF;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodeBlk;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.*;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.op.*;


public class CtrOp extends IROp {

	public static final Type CTR_TYPE = new Type();

	public static final int NEW_INSTANCE = 0;
	public static final int PROPAGATION = 1;

	private CtrOp(CodeBuilder builder, Op ptr) {
		super(builder, ptr);
	}

	@Override
	public final Op ptr() {
		return (Op) super.ptr();
	}

	public ObjectOp newObject(
			CodeDirs dirs,
			ObjectOp scope,
			ObjectRefFunc ancestorFunc,
			ObjectOp sample,
			int flags) {

		final Code code = dirs.code();

		dirs = dirs.begin(
				"new_object",
				"New object: sample=" + sample
				+ ", ancestorFunc=" + ancestorFunc
				+ ", scope=" + scope
				+ ", flags=" + Integer.toHexString(flags));

		ptr().scopeType(code).store(code, scope.objectType(code).ptr());
		ptr().ancestorFunc(code).store(code, ancestorFunc);
		ptr().type(code).store(code, sample.objectType(code).ptr());
		ptr().flags(code).store(code, code.int32(flags));

		final DataOp result = newFunc().op(null, code).newObject(code, this);
		final CodeBlk nullObject = code.addBlock("null_new_object");

		result.isNull(null, code).go(code, nullObject.head());

		if (nullObject.exists()) {
			dirs.goWhenFalse(nullObject);
		}

		dirs.end();

		return anonymousObject(
				sample.getBuilder(),
				result,
				sample.getWellKnownType());
	}

	public ObjectOp newObject(
			CodeDirs dirs,
			ObjectOp ancestor,
			ObjectOp sample,
			int flags) {

		final Code code = dirs.code();

		dirs = dirs.begin(
				"new_object",
				"New object: sample=" + sample
				+ ", ancestor=" + ancestor
				+ ", flags=" + Integer.toHexString(flags));

		ptr().scopeType(code).store(code, code.nullPtr(OBJECT_TYPE));
		ptr().ancestorFunc(code).store(
				code,
				code.nullPtr(OBJECT_REF));
		ptr().ancestorType(code).store(
				code,
				ancestor != null
				? ancestor.objectType(code).ptr()
				: code.nullPtr(OBJECT_TYPE));
		ptr().type(code).store(code, sample.objectType(code).ptr());
		ptr().flags(code).store(code, code.int32(flags));

		final DataOp result = newFunc().op(null, code).newObject(code, this);
		final CodeBlk nullObject = code.addBlock("null_new_object");

		result.isNull(null, code).go(code, nullObject.head());

		if (nullObject.exists()) {
			dirs.goWhenFalse(nullObject);
		}

		dirs.end();

		return anonymousObject(
				sample.getBuilder(),
				result,
				sample.getWellKnownType());
	}

	private FuncPtr<NewObjectFunc> newFunc() {
		return getGenerator().externalFunction("o42a_obj_new", NEW_OBJECT);
	}

	public static final class Op extends StructOp {

		private Op(StructWriter writer) {
			super(writer);
		}

		@Override
		public final Type getType() {
			return (Type) super.getType();
		}

		public final RecOp<ObjectType.Op> scopeType(Code code) {
			return ptr(null, code, getType().scopeType());
		}

		public final FuncOp<ObjectRefFunc> ancestorFunc(Code code) {
			return func(null, code, getType().ancestorFunc());
		}

		public final RecOp<ObjectType.Op> ancestorType(Code code) {
			return ptr(null, code, getType().ancestorType());
		}

		public final RecOp<ObjectType.Op> type(Code code) {
			return ptr(null, code, getType().type());
		}

		public final RecOp<Int32op> flags(Code code) {
			return int32(null, code, getType().flags());
		}

		public final CtrOp op(CodeBuilder builder) {
			return new CtrOp(builder, this);
		}

	}

	public static final class Type extends org.o42a.codegen.data.Type<Op> {

		private StructRec<ObjectType.Op> scopeType;
		private FuncRec<ObjectRefFunc> ancestorFunc;
		private StructRec<ObjectType.Op> ancestorType;
		private StructRec<ObjectType.Op> type;
		private Int32rec flags;

		private Type() {
		}

		@Override
		public Op op(StructWriter writer) {
			return new Op(writer);
		}

		public final StructRec<ObjectType.Op> scopeType() {
			return this.scopeType;
		}

		public final FuncRec<ObjectRefFunc> ancestorFunc() {
			return this.ancestorFunc;
		}

		public final StructRec<ObjectType.Op> ancestorType() {
			return this.ancestorType;
		}

		public final StructRec<ObjectType.Op> type() {
			return this.type;
		}

		public final Int32rec flags() {
			return this.flags;
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.id("Ctr");
		}

		@Override
		protected void allocate(SubData<Op> data) {
			this.scopeType = data.addPtr("scope_type", OBJECT_TYPE);
			this.ancestorFunc = data.addFuncPtr("ancestor_f", OBJECT_REF);
			this.ancestorType = data.addPtr("ancestor_type", OBJECT_TYPE);
			this.type = data.addPtr("type", OBJECT_TYPE);
			this.flags = data.addInt32("flags");
		}

	}

}
