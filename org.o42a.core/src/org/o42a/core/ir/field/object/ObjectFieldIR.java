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
package org.o42a.core.ir.field.object;

import static org.o42a.core.ir.local.RefLclOp.REF_LCL;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.field.FieldIR;
import org.o42a.core.ir.field.RefFld;
import org.o42a.core.ir.field.getter.GetterFld;
import org.o42a.core.ir.field.variable.VarFld;
import org.o42a.core.ir.local.RefLclOp;
import org.o42a.core.ir.object.ObjectIRBody;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.LinkUses;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.object.link.LinkValueType;


public final class ObjectFieldIR extends FieldIR {

	public ObjectFieldIR(Generator generator, Field field) {
		super(generator, field);
	}

	@Override
	protected HostOp createOp(CodeBuilder builder, Code code) {

		final Obj object = getField().toObject();

		return object.ir(getGenerator()).op(builder, code);
	}

	@Override
	protected RefFld<?> declare(SubData<?> data, ObjectIRBody bodyIR) {

		final RefFld<?> linkFld = declareLink(data, bodyIR);

		if (linkFld != null) {
			return linkFld;
		}

		final ObjFld fld = new ObjFld(bodyIR, getField());

		fld.allocate(data, getField().toObject());

		return fld;
	}

	private RefFld<?> declareLink(SubData<?> data, ObjectIRBody bodyIR) {

		final Field field = getField();
		final Obj object = field.toObject();
		final LinkValueType linkType =
				object.value().getValueType().toLinkType();

		if (linkType == null) {
			return null;
		}

		final LinkUses linkUses = object.type().linkUses();

		if (!linkUses.simplifiedLink(getGenerator().getAnalyzer())) {
			return null;
		}

		final Obj ascendant =
				object.value()
				.getValueStruct()
				.toLinkStruct()
				.getTypeRef()
				.getType();
		final Obj target;
		final DefTarget defTarget = object.value().getDefinitions().target();

		assert defTarget.exists() :
			"Link target does not exist: " + object;

		if (defTarget.isUnknown()) {
			target = ascendant;
		} else {
			target = defTarget.getRef().getResolution().toObject();
		}

		final RefFld<?> fld;

		if (linkType == LinkValueType.LINK) {
			fld = new GetterFld(bodyIR, field, target);
		} else if (linkType == LinkValueType.VARIABLE) {
			fld = new VarFld(bodyIR, field, target);
		} else {
			return null;
		}

		fld.allocate(data, ascendant);

		return fld;
	}

	@Override
	protected RefLclOp allocateLocal(CodeBuilder builder, Code code) {
		return code.allocate(null, REF_LCL).op(builder, this);
	}

}
