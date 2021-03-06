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
package org.o42a.core.ir.object;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.core.Container;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.member.field.Field;


public abstract class FieldIRBase extends ScopeIR {

	private Fld<?, ?> fld;
	private boolean targetAllocated;

	public FieldIRBase(Generator generator, Field field) {
		super(generator, field);
	}

	public final Field getField() {
		return getScope().toField();
	}

	public FldOp<?, ?> field(Code code, ObjOp host) {
		return this.fld.op(code, host);
	}

	@Override
	public void allocate() {
		getField().toObject().ir(getGenerator()).allocate();
	}

	@Override
	protected void targetAllocated() {

		final Container enclosingContainer = getField().getEnclosingContainer();

		if (enclosingContainer == null) {
			return;
		}

		enclosingContainer.getScope().ir(getGenerator()).allocate();
		if (this.fld != null) {
			this.fld.targetAllocated();
		} else {
			this.targetAllocated = true;
		}
	}

	protected abstract Fld<?, ?> declareFld(ObjectIRBody bodyIR);

	protected abstract Fld<?, ?> declareDummyFld(ObjectIRBody bodyIR);

	final Fld<?, ?> declare(ObjectIRBody bodyIR) {

		final Fld<?, ?> fld = declareFld(bodyIR);

		if (fld == null) {
			return null;
		}

		if (bodyIR.bodies().isTypeBodies()) {
			return fld;
		}

		this.fld = fld;
		if (this.targetAllocated) {
			this.targetAllocated = false;
			fld.targetAllocated();
		}

		return fld;
	}

	final Fld<?, ?> declareDummy(ObjectIRBody bodyIR) {

		final Fld<?, ?> fld = declareDummyFld(bodyIR);

		if (!bodyIR.bodies().isTypeBodies()) {
			this.fld = fld;
		}

		return fld;
	}

}
