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

import static org.o42a.core.ir.IRUtil.encodeMemberId;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.data.SubData;
import org.o42a.core.Container;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.IRGenerator;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.local.LocalFieldIRBase;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.Field;


public abstract class FieldIRBase<A extends Artifact<A>>
		extends LocalFieldIRBase<A> {

	private final CodeId id;
	private Fld lastFld;
	private boolean targetAllocated;

	public FieldIRBase(IRGenerator generator, Field<A> field) {
		super(generator, field);
		this.id = encodeMemberId(
				field.getEnclosingScope().ir(generator),
				field.toMember());
	}

	@Override
	public final CodeId getId() {
		return this.id;
	}

	public FldOp field(Code code, ObjOp host) {
		assertNotLocal();
		return this.lastFld.op(code, host);
	}

	@Override
	public void allocate() {
		getField().getArtifact().materialize().ir(getGenerator()).allocate();
	}

	@Override
	protected void targetAllocated() {

		final Container enclosingContainer = getField().getEnclosingContainer();

		if (enclosingContainer == null) {
			return;
		}

		enclosingContainer.getScope().ir(getGenerator()).allocate();
		if (this.lastFld != null) {
			this.lastFld.targetAllocated();
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

		return this.lastFld.op(code, host);
	}

	Fld allocate(SubData<?> data, ObjectBodyIR bodyIR) {
		assertNotLocal();

		final Member declaredField =
			bodyIR.getAscendant().member(getField().getKey());

		if (declaredField.isOverride()) {
			return null;
		}

		final Fld fld = declare(data, bodyIR);

		if (fld == null) {
			return null;
		}

		this.lastFld = fld;
		if (this.targetAllocated) {
			this.targetAllocated = false;
			fld.targetAllocated();
		}

		return fld;
	}

}
