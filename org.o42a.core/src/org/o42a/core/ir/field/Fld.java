/*
    Compiler Core
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
package org.o42a.core.ir.field;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Content;
import org.o42a.codegen.data.Data;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIRBody;
import org.o42a.core.ir.object.ObjectIRBodyData;
import org.o42a.core.member.MemberKey;
import org.o42a.core.object.Obj;
import org.o42a.util.string.ID;


public abstract class Fld<F extends Fld.Op<F>> implements FldIR {

	public static final ID FIELD_ID = ID.id("field");
	public static final ID FLD_ID = ID.id("fld");

	private ObjectIRBody bodyIR;
	private Type<F> instance;
	private byte omitted;

	public final Generator getGenerator() {
		return getBodyIR().getGenerator();
	}

	@Override
	public final ObjectIRBody getBodyIR() {
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

	public abstract boolean isDummy();

	@Override
	public abstract FldKind getKind();

	public abstract boolean isOverrider();

	@Override
	public final Obj getDeclaredIn() {
		return getKey().getOrigin().toObject();
	}

	public abstract Obj getDefinedIn();

	public Type<F> getInstance() {
		return this.instance;
	}

	public final FldOp<F> op(Code code, ObjOp host) {
		return op(
				code,
				host,
				isOmitted() ? null : host.ptr().field(code, getInstance()));
	}

	public void targetAllocated() {
	}

	protected abstract boolean mayOmit();

	protected void allocate(ObjectIRBodyData data) {
		this.bodyIR = data.getBodyIR();
		data.declareFld(this);
		if (isOmitted()) {
			return;
		}
		this.instance = data.getData().addInstance(
				FLD_ID.detail(getId().getLocal()),
				getType(),
				isDummy() ? dummyContent() : content());
	}

	protected abstract Type<F> getType();

	protected abstract Content<? extends Type<F>> content();

	protected abstract Content<? extends Type<F>> dummyContent();

	protected abstract FldOp<F> op(Code code, ObjOp host, F ptr);

	public static abstract class Op<F extends Op<F>> extends StructOp<F> {

		public Op(StructWriter<F> writer) {
			super(writer);
		}

		@Override
		public Type<F> getType() {
			return (Type<F>) super.getType();
		}

	}

	public static abstract class Type<F extends Op<F>>
			extends org.o42a.codegen.data.Type<F> {

		public Type(ID id) {
			super(id);
		}

	}

}
