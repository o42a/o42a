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
package org.o42a.core.ir.field.object;

import org.o42a.codegen.Generator;
import org.o42a.core.ir.field.FieldIR;
import org.o42a.core.ir.field.RefFld;
import org.o42a.core.ir.field.link.EagerLinkFld;
import org.o42a.core.ir.field.link.LinkFld;
import org.o42a.core.ir.field.variable.VarFld;
import org.o42a.core.ir.object.ObjectIRBody;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.LinkUses;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.link.LinkValueType;


public final class ObjectFieldIR extends FieldIR {

	public ObjectFieldIR(Generator generator, Field field) {
		super(generator, field);
	}

	@Override
	protected RefFld<?, ?, ?, ?> declareFld(ObjectIRBody bodyIR) {
		return declareFld(bodyIR, false);
	}

	@Override
	protected RefFld<?, ?, ?, ?> declareDummyFld(ObjectIRBody bodyIR) {
		return declareFld(bodyIR, true);
	}

	private RefFld<?, ?, ?, ?> declareFld(ObjectIRBody bodyIR, boolean dummy) {

		final RefFld<?, ?, ?, ?> linkFld = declareLink(bodyIR, dummy);

		if (linkFld != null) {
			return linkFld;
		}

		if (getField()
				.getFirstDeclaration()
				.toObject()
				.analysis()
				.overridersEscapeMode(getGenerator().getEscapeAnalyzer())
				.isEscapePossible()) {
			return new ObjFld(bodyIR, getField(), dummy);
		}

		return new SAObjFld(bodyIR, getField(), dummy);
	}

	private RefFld<?, ?, ?, ?> declareLink(ObjectIRBody bodyIR, boolean dummy) {

		final Field field = getField();
		final Obj object = field.toObject();
		final LinkValueType linkType =
				object.type().getValueType().toLinkType();

		if (linkType == null) {
			return null;
		}

		final LinkUses linkUses = object.type().linkUses();

		if (!linkUses.simplifiedLink(getGenerator().getAnalyzer())) {
			return null;
		}

		final TypeParameters<?> parameters = object.type().getParameters();
		final Obj ascendant =
				dummy
				? null
				: parameters.getValueType()
				.toLinkType()
				.interfaceRef(parameters)
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

		if (linkType.is(LinkValueType.LINK)) {
			if (!object.value().getStatefulness().isEager()) {
				return new LinkFld(bodyIR, field, dummy, target, ascendant);
			}
			return new EagerLinkFld(bodyIR, field, dummy, target, ascendant);
		}
		if (linkType.is(LinkValueType.VARIABLE)) {
			return new VarFld(bodyIR, field, dummy, target, ascendant);
		}

		return null;
	}

}
