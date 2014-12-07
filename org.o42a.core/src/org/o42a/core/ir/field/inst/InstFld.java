/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.core.ir.field.inst;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Content;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.data.Type;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.ir.field.FldIR;
import org.o42a.core.ir.field.FldKind;
import org.o42a.core.ir.field.local.LocalIR;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIRBodies;
import org.o42a.core.ir.object.ObjectIRBody;
import org.o42a.core.object.Obj;
import org.o42a.util.string.ID;


public abstract class InstFld<F extends StructOp<F>, T extends Type<F>>
		implements FldIR<F, T> {

	private final ObjectIRBody bodyIR;
	private T instance;

	public InstFld(ObjectIRBody bodyIR) {
		this.bodyIR = bodyIR;
	}

	@Override
	public final ID getId() {
		return getInstFldKind().getId();
	}

	@Override
	public final FldKind getKind() {
		return getInstFldKind().getKind();
	}

	public abstract InstFldKind getInstFldKind();

	@Override
	public boolean requiresLock() {
		return false;
	}

	@Override
	public final Obj getDeclaredIn() {
		return getBodyIR().getSampleDeclaration();
	}

	@Override
	public final ObjectIRBody getBodyIR() {
		return this.bodyIR;
	}

	@Override
	public T getInstance() {
		if (this.instance == null) {
			getBodyIR().bodies().getStruct().allocate();
			assert this.instance != null :
				this + " not allocated";
		}
		return this.instance;
	}

	public abstract InstFldOp<F, T> op(Code code, ObjOp host);

	@Override
	public final void allocate(SubData<?> data) {
		this.instance = data.addNewInstance(
				getId(),
				getType(),
				content());
	}

	@SuppressWarnings("unchecked")
	@Override
	public InstFld<F, T> get(ObjectIRBodies bodies) {
		return (InstFld<F, T>) bodies.instFld(getInstFldKind());
	}

	public abstract InstFld<F, T> derive(ObjectIRBody inheritantBodyIR);

	@Override
	public final Fld<?, ?> toFld() {
		return null;
	}

	@Override
	public final LocalIR toLocal() {
		return null;
	}

	protected abstract T getType();

	protected abstract Content<T> content();

}
