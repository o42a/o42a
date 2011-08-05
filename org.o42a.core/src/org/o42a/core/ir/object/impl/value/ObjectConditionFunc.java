/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.core.ir.object.impl.value;

import org.o42a.codegen.data.FuncRec;
import org.o42a.core.def.DefValue;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.object.ObjectIRData;
import org.o42a.core.ir.object.ObjectValueIR;
import org.o42a.core.ir.op.ObjectCondFunc;
import org.o42a.core.ref.Resolver;


public final class ObjectConditionFunc extends ObjectValueIRCondFunc {

	public ObjectConditionFunc(ObjectValueIR valueIR) {
		super(valueIR);
	}

	@Override
	public boolean isRequirement() {
		return false;
	}

	@Override
	protected String suffix() {
		return "condition";
	}

	@Override
	protected DefValue value(Definitions definitions) {

		final Resolver resolver = definitions.getScope().dummyResolver();

		return definitions.conditions().resolve(resolver);
	}

	@Override
	protected FuncRec<ObjectCondFunc> func(ObjectIRData data) {
		return data.conditionFunc();
	}

}
