/*
    Test Framework
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
package org.o42a.lib.test.rt;

import static org.o42a.core.member.MemberId.memberName;
import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;

import org.o42a.codegen.code.Code;
import org.o42a.common.intrinsic.IntrinsicObject;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.value.ValueType;
import org.o42a.lib.test.TestModule;


public class RtFalse extends IntrinsicObject {

	public RtFalse(TestModule module) {
		super(
				module.toMemberOwner(),
				fieldDeclaration(
						module,
						module.distribute(),
						memberName("rt-false")));
		setValueType(ValueType.VOID);
	}

	@Override
	protected Ascendants createAscendants() {
		return new Ascendants(this).setAncestor(
				getValueType().typeRef(this, getScope().getEnclosingScope()));
	}

	@Override
	protected Definitions explicitDefinitions() {
		return getValueType().runtimeDef(this, distribute()).toDefinitions();
	}

	@Override
	protected ObjectValueIR createValueIR(ObjectIR objectIR) {
		return new ValueIR(objectIR);
	}

	private static final class ValueIR extends ProposedValueIR {

		ValueIR(ObjectIR objectIR) {
			super(objectIR);
		}

		@Override
		protected void proposition(Code code, ValOp result, ObjectOp host) {
			code.debug("Run-time false");
			result.storeFalse(code);
		}

	}

}
