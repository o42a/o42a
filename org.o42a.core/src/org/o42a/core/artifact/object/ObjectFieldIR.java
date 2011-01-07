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
package org.o42a.core.artifact.object;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.IRGenerator;
import org.o42a.core.ir.field.FieldIR;
import org.o42a.core.ir.field.ObjFld;
import org.o42a.core.ir.field.RefFldOp;
import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.local.RefLclOp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectBodyIR;
import org.o42a.core.member.field.Field;


final class ObjectFieldIR extends FieldIR<Obj> {

	ObjectFieldIR(IRGenerator generator, Field<Obj> field) {
		super(generator, field);
	}

	@Override
	public RefFldOp field(Code code, ObjOp host) {
		return (RefFldOp) super.field(code, host);
	}

	@Override
	protected HostOp createOp(CodeBuilder builder, Code code) {

		final Obj object = getField().getArtifact();

		return object.ir(getGenerator()).op(builder, code);
	}

	@Override
	protected ObjFld declare(SubData<?> data, ObjectBodyIR bodyIR) {

		final ObjFld fld = new ObjFld(bodyIR, getField());

		fld.allocate(data, getField().getArtifact());

		return fld;
	}

	@Override
	protected RefLclOp allocateLocal(LocalBuilder builder, Code code) {
		return code.allocate(getGenerator().refLclType()).op(builder, this);
	}

}
