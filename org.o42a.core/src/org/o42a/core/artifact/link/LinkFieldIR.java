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
package org.o42a.core.artifact.link;

import static org.o42a.core.ir.local.RefLclOp.REF_LCL;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.AllocationCode;
import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.field.*;
import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.local.RefLclOp;
import org.o42a.core.ir.object.ObjectBodyIR;
import org.o42a.core.member.field.Field;


final class LinkFieldIR extends FieldIR<Link> {

	LinkFieldIR(Generator generator, Field<Link> field) {
		super(generator, field);
	}

	@Override
	protected RefFld<?> declare(SubData<?> data, ObjectBodyIR bodyIR) {

		final RefFld<?> fld;
		final Field<Link> field = getField();

		if (field.getArtifact().isVariable()) {
			fld = new VarFld(bodyIR, field);
		} else {
			fld = new LinkFld(bodyIR, field);
		}

		fld.allocate(
				data,
				getField().getArtifact().getTypeRef().typeObject(dummyUser()));

		return fld;
	}

	@Override
	protected RefLclOp allocateLocal(
			LocalBuilder builder,
			AllocationCode code) {
		return code.allocate(null, REF_LCL).op(builder, this);
	}

}
