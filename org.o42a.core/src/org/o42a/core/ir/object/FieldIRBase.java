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
package org.o42a.core.ir.object;

import static org.o42a.core.ir.IRNames.encodeMemberId;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.data.SubData;
import org.o42a.core.Container;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.local.LocalFieldIRBase;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;
import org.o42a.util.string.ID;


public abstract class FieldIRBase extends LocalFieldIRBase {

	private final ID id;
	private Fld fld;
	private boolean targetAllocated;

	public FieldIRBase(Generator generator, Field field) {
		super(generator, field);
		this.id = encodeMemberId(
				field.getEnclosingScope().ir(generator),
				field.toMember());
	}

	@Override
	public final ID getId() {
		return this.id;
	}

	public FldOp field(Code code, ObjOp host) {
		assertNotLocal();
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

	protected abstract Fld declare(SubData<?> data, ObjectBodyIR bodyIR);

	@Override
	protected HostOp createOp(CodeBuilder builder, Code code) {
		assertNotLocal();

		final Obj owner = getField().getEnclosingContainer().toObject();
		final ObjOp host = owner.ir(getGenerator()).op(builder, code);

		return this.fld.op(code, host);
	}

	Fld allocate(SubData<?> data, ObjectBodyIR bodyIR) {
		assertNotLocal();

		final Fld fld = declare(data, bodyIR);

		if (fld == null) {
			return null;
		}

		this.fld = fld;
		if (this.targetAllocated) {
			this.targetAllocated = false;
			fld.targetAllocated();
		}

		return fld;
	}

}
