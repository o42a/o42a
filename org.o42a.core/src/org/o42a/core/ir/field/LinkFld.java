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
package org.o42a.core.ir.field;

import static org.o42a.core.ir.op.ObjectRefFunc.OBJECT_REF;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.core.artifact.link.Link;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectBodyIR;
import org.o42a.core.ir.op.ObjectRefFunc;
import org.o42a.core.member.field.Field;


public class LinkFld extends RefFld<ObjectRefFunc> {

	public static final Type LINK_FLD = new Type();

	public LinkFld(ObjectBodyIR bodyIR, Field<Link> field) {
		super(bodyIR, field);
	}

	@Override
	public final FldKind getKind() {
		return FldKind.LINK;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Field<Link> getField() {
		return (Field<Link>) super.getField();
	}

	@Override
	public Type getInstance() {
		return (Type) super.getInstance();
	}

	@Override
	public LinkFldOp op(Code code, ObjOp host) {
		return new LinkFldOp(
				this,
				host,
				host.ptr().writer().struct(code, getInstance()));
	}

	@Override
	protected Type getType() {
		return LINK_FLD;
	}

	public static final class Op extends RefFld.Op<ObjectRefFunc> {

		private Op(StructWriter writer) {
			super(writer);
		}

		@Override
		public final Type getType() {
			return (Type) super.getType();
		}

		@Override
		public Op create(StructWriter writer) {
			return new Op(writer);
		}

		@Override
		protected AnyOp construct(
				Code code,
				ObjOp host,
				ObjectRefFunc constructor) {
			return constructor.call(code, host.toAny(code));
		}

	}

	public static final class Type extends RefFld.Type<Op, ObjectRefFunc> {

		private Type() {
		}

		@Override
		public Op op(StructWriter writer) {
			return new Op(writer);
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.id("LinkFld");
		}

		@Override
		protected Signature<ObjectRefFunc> signature() {
			return OBJECT_REF;
		}

	}

}
