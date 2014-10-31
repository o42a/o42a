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

import static org.o42a.core.member.field.FieldUsage.ALL_FIELD_USAGES;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.OpMeans;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Content;
import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.field.local.LocalIR;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIRBodies;
import org.o42a.core.ir.object.ObjectIRBody;
import org.o42a.core.ir.object.vmt.VmtRecord;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;
import org.o42a.util.string.ID;


public abstract class Fld<F extends Fld.Op<F>, T extends Fld.Type<F>>
		implements FldIR<F, T> {

	public static final ID FIELD_ID = ID.id("field");
	public static final ID FLD_ID = ID.id("fld");

	private final ObjectIRBody bodyIR;
	private final Field field;
	private T instance;
	private final boolean dummy;
	private byte omitted;

	public Fld(ObjectIRBody bodyIR, Field field, boolean dummy) {
		assert field
		.toMember()
		.getAnalysis()
		.isUsed(bodyIR.getGenerator().getAnalyzer(), ALL_FIELD_USAGES) :
				"Attempt to generate never accessed field " + field;
		this.bodyIR = bodyIR;
		this.field = field;
		this.dummy = dummy;
	}

	@Override
	public final ID getId() {
		return getField().getId();
	}

	@Override
	public final ObjectIRBody getBodyIR() {
		return this.bodyIR;
	}

	public final Field getField() {
		return this.field;
	}

	public final MemberKey getKey() {
		return getField().getKey();
	}

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
	public final boolean isStateless() {
		return getKind().isStateless() || isOmitted();
	}

	public final boolean isDummy() {
		return this.dummy;
	}

	@Override
	public final Obj getDeclaredIn() {
		return getKey().getOrigin().toObject();
	}

	@Override
	public T getInstance() {
		assert !isStateless() :
			this + " is stateless";
		if (this.instance == null) {
			getBodyIR().bodies().getStruct().allocate();
			assert this.instance != null :
				this + " not allocated";
		}
		return this.instance;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final Fld<F, T> get(ObjectIRBodies bodies) {
		return (Fld<F, T>) bodies.fld(getKey());
	}

	@Override
	public abstract VmtRecord vmtRecord();

	public final FldOp<F, T> op(Code code, ObjOp host) {
		return op(
				code,
				host,
				isStateless()
				? null
				: code.means(c -> host.ptr().field(c, getTypeInstance())));
	}

	@Override
	public final void allocate(SubData<?> data) {
		if (isStateless()) {
			return;
		}
		this.instance = data.addNewInstance(
				FLD_ID.detail(getId()),
				getType(),
				isDummy() ? dummyContent() : content());
	}

	public void targetAllocated() {
	}

	@Override
	public final Fld<F, T> toFld() {
		return this;
	}

	@Override
	public final LocalIR toLocal() {
		return null;
	}

	@Override
	public String toString() {
		if (this.field == null) {
			return super.toString();
		}
		return this.field.toString();
	}

	protected boolean mayOmit() {
		return !getField()
				.toMember()
				.getAnalysis()
				.derivation()
				.isUsed(getGenerator());
	}

	protected abstract T getType();

	protected abstract Content<T> content();

	protected abstract Content<T> dummyContent();

	protected abstract FldOp<F, T> op(Code code, ObjOp host, OpMeans<F> ptr);

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
