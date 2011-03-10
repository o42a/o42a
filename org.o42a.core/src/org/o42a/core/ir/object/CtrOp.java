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

import static org.o42a.core.ir.object.ObjectDataType.OBJECT_DATA_TYPE;
import static org.o42a.core.ir.object.ObjectOp.anonymousObject;
import static org.o42a.core.ir.op.ObjectRefFunc.OBJECT_REF;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.codegen.code.CodePtr;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.*;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.op.IROp;
import org.o42a.core.ir.op.ObjectRefFunc;


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
			Code code,
			CodePos exit,
			ObjectOp scope,
			ObjectRefFunc ancestorFunc,
			ObjectOp sample,
			int flags) {
		code.debug(
				"--- newObject: sample=" + sample
				+ ", ancestorFunc=" + ancestorFunc
				+ ", scope=" + scope
				+ ", flags=" + Integer.toHexString(flags));

		ptr().scopeType(code).store(code, scope.data(code).ptr());
		ptr().ancestorFunc(code).store(code, ancestorFunc);
		ptr().type(code).store(code, sample.data(code).ptr());
		ptr().flags(code).store(code, code.int32(flags));

		final AnyOp result = newFunc().op(code).call(code, ptr().toAny(code));

		result.isNull(code).go(code, exit);

		return anonymousObject(
				sample.getBuilder(),
				result,
				sample.getWellKnownType());
	}

	public ObjectOp newObject(
			Code code,
			CodePos exit,
			ObjectOp ancestor,
			ObjectOp sample,
			int flags) {
		code.debug(
				"--- newObject: sample=" + sample
				+ ", ancestor=" + ancestor
				+ ", flags=" + Integer.toHexString(flags));

		ptr().scopeType(code).store(code, code.nullPtr(OBJECT_DATA_TYPE));
		ptr().ancestorFunc(code).store(
				code,
				code.nullPtr(OBJECT_REF));
		ptr().ancestorType(code).store(
				code,
				ancestor != null
				? ancestor.data(code).ptr()
				: code.nullPtr(OBJECT_DATA_TYPE));
		ptr().type(code).store(code, sample.data(code).ptr());
		ptr().flags(code).store(code, code.int32(flags));

		final AnyOp result = newFunc().op(code).call(code, ptr().toAny(code));

		result.isNull(code).go(code, exit);

		return anonymousObject(
				sample.getBuilder(),
				result,
				sample.getWellKnownType());
	}

	private CodePtr<ObjectRefFunc> newFunc() {
		return getGenerator().externalFunction(
				"o42a_obj_new",
				OBJECT_REF);
	}

	public static final class Op extends StructOp {

		private Op(StructWriter writer) {
			super(writer);
		}

		@Override
		public final Type getType() {
			return (Type) super.getType();
		}

		public final DataOp<ObjectDataType.Op> scopeType(Code code) {
			return writer().ptr(code, getType().scopeType());
		}

		public final CodeOp<ObjectRefFunc> ancestorFunc(Code code) {
			return writer().func(code, getType().ancestorFunc());
		}

		public final DataOp<ObjectDataType.Op> ancestorType(Code code) {
			return writer().ptr(code, getType().ancestorType());
		}

		public final DataOp<ObjectDataType.Op> type(Code code) {
			return writer().ptr(code, getType().type());
		}

		public final DataOp<Int32op> flags(Code code) {
			return writer().int32(code, getType().flags());
		}

		public final CtrOp op(CodeBuilder builder) {
			return new CtrOp(builder, this);
		}

	}

	public static final class Type extends org.o42a.codegen.data.Type<Op> {

		private StructPtrRec<ObjectDataType.Op> scopeType;
		private CodeRec<ObjectRefFunc> ancestorFunc;
		private StructPtrRec<ObjectDataType.Op> ancestorType;
		private StructPtrRec<ObjectDataType.Op> type;
		private Int32rec flags;

		private Type() {
		}

		@Override
		public Op op(StructWriter writer) {
			return new Op(writer);
		}

		public final StructPtrRec<ObjectDataType.Op> scopeType() {
			return this.scopeType;
		}

		public final CodeRec<ObjectRefFunc> ancestorFunc() {
			return this.ancestorFunc;
		}

		public final StructPtrRec<ObjectDataType.Op> ancestorType() {
			return this.ancestorType;
		}

		public final StructPtrRec<ObjectDataType.Op> type() {
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
			this.scopeType = data.addPtr("scope_type", OBJECT_DATA_TYPE);
			this.ancestorFunc = data.addCodePtr("ancestor_f", OBJECT_REF);
			this.ancestorType = data.addPtr("ancestor_type", OBJECT_DATA_TYPE);
			this.type = data.addPtr("type", OBJECT_DATA_TYPE);
			this.flags = data.addInt32("flags");
		}

	}

}
