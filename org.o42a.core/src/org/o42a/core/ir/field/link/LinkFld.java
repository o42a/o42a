/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.core.ir.field.link;

import static org.o42a.core.ir.object.op.ObjectRefFunc.OBJECT_REF;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.field.FldKind;
import org.o42a.core.ir.field.RefFld;
import org.o42a.core.ir.object.ObjBuilder;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjectRefFunc;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;
import org.o42a.util.string.ID;


public class LinkFld extends AbstractLinkFld<LinkFld.Op> {

	public static final Type LINK_FLD = new Type();

	public LinkFld(Field field, Obj target) {
		super(field, target);
	}

	@Override
	public final FldKind getKind() {
		return FldKind.LINK;
	}

	@Override
	public Type getInstance() {
		return (Type) super.getInstance();
	}

	@Override
	protected Type getType() {
		return LINK_FLD;
	}

	@Override
	protected void buildConstructor(ObjBuilder builder, CodeDirs dirs) {

		final Block code = dirs.code();
		final ObjectOp result = construct(builder, dirs);

		result.toData(null, code).returnValue(code);
	}

	@Override
	protected LinkFldOp op(Code code, ObjOp host, Op ptr) {
		return new LinkFldOp(this, host, ptr);
	}

	public static final class Op extends RefFld.Op<Op, ObjectRefFunc> {

		private Op(StructWriter<Op> writer) {
			super(writer);
		}

		@Override
		public final Type getType() {
			return (Type) super.getType();
		}

	}

	public static final class Type extends RefFld.Type<Op, ObjectRefFunc> {

		private Type() {
			super(ID.rawId("o42a_fld_link"));
		}

		@Override
		public boolean isStateless() {
			return true;
		}

		@Override
		public Op op(StructWriter<Op> writer) {
			return new Op(writer);
		}

		@Override
		protected DebugTypeInfo createTypeInfo() {
			return externalTypeInfo(0x042a0200 | FldKind.LINK.code());
		}

		@Override
		protected ObjectRefFunc.Signature getSignature() {
			return OBJECT_REF;
		}

		@Override
		protected FuncPtr<ObjectRefFunc> constructorStub() {
			throw new IllegalStateException(
					"The link constructor can not be a stub");
		}

	}

}
