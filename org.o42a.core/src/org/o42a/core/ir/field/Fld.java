/*
    Compiler Core
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
package org.o42a.core.ir.field;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Content;
import org.o42a.codegen.data.Data;
import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectBodyIR;
import org.o42a.core.member.MemberKey;
import org.o42a.core.object.Obj;
import org.o42a.util.string.ID;


public abstract class Fld implements FldIR {

	public static final ID FIELD_ID = ID.id("field");
	public static final ID FLD_ID = ID.id("fld");

	private final ObjectBodyIR bodyIR;
	private Type<?> instance;
	private byte omitted;

	public Fld(ObjectBodyIR bodyIR) {
		this.bodyIR = bodyIR;
	}

	public final Generator getGenerator() {
		return getBodyIR().getGenerator();
	}

	@Override
	public final ObjectBodyIR getBodyIR() {
		return this.bodyIR;
	}

	@Override
	public final Data<?> data(Generator generator) {
		return getInstance().data(generator);
	}

	public abstract MemberKey getKey();

	public final boolean isOmitted() {
		if (this.omitted != 0) {
			return this.omitted > 0;
		}
		if (mayOmit()) {
			this.omitted = 1;
			return true;
		}
		this.omitted = -1;
		return false;
	}

	@Override
	public abstract FldKind getKind();

	public abstract boolean isOverrider();

	@Override
	public abstract Obj getDeclaredIn();

	public abstract Obj getDefinedIn();

	public Type<?> getInstance() {
		return this.instance;
	}

	public abstract FldOp op(Code code, ObjOp host);

	public void targetAllocated() {
	}

	protected abstract boolean mayOmit();

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void allocate(SubData<?> data) {
		if (isOmitted()) {
			return;
		}
		this.instance = data.addInstance(
				FLD_ID.detail(getId().getLocal()),
				(Type) getType(),
				(Content) content());
	}

	protected abstract Type<?> getType();

	protected abstract Content<?> content();

	public static abstract class Op<S extends Op<S>> extends StructOp<S> {

		public Op(StructWriter<S> writer) {
			super(writer);
		}

		@Override
		public Type<S> getType() {
			return (Type<S>) super.getType();
		}

	}

	public static abstract class Type<S extends Op<S>>
			extends org.o42a.codegen.data.Type<S> {

		public Type(ID id) {
			super(id);
		}

	}

}
