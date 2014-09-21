/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.core.ir.field.link;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.object.type.DerivationUsage.ALL_DERIVATION_USAGES;

import org.o42a.core.ir.field.RefFld;
import org.o42a.core.ir.object.ObjectIRBody;
import org.o42a.core.ir.object.op.ObjectRefFn;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;
import org.o42a.core.value.TypeParameters;


public abstract class AbstractLinkFld<
		F extends RefFld.Op<F>,
		T extends RefFld.Type<F>>
				extends RefFld<F, T, ObjectRefFn> {

	public AbstractLinkFld(
			ObjectIRBody bodyIR,
			Field field,
			boolean dummy,
			Obj target,
			Obj targetAscendant) {
		super(bodyIR, field, dummy, target, targetAscendant);
	}

	@Override
	protected boolean mayOmit() {
		if (!super.mayOmit()) {
			return false;
		}
		return !getTarget().type().derivation().isUsed(
				getGenerator().getAnalyzer(),
				ALL_DERIVATION_USAGES);
	}

	@Override
	protected Obj targetType(Obj bodyType) {

		final Obj object =
				bodyType.member(getField().getKey())
				.toField()
				.object(dummyUser());

		final TypeParameters<?> parameters = object.type().getParameters();

		return parameters.getValueType()
				.toLinkType()
				.interfaceRef(parameters)
				.getType();
	}

}
